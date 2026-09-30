# ============================================
# Config
# ============================================
$logFilePath = "C:\Program Files\phpstudy_pro\Extensions\Nginx1.15.11\logs\access.log"
$rulePrefix  = "Block_Dangerous_IP"
$maxEntries  = 2000      # Scan last 2000 lines
$threshold   = 30        # If IP matches > threshold => block
$maxBlocksPerRun = 20    # Safety limit: max new firewall rules per run
$dryRun      = $false    # Set to $true to only print what would be blocked
$auditLogPath = Join-Path $PSScriptRoot "blocked_ips.log"
$dangerRegex = @(
    "select.+from",
    "union.+select",
    "information_schema",
    "\.\./\.\./",
    "/etc/passwd",
    "or 1=1",
    "base\.shtml",
    "start\.shtml",
    "wget ",
    "curl ",
    "python",
    "script",
    "\.env",
    "select\(",
    "insert ",
    "update ",
    "delete ",
    " drop ",
    "alert\(",
    "document\.cookie"
)

# White-list API (won’t be blocked)
$whiteApi = @(
    "/api/user/selectFollow",
    "/api/user/selectUserInfoById"
)

# IPs/ranges that must never be blocked by this script.
$protectedIps = @(
    "127.0.0.1",
    "0.0.0.0",
    "255.255.255.255"
)

# ============================================
# Helpers
# ============================================
function Write-AuditLog {
    param([string]$Message)

    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Add-Content -Path $auditLogPath -Value "[$timestamp] $Message" -Encoding UTF8 -ErrorAction SilentlyContinue
}

function Test-ValidIPv4 {
    param([string]$Ip)

    if ($Ip -notmatch "^\d{1,3}(\.\d{1,3}){3}$") {
        return $false
    }

    foreach ($part in $Ip.Split(".")) {
        $number = 0
        if (-not [int]::TryParse($part, [ref]$number)) {
            return $false
        }
        if ($number -lt 0 -or $number -gt 255) {
            return $false
        }
    }

    return $true
}

function Test-ProtectedIPv4 {
    param([string]$Ip)

    if ($protectedIps -contains $Ip) {
        return $true
    }

    $parts = $Ip.Split(".") | ForEach-Object { [int]$_ }

    if ($parts[0] -eq 10) { return $true }
    if ($parts[0] -eq 127) { return $true }
    if ($parts[0] -eq 169 -and $parts[1] -eq 254) { return $true }
    if ($parts[0] -eq 172 -and $parts[1] -ge 16 -and $parts[1] -le 31) { return $true }
    if ($parts[0] -eq 192 -and $parts[1] -eq 168) { return $true }

    return $false
}

function Get-DecodedLogLine {
    param([string]$Line)

    try {
        return [System.Uri]::UnescapeDataString($Line)
    } catch {
        return $Line
    }
}

# ============================================
# Ensure admin
# ============================================
$adminCheck = [Security.Principal.WindowsIdentity]::GetCurrent()
if (-not ($adminCheck.Groups -match "S-1-5-32-544")) {
    Write-Host "[ERROR] Run PowerShell as Administrator!" -ForegroundColor Red
    exit
}

# ============================================
# Read last N lines
# ============================================
$logLines = Get-Content -Path $logFilePath -ErrorAction SilentlyContinue -Tail $maxEntries

if ($logLines.Count -eq 0) {
    Write-Host "[ERROR] Cannot read Nginx access.log or the log is empty!" -ForegroundColor Red
    exit
}

Write-Host "Scanning last $maxEntries log entries..."

$ipHits = @{}

# Add a label to the outer loop
:LineLoop foreach ($line in $logLines) {
    $decodedLine = Get-DecodedLogLine $line

    # Skip white-list API
    foreach ($api in $whiteApi) {
        if ($line -like "*$api*" -or $decodedLine -like "*$api*") {
            # Use the label to continue the outer loop
            continue LineLoop
        }
    }

    foreach ($pattern in $dangerRegex) {
        if ([regex]::IsMatch($decodedLine, $pattern, [System.Text.RegularExpressions.RegexOptions]::IgnoreCase)) {

            # Extract IP
            if ($line -match "^(\d{1,3}(\.\d{1,3}){3})") {
                $ip = $matches[1]

                if (-not (Test-ValidIPv4 $ip)) {
                    Write-AuditLog "Skipped invalid IP from log: $ip"
                    break
                }

                if (Test-ProtectedIPv4 $ip) {
                    Write-AuditLog "Skipped protected IP: $ip"
                    break
                }

                if ($ipHits.ContainsKey($ip)) {
                    $ipHits[$ip]++
                } else {
                    $ipHits[$ip] = 1
                }
                # Break after the first pattern match for a given line
                break
            }
        }
    }
}

Write-Host "Suspicious IP detection finished."
Write-Host "------------------------------------"

if ($ipHits.Count -eq 0) {
    Write-Host "No suspicious IPs found."
} else {
    Write-Host "IP Hit Counts:"
    $ipHits.GetEnumerator() | ForEach-Object {
        Write-Host "  IP: $($_.Name), Hits: $($_.Value)"
    }
}

Write-Host "------------------------------------"
Write-Host "Blocking IPs that exceed threshold ($threshold)..."

$newBlockCount = 0

# ============================================
# Block
# ============================================
foreach ($ip in $ipHits.Keys) {
    $count = $ipHits[$ip]

    if ($count -ge $threshold) {
        $ruleName = "$rulePrefix-$ip"

        # Check if already exists
        $existing = Get-NetFirewallRule -DisplayName $ruleName -ErrorAction SilentlyContinue

        if (-not $existing) {
            if ($newBlockCount -ge $maxBlocksPerRun) {
                Write-Host "Skip blocking $ip because maxBlocksPerRun ($maxBlocksPerRun) has been reached." -ForegroundColor Yellow
                Write-AuditLog "Skipped $ip after hitting maxBlocksPerRun. Hits=$count"
                continue
            }

            if ($dryRun) {
                Write-Host "[DRY RUN] Would block IP: $ip   Hits: $count" -ForegroundColor Cyan
                Write-AuditLog "DRY RUN would block $ip. Hits=$count"
                continue
            }

            try {
                New-NetFirewallRule -DisplayName $ruleName -Direction Inbound -Action Block -RemoteAddress $ip -Profile Any -Enabled True | Out-Null
                $newBlockCount++
                Write-Host "Blocked IP: $ip   Hits: $count" -ForegroundColor Green
                Write-AuditLog "Blocked $ip. Hits=$count Rule=$ruleName"
            } catch {
                Write-Host "Failed to block IP: $ip   Error: $($_.Exception.Message)" -ForegroundColor Red
                Write-AuditLog "Failed to block $ip. Hits=$count Error=$($_.Exception.Message)"
            }
        } else {
            Write-Host "Already blocked: $ip" -ForegroundColor Yellow
            Write-AuditLog "Already blocked $ip. Hits=$count Rule=$ruleName"
        }
    }
}

Write-Host "Script finished."