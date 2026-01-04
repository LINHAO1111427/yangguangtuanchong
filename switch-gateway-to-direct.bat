@echo off
echo ==================================================
echo Switch Gateway Routes to Direct Connection Mode
echo ==================================================

cd /d "%~dp0"

set "file=yudao-gateway\src\main\resources\application.yaml"

echo.
echo Processing Gateway routes...

REM Replace grayLb:// with http://localhost: for each service
powershell -Command "(Get-Content '%file%') -replace 'uri: grayLb://system-server', 'uri: http://localhost:48081' | Set-Content '%file%'"
powershell -Command "(Get-Content '%file%') -replace 'uri: grayLb://infra-server', 'uri: http://localhost:48082' | Set-Content '%file%'"
powershell -Command "(Get-Content '%file%') -replace 'uri: grayLb://member-server', 'uri: http://localhost:48085' | Set-Content '%file%'"
powershell -Command "(Get-Content '%file%') -replace 'uri: grayLb://message-server', 'uri: http://localhost:48084' | Set-Content '%file%'"
powershell -Command "(Get-Content '%file%') -replace 'uri: grayLb://pay-server', 'uri: http://localhost:48087' | Set-Content '%file%'"
powershell -Command "(Get-Content '%file%') -replace 'uri: grayLb://bpm-server', 'uri: http://localhost:48086' | Set-Content '%file%'"

echo.
echo ==================================================
echo DONE! Gateway routes switched to direct connection
echo All grayLb:// replaced with http://localhost:PORT
echo ==================================================
pause
