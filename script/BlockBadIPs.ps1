# ============================================
# BlockBadIPs.ps1
# 仅针对明确的攻击特征(SQL 注入 / 路径穿越)封禁来源 IP。
# UA 中的 curl / python / wget 等工具特征不再作为封禁依据。
# CDN 回源流量永不封禁，并自动清理过期规则。
# 用法: powershell -ExecutionPolicy Bypass -File BlockBadIPs.ps1 [-DryRun]
# ============================================

[CmdletBinding()]
param(
    [string] $LogFilePath   = '',
    [switch] $DryRun
)

# ---------------- Config ----------------
$scanLines        = 5000
$blockThreshold   = 200
$maxBlocksPerRun  = 20
$ruleMaxAgeDays   = 7
$purgeLegacyRules = $true
$rulePrefix       = 'Block_Dangerous_IP'
$scriptDir        = $PSScriptRoot
$auditLogPath     = Join-Path $scriptDir 'blocked_ips.log'
$whiteListFile    = Join-Path $scriptDir 'cdn_whitelist.txt'

# 成功响应达到该次数的 IP 判定为 CDN / 回源节点，永不封禁
$cdnSuccessGuard = 200

# 永不封禁的基础地址
$protectedIps = @(
    '127.0.0.1'
    '0.0.0.0'
    '255.255.255.255'
)

# 攻击特征:仅匹配 URI，不匹配 User-Agent / Referer
$attackUriRegex = @(
    'union\s+(all\s+)?select\s'
    '\bselect\b.{1,200}?\bfrom\b'
    '\binformation_schema\b'
    '\bsleep\s*\(\s*\d'
    '\bbenchmark\s*\('
    '\bwaitfor\s+delay\b'
    ';\s*(drop|delete|update|insert|truncate)\s'
    '\b(drop|truncate)\s+table\b'
    '\bor\s+1\s*=\s*1\b'
    '\.\./'
    '/etc/passwd'
    '/etc/shadow'
    '<script'
    'javascript\s*:'
    'document\s*\.\s*cookie'
    'base64_decode\s*\('
    'php://'
    '\x00'
)

# 工具类UA:仅记录审计，不封禁
$toolUaRegex = @(
    'sqlmap'
    'nikto'
    'nmap'
    'masscan'
    'zgrab'
    'zgrab'
    'acunetix'
    'nessus'
)

# ---------------- Locate log ----------------
function Resolve-LogPath {
    param([string] $Explicit)

    if ($Explicit -and (Test-Path -LiteralPath $Explicit)) {
        return $Explicit
    }

    $roots = @(
        'C:\Program Files\phpstudy_pro\Extensions'
        'C:\phpstudy_pro\Extensions'
        'D:\phpstudy_pro\Extensions'
        'E:\phpstudy_pro\Extensions'
        'C:\Users\Administrator\Desktop\bilibili'
    )

    foreach ($root in $roots) {
        if (-not (Test-Path -LiteralPath $root)) { continue }
        $hit = Get-ChildItem -LiteralPath $root -Directory -ErrorAction SilentlyContinue |
            Where-Object { $_.Name -like 'Nginx*' } |
            ForEach-Object { Join-Path $_.FullName 'logs\access.log' } |
            Where-Object { Test-Path -LiteralPath $_ } |
            Select-Object -First 1
        if ($hit) { return $hit }
    }

    return $null
}

function Write-AuditLog {
    param([string] $Message)
    $ts = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'
    Add-Content -LiteralPath $auditLogPath -Value "[$ts] $Message" -Encoding UTF8 -ErrorAction SilentlyContinue
}

