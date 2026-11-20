# Sends the scout clips that get-scout-clips.ps1 made to GitHub, so a Claude session can look at them.
# Each clip is shrunk to 480p first (about 4 MB instead of 70), then all of them and their .json files
# go up on one throwaway branch, scout-clips/<date> (never merged; delete it when the scouting is done).
# Works in a temporary git folder: no checkout of yours is touched. Windows PowerShell 5.1.
#
#   .\send-scout-clips.ps1                                 # Desktop\scout-clips, today's branch
#   .\send-scout-clips.ps1 -In "G:\My Drive\scout-clips"
param(
    [string]$In = (Join-Path ([Environment]::GetFolderPath("Desktop")) "scout-clips"),
    [string]$Branch = ("scout-clips/" + (Get-Date).ToString("yyyy-MM-dd")),
    [string]$Remote = "https://github.com/Mona-Shores-FTC-Robotics/biobuzz.git"
)
$ErrorActionPreference = "Stop"
$ffmpeg = Join-Path $env:LOCALAPPDATA "BIOBUZZ-Replay\tools\ffmpeg.exe"
if (-not (Test-Path $ffmpeg)) { throw "ffmpeg.exe is missing: run the match-replay setup first (tools/advantagescope/replay/README.md, step 1)" }
if (-not (Get-Command git -ErrorAction SilentlyContinue)) { throw "git not found" }
$clips = @(Get-ChildItem -LiteralPath $In -Filter *.mp4 -File | Where-Object { $_.Name -notlike "*.part.mp4" })
if ($clips.Count -eq 0) { throw "no clips in $In" }

$work = Join-Path $env:TEMP ("biobuzz-scout-clips-" + [guid]::NewGuid().ToString("N"))
New-Item -ItemType Directory -Force $work | Out-Null
try {
    & git -C $work init --quiet
    foreach ($c in $clips) {
        $small = Join-Path $work $c.Name
        Write-Host "  ...     $($c.Name)"
        & $ffmpeg -hide_banner -loglevel error -y -i $c.FullName -vf "scale=-2:480" -an -c:v libx264 -preset veryfast -crf 28 -pix_fmt yuv420p $small
        if ($LASTEXITCODE -ne 0) { Write-Host "  FAILED  $($c.Name): ffmpeg could not shrink it" -ForegroundColor Red; continue }
        $json = "$($c.FullName).json"
        if (Test-Path -LiteralPath $json) { Copy-Item -LiteralPath $json (Join-Path $work "$($c.Name).json") }
        Write-Host ("  OK      {0} ({1:N1} MB)" -f $c.Name, ((Get-Item $small).Length / 1MB)) -ForegroundColor Green
    }
    & git -C $work add -A
    & git -C $work -c user.name="scout-clips" -c user.email="scout-clips@users.noreply.github.com" commit --quiet -m "Scout clips, 480p, from scouting/2026/send-scout-clips.ps1 (throwaway, never merge)"
    if ($LASTEXITCODE -ne 0) { throw "could not commit the clips" }
    & git -C $work push --force $Remote "HEAD:refs/heads/$Branch"
    if ($LASTEXITCODE -ne 0) { throw "could not push $Branch" }
    Write-Host "Sent $($clips.Count) clip(s) to GitHub branch $Branch. Tell the Claude session." -ForegroundColor Green
} finally {
    Remove-Item -Recurse -Force -LiteralPath $work -ErrorAction SilentlyContinue
}
