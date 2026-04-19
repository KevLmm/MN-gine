$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot\..
$cp = (Get-ChildItem -Path "lib" -Filter "*.jar" | ForEach-Object { $_.FullName }) -join ";"
$sources = @(Get-ChildItem -Path "engine" -Recurse -Filter "*.java" | ForEach-Object { $_.FullName })
if ($sources.Count -eq 0) {
    Write-Error "No Java sources under engine\"
    exit 1
}
New-Item -ItemType Directory -Path "bin" -Force | Out-Null
& javac -encoding UTF-8 -sourcepath engine -cp $cp -d bin @sources
exit $LASTEXITCODE
