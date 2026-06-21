param(
    [ValidateSet("Startup", "ResetFrames", "DumpFrames")]
    [string]$Action,
    [int]$Runs = 5,
    [string]$OutputPath = "",
    [string]$PackageName = "com.app.lokacara",
    [string]$ActivityName = ".MainActivity",
    [string]$AdbPath = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path -LiteralPath $AdbPath)) {
    throw "adb tidak ditemukan di $AdbPath"
}

$devices = & $AdbPath devices
if (($devices | Select-String -Pattern "\tdevice$").Count -ne 1) {
    throw "Hubungkan tepat satu emulator/perangkat sebelum menjalankan pengukuran."
}

function Write-Result([string[]]$Lines) {
    if ([string]::IsNullOrWhiteSpace($OutputPath)) {
        $Lines
        return
    }

    $directory = Split-Path -Parent $OutputPath
    if ($directory) {
        New-Item -ItemType Directory -Force -Path $directory | Out-Null
    }
    $Lines | Set-Content -LiteralPath $OutputPath -Encoding utf8
    Write-Host "Hasil disimpan ke $OutputPath"
}

switch ($Action) {
    "Startup" {
        $results = @("Startup benchmark: $PackageName/$ActivityName", "Runs: $Runs", "")
        for ($run = 1; $run -le $Runs; $run++) {
            & $AdbPath shell am force-stop $PackageName | Out-Null
            $launch = & $AdbPath shell am start -W -n "$PackageName/$ActivityName"
            $results += "Run $run"
            $results += $launch | Where-Object { $_ -match "^(Status|LaunchState|Activity|TotalTime|WaitTime|Complete):" }
            $results += ""
        }
        Write-Result $results
    }
    "ResetFrames" {
        & $AdbPath shell dumpsys gfxinfo $PackageName reset | Out-Null
        Write-Host "Frame stats direset untuk $PackageName."
    }
    "DumpFrames" {
        $dump = & $AdbPath shell dumpsys gfxinfo $PackageName
        $summary = $dump | Select-String -Pattern "Total frames rendered|Janky frames|Janky frames \(legacy\)|50th percentile|90th percentile|95th percentile|99th percentile|Number Missed Vsync|Number High input latency|Number Slow UI thread|Number Slow bitmap uploads|Number Slow issue draw commands|HISTOGRAM"
        Write-Result @(
            "Frame benchmark: $PackageName",
            "Captured: $(Get-Date -Format o)",
            "",
            $summary.Line
        )
    }
}
