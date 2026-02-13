# Script to verify the chapter fetch race condition fix
# This script monitors logcat for race condition indicators

param(
    [string]$DeviceSerial = "RZ8M71AYVZB",
    [int]$DurationSeconds = 60
)

Write-Host "=== Chapter Fetch Race Condition Verification ===" -ForegroundColor Cyan
Write-Host "Device: $DeviceSerial" -ForegroundColor Yellow
Write-Host "Duration: $DurationSeconds seconds" -ForegroundColor Yellow
Write-Host ""

Write-Host "Instructions:" -ForegroundColor Green
Write-Host "1. Open IReader app on device" -ForegroundColor White
Write-Host "2. Navigate to a book with chapters that need fetching" -ForegroundColor White
Write-Host "3. Quickly navigate between chapters to trigger concurrent fetches" -ForegroundColor White
Write-Host "4. This script will monitor for race condition indicators" -ForegroundColor White
Write-Host ""

Write-Host "Monitoring logcat for $DurationSeconds seconds..." -ForegroundColor Cyan
Write-Host "Press Ctrl+C to stop early" -ForegroundColor Yellow
Write-Host ""

# Clear logcat
adb -s $DeviceSerial logcat -c

# Start monitoring
$startTime = Get-Date
$endTime = $startTime.AddSeconds($DurationSeconds)

$fetchCount = 0
$deduplicationCount = 0
$saveCount = 0
$readBackSuccessCount = 0
$readBackFailCount = 0
$errorCount = 0

Write-Host "Timestamp`t`tEvent" -ForegroundColor Cyan
Write-Host "=========`t`t=====" -ForegroundColor Cyan

while ((Get-Date) -lt $endTime) {
    $logs = adb -s $DeviceSerial logcat -d -s "FetchAndSaveChapterContent:D" "FetchAndSaveChapterContent:W" "FetchAndSaveChapterContent:E" | Select-String -Pattern "FetchAndSaveChapterContent"
    
    foreach ($log in $logs) {
        $timestamp = Get-Date -Format "HH:mm:ss"
        
        if ($log -match "Saving chapter") {
            $fetchCount++
            $saveCount++
            Write-Host "$timestamp`t`t[FETCH] Saving chapter" -ForegroundColor Green
        }
        elseif ($log -match "Deduplicating fetch") {
            $deduplicationCount++
            Write-Host "$timestamp`t`t[DEDUP] Duplicate fetch prevented" -ForegroundColor Yellow
        }
        elseif ($log -match "Read back chapter.*found=true") {
            $readBackSuccessCount++
            Write-Host "$timestamp`t`t[SUCCESS] Chapter read back successfully" -ForegroundColor Green
        }
        elseif ($log -match "Read back chapter.*found=false") {
            $readBackFailCount++
            Write-Host "$timestamp`t`t[FAIL] Chapter read back failed!" -ForegroundColor Red
        }
        elseif ($log -match "Could not read back chapter") {
            $readBackFailCount++
            Write-Host "$timestamp`t`t[WARN] Using fallback chapter" -ForegroundColor Yellow
        }
        elseif ($log -match "Error saving chapter") {
            $errorCount++
            Write-Host "$timestamp`t`t[ERROR] Error saving chapter" -ForegroundColor Red
        }
    }
    
    Start-Sleep -Milliseconds 500
}

Write-Host ""
Write-Host "=== Verification Results ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "Total Fetches: $fetchCount" -ForegroundColor White
Write-Host "Deduplications: $deduplicationCount" -ForegroundColor Yellow
Write-Host "Successful Saves: $saveCount" -ForegroundColor Green
Write-Host "Successful Read-backs: $readBackSuccessCount" -ForegroundColor Green
Write-Host "Failed Read-backs: $readBackFailCount" -ForegroundColor $(if ($readBackFailCount -gt 0) { "Red" } else { "Green" })
Write-Host "Errors: $errorCount" -ForegroundColor $(if ($errorCount -gt 0) { "Red" } else { "Green" })
Write-Host ""

# Analysis
Write-Host "=== Analysis ===" -ForegroundColor Cyan
Write-Host ""

if ($deduplicationCount -gt 0) {
    Write-Host "[PASS] Deduplication is working - prevented $deduplicationCount duplicate fetches" -ForegroundColor Green
} else {
    Write-Host "[INFO] No duplicate fetches detected (may need more testing)" -ForegroundColor Yellow
}

if ($readBackFailCount -eq 0 -and $saveCount -gt 0) {
    Write-Host "[PASS] All saved chapters were successfully read back" -ForegroundColor Green
} elseif ($readBackFailCount -gt 0) {
    Write-Host "[FAIL] Some chapters failed to read back - race condition may still exist!" -ForegroundColor Red
}

if ($errorCount -eq 0) {
    Write-Host "[PASS] No errors during fetch operations" -ForegroundColor Green
} else {
    Write-Host "[FAIL] $errorCount errors occurred during fetch operations" -ForegroundColor Red
}

$deduplicationRate = if ($fetchCount -gt 0) { [math]::Round(($deduplicationCount / ($fetchCount + $deduplicationCount)) * 100, 2) } else { 0 }
Write-Host ""
Write-Host "Deduplication Rate: $deduplicationRate%" -ForegroundColor $(if ($deduplicationRate -gt 0) { "Green" } else { "Yellow" })

Write-Host ""
Write-Host "=== Recommendations ===" -ForegroundColor Cyan
Write-Host ""

if ($fetchCount -eq 0) {
    Write-Host "- No fetch operations detected. Try navigating between chapters in the app." -ForegroundColor Yellow
}

if ($deduplicationCount -eq 0 -and $fetchCount -gt 1) {
    Write-Host "- Try navigating more quickly between chapters to trigger concurrent fetches." -ForegroundColor Yellow
}

if ($readBackFailCount -gt 0) {
    Write-Host "- CRITICAL: Race condition may still exist. Check database operations." -ForegroundColor Red
}

Write-Host ""
Write-Host "Verification complete!" -ForegroundColor Cyan
