$log = "C:\Program Files\phpstudy_pro\Extensions\Nginx1.15.11\logs\access.log"
Set-Content -Path $log -Value "" -Encoding ascii
$errorLog = "C:\Program Files\phpstudy_pro\Extensions\Nginx1.15.11\logs\error.log"
Set-Content -Path $errorLog -Value "" -Encoding ascii