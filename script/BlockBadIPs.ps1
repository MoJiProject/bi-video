# ============================================
# Config
# ============================================
$logFilePath = "C:\Program Files\phpstudy_pro\Extensions\Nginx1.15.11\logs\access.log"
$rulePrefix  = "Block_Dangerous_IP"
$maxEntries  = 2000      # Scan last 2000 lines
$threshold   = 30        # If IP matches > threshold => block
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
    Write-Host "[ERROR] Cannot read Nginx access.log !" -ForegroundColor Red
    exit
}

Write-Host "Scanning last $maxEntries log entries..."

$ipHits = @{}

foreach ($line in $logLines) {

    # Skip white-list API
    foreach ($api in $whiteApi) {
        if ($line -like "*$api*") {
            continue 2   # Skip this log line entirely
        }
    }

    foreach ($pattern in $dangerRegex) {
        if ($line -match $pattern) {

            # Extract IP
            if ($line -match "^(\d{1,3}(\.\d{1,3}){3})") {
                $ip = $matches[1]

                if ($ipHits.ContainsKey($ip)) {
                    $ipHits[$ip]++
                } else {
                    $ipHits[$ip] = 1
                }
            }

            break
        }
    }
}

Write-Host "Suspicious IP detection finished."
Write-Host "------------------------------------"

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
            New-NetFirewallRule -DisplayName $ruleName -Direction Inbound -Action Block -RemoteAddress $ip
            Write-Host "Blocked IP: $ip   Hits: $count"
        } else {
            Write-Host "Already blocked: $ip"
        }
    }
}