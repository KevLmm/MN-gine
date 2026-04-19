param(
    [Parameter(Mandatory = $true, Position = 0)]
    [string] $MainClass
)
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot\..
$cp = (Get-ChildItem -Path "lib" -Filter "*.jar" | ForEach-Object { $_.FullName }) -join ";"
& java -cp "bin;$cp" $MainClass @args
exit $LASTEXITCODE