function Exit-WithError {
    param([string] $Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
    Write-AuditLog "ERROR $Message"
    exit 1
}

# ---------------- IP helpers ----------------
function ConvertTo-UInt32 {
    param([string] $Ip)
    $p = $Ip.Split('.')
    if ($p.Count -ne 4) { return $null }
    $v = [uint64] 0
    foreach ($part in $p) {
        $n = 0
        if (-not [int]::TryParse($part, [ref] $n)) { return $null }
        if ($n -lt 0 -or $n -gt 255) { return $null }
        $v = ($v -shl 8) -bor [uint64] $n
    }
    return $v
}

function Test-ValidIPv4 {
    param([string] $Ip)
    return ($null -ne (ConvertTo-UInt32 $Ip))
}

function Test-ProtectedIPv4 {
    param([string] $Ip)

    if ($protectedIps -contains $Ip) { return $true }

    $p = $Ip.Split('.') | ForEach-Object { [int] $_ }
    if ($p[0] -eq 10) { return $true }
    if ($p[0] -eq 127) { return $true }
    if ($p[0] -eq 169 -and $p[1] -eq 254) { return $true }
    if ($p[0] -eq 172 -and $p[1] -ge 16 -and $p[1] -le 31) { return $true }
    if ($p[0] -eq 192 -and $p[1] -eq 168) { return $true }

    return $false
}

function Test-IpInCidr {
    param([string] $Ip, [string] $Cidr)

    $bits = 32
    if ($Cidr.Contains('/')) {
        $parts = $Cidr.Split('/')
        $Cidr = $parts[0]
        if (-not [int]::TryParse($parts[1], [ref] $bits)) { return $false }
        if ($bits -lt 0 -or $bits -gt 32) { return $false }
    }

    $ipVal = ConvertTo-UInt32 $Ip
    $netVal = ConvertTo-UInt32 $Cidr
    if ($null -eq $ipVal -or $null -eq $netVal) { return $false }

    $mask = 0
    if ($bits -eq 0) {
        $mask = [int64] 0
    } else {
        $mask = (([int64] 4294967295 -shl (32 - $bits)) -band 4294967295)
    }

    return ((([int64] $ipVal -band $mask)) -eq (([int64] $netVal -band $mask)))
}

function Import-CidrList {
    param([string[]] $Raw)
    $out = New-Object System.Collections.Generic.List[string]
    foreach ($entry in $Raw) {
        if ($null -eq $entry) { continue }
        foreach ($item in ($entry -split '[,;\s]+')) {
            $t = $item.Trim()
            if ($t) { $out.Add($t) }
        }
    }
    return $out.ToArray()
}

# ---------------- Admin check ----------------
$identity = [Security.Principal.WindowsIdentity]::GetCurrent()
if (-not ($identity.Groups -match 'S-1-5-32-544')) {
    Write-Host '[ERROR] Run PowerShell as Administrator!' -ForegroundColor Red
    exit 1
}

$logFilePath = Resolve-LogPath $LogFilePath
if (-not $logFilePath) {
    Exit-WithError 'Cannot locate nginx access.log. Pass -LogFilePath explicitly.'
}
if ($DryRun) {
    Write-Host "[DRY RUN] log: $logFilePath" -ForegroundColor Cyan
} else {
    Write-Host "log: $logFilePath"
}

# ---------------- Load CDN whitelist ----------------
$cdnRanges = New-Object System.Collections.Generic.List[string]

if (Test-Path -LiteralPath $whiteListFile) {
    $fileRanges = Import-CidrList (Get-Content -LiteralPath $whiteListFile -ErrorAction SilentlyContinue |
        Where-Object { $_ -and -not $_.Trim().StartsWith('#') })
    foreach ($r in $fileRanges) { $cdnRanges.Add($r) }
    Write-Host "CDN whitelist file: $($cdnRanges.Count) range(s)"
}

foreach ($r in (Import-CidrList $cdnIpRanges)) { $cdnRanges.Add($r) }

function Test-CdnIp {
    param([string] $Ip)
    foreach ($r in $script:cdnRanges) {
        if (Test-IpInCidr $Ip $r) { return $true }
    }
    return $false
}

# 白名单自检: 每条网段用自身地址验证一次，避免无效条目导致保护静默失效
$validRanges = New-Object System.Collections.Generic.List[string]
foreach ($r in $cdnRanges) {
    $probe = $r.Split('/')[0]
    if ((Test-ValidIPv4 $probe) -and (Test-IpInCidr $probe $r)) {
        $validRanges.Add($r)
    } else {
        Write-Host "[WARN] Invalid CDN whitelist entry ignored: $r" -ForegroundColor Yellow
        Write-AuditLog "Invalid CDN whitelist entry $r"
    }
}
$script:cdnRanges = $validRanges
Write-Host "CDN whitelist active: $($script:cdnRanges.Count) range(s)"

# ---------------- Parse access.log ----------------
$logLines = Get-Content -LiteralPath $logFilePath -ErrorAction SilentlyContinue -Tail $scanLines
if (-not $logLines -or $logLines.Count -eq 0) {
    Exit-WithError 'access.log is empty or unreadable.'
}

Write-Host "Scanning last $($logLines.Count) lines..."

$logRegex = '^(?<ip>\S+)\s+\S+\s+\S+\s+\[[^\]]+\]\s+"(?<request>(?:[^"\\]|\\.)*)"\s+(?<status>\d{3})\s+(?<bytes>\S+)'

$attackHits = @{}
$successCount = @{}
$toolUaSeen = New-Object System.Collections.Generic.List[string]

foreach ($line in $logLines) {
    $m = [regex]::Match($line, $logRegex)
    if (-not $m.Success) { continue }

    $ip = $m.Groups['ip'].Value
    if (-not (Test-ValidIPv4 $ip)) { continue }

    $status = [int] $m.Groups['status'].Value
    if ($status -ge 200 -and $status -lt 300) {
        if ($successCount.ContainsKey($ip)) { $successCount[$ip]++ } else { $successCount[$ip] = 1 }
    }

    $request = $m.Groups['request'].Value
    $reqMatch = [regex]::Match($request, '^(?<method>[A-Z]+)\s+(?<uri>\S*)')
    if (-not $reqMatch.Success) { continue }

    $uri = $reqMatch.Groups['uri'].Value
    try { $uri = [System.Uri]::UnescapeDataString($uri) } catch { }

    foreach ($pattern in $attackUriRegex) {
        if ([regex]::IsMatch($uri, $pattern, [System.Text.RegularExpressions.RegexOptions]::IgnoreCase)) {
            if ($attackHits.ContainsKey($ip)) { $attackHits[$ip]++ } else { $attackHits[$ip] = 1 }
            break
        }
    }
}

# 工具 UA 仅审计
foreach ($line in $logLines) {
    foreach ($pattern in $toolUaRegex) {
        if ($line -match $pattern) {
            $ipMatch = [regex]::Match($line, $logRegex)
            if ($ipMatch.Success) {
                $toolUaSeen.Add("$($ipMatch.Groups['ip'].Value)  <- $pattern")
            }
            break
        }
    }
}

Write-Host 'Scan finished.'
Write-Host '------------------------------------'
Write-Host "Attack-pattern hits: $($attackHits.Count) IP(s)"

if ($toolUaSeen.Count -gt 0) {
    $unique = $toolUaSeen | Sort-Object -Unique
    Write-Host "Tool UA seen (NOT blocked): $($unique.Count)"
    foreach ($u in ($unique | Select-Object -First 10)) { Write-Host "  $u" -ForegroundColor DarkGray }
}

Write-Host '------------------------------------'

# ---------------- Cleanup old rules ----------------
function Remove-ExpiredRules {
    $removed = 0
    $cutoff = (Get-Date).AddDays(-$ruleMaxAgeDays)
    $stampPattern = '^{0}_(?<ip>\d{{1,3}}(\.\d{{1,3}}){{3}})_(?<ts>\d{{14}})$' -f [regex]::Escape($rulePrefix)

    $rules = Get-NetFirewallRule -DisplayName "$rulePrefix*" -ErrorAction SilentlyContinue
    if (-not $rules) { return 0 }

    foreach ($rule in $rules) {
        $name = $rule.DisplayName
        $m = [regex]::Match($name, $stampPattern)

        if ($m.Success) {
            $created = [datetime]::MinValue
            if ([datetime]::TryParseExact($m.Groups['ts'].Value, 'yyyyMMddHHmmss', $null, [System.Globalization.DateTimeStyles]::None, [ref] $created)) {
                if ($created -lt $cutoff) {
                    if ($DryRun) {
                        Write-Host "[DRY RUN] Would remove expired rule: $name" -ForegroundColor Cyan
                    } else {
                        Remove-NetFirewallRule -DisplayName $name -ErrorAction SilentlyContinue
                        Write-AuditLog "Removed expired rule $name"
                    }
                    $removed++
                }
            }
        } elseif ($purgeLegacyRules) {
            if ($DryRun) {
                Write-Host "[DRY RUN] Would remove legacy rule: $name" -ForegroundColor Cyan
            } else {
                Remove-NetFirewallRule -DisplayName $name -ErrorAction SilentlyContinue
                Write-AuditLog "Removed legacy rule $name"
            }
            $removed++
        }
    }

    return $removed
}

Write-Host "Cleaning rules older than $ruleMaxAgeDays day(s)..."
$cleaned = Remove-ExpiredRules
Write-Host "Removed: $cleaned"

# ---------------- Block ----------------
Write-Host "Blocking IPs reaching threshold ($blockThreshold)..."
Write-Host '------------------------------------'

$newBlocks = 0
$stamp = Get-Date -Format 'yyyyMMddHHmmss'

foreach ($ip in ($attackHits.Keys | Sort-Object { $attackHits[$_] } -Descending)) {
    $hits = $attackHits[$ip]

    if ($hits -lt $blockThreshold) { continue }

    if (Test-ProtectedIPv4 $ip) {
        Write-AuditLog "Skipped protected IP $ip Hits=$hits"
        continue
    }

    if (Test-CdnIp $ip) {
        Write-AuditLog "Skipped CDN IP $ip Hits=$hits"
        Write-Host "  skip(CDN) $ip hits=$hits" -ForegroundColor DarkGray
        continue
    }

    $ok = 0
    if ($successCount.ContainsKey($ip)) { $ok = $successCount[$ip] }
    if ($ok -ge $cdnSuccessGuard) {
        Write-AuditLog "Skipped high-success IP (likely CDN/origin) $ip Hits=$hits OK2xx=$ok"
        Write-Host "  skip(CDN-like, 2xx=$ok) $ip hits=$hits" -ForegroundColor DarkGray
        continue
    }

    $ruleName = "$rulePrefix`_$ip`_$stamp"

    if (Get-NetFirewallRule -DisplayName $ruleName -ErrorAction SilentlyContinue) {
        Write-Host "  already blocked: $ip" -ForegroundColor Yellow
        continue
    }

    if ($newBlocks -ge $maxBlocksPerRun) {
        Write-AuditLog "Skipped $ip after hitting maxBlocksPerRun. Hits=$hits"
        Write-Host "  skip(limit reached) $ip" -ForegroundColor Yellow
        continue
    }

    if ($DryRun) {
        Write-Host "[DRY RUN] Would block $ip  Hits=$hits  OK2xx=$ok" -ForegroundColor Cyan
        Write-AuditLog "DRY RUN would block $ip Hits=$hits OK2xx=$ok"
        continue
    }

    try {
        New-NetFirewallRule -DisplayName $ruleName `
            -Direction Inbound `
            -Action Block `
            -RemoteAddress $ip `
            -Profile Any `
            -Enabled True | Out-Null
        $newBlocks++
        Write-AuditLog "Blocked $ip Hits=$hits OK2xx=$ok Rule=$ruleName"
        Write-Host "  blocked: $ip  Hits=$hits  OK2xx=$ok" -ForegroundColor Green
    } catch {
        Write-AuditLog "Failed to block $ip Error=$($_.Exception.Message)"
        Write-Host "  failed: $ip  $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "New rules created: $newBlocks"
Write-Host 'Script finished.'