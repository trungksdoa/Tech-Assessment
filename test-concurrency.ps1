# Nạp thư viện cần thiết cho PowerShell 5.1
Add-Type -AssemblyName System.Net.Http

$url = "http://localhost:8080/api/bookings"
$requests = 500
$ticketCategoryId = 1

Write-Host "--- START CONCURRENCY TEST ---" -ForegroundColor Cyan
Write-Host "Sending $requests concurrent requests..."

# Khởi tạo HttpClient và danh sách Task
$client = New-Object System.Net.Http.HttpClient
$tasks = New-Object System.Collections.Generic.List[System.Threading.Tasks.Task]

for ($i = 1; $i -le $requests; $i++) {
    $idempotencyKey = "test-key-" + [System.Guid]::NewGuid().ToString()
    $body = @{
        userId           = 1
        idempotencyKey   = $idempotencyKey
        voucherCode      = "SUMMER2024"
        bookingItems     = @(@{
                ticketCategoryId = $ticketCategoryId
                quantity         = 1
            })
    } | ConvertTo-Json -Depth 5

    # Sử dụng StringContent để tạo dữ liệu JSON
    $content = New-Object System.Net.Http.StringContent($body, [System.Text.Encoding]::UTF8, "application/json")
    
    # Bắn request và thêm vào danh sách quản lý
    $tasks.Add($client.PostAsync($url, $content))
}

Write-Host "All requests sent. Waiting for responses..."

# Đợi tất cả 500 cái hoàn thành
[System.Threading.Tasks.Task]::WaitAll($tasks.ToArray())

# TỔNG KẾT
$successCount = 0
$failCount = 0

foreach ($task in $tasks) {
    if ($task.Result.IsSuccessStatusCode) {
        $successCount++
    }
    else {
        $failCount++
    }
}

Write-Host "`n==============================" -ForegroundColor Yellow
Write-Host "      FINAL TEST SUMMARY      " -ForegroundColor Yellow
Write-Host "==============================" -ForegroundColor Yellow
Write-Host "Total Requests  : $requests"
Write-Host "SUCCESS (Booked): $successCount" -ForegroundColor Green
Write-Host "FAILED (Sold out): $failCount" -ForegroundColor Red
Write-Host "==============================" -ForegroundColor Yellow

if ($successCount -eq 100) {
    Write-Host "-> Done with 100 tickets" -ForegroundColor Green
}
else {
    Write-Host "-> Fail " -ForegroundColor Magenta
}
