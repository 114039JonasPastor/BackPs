@echo off
echo ============================================
echo   TEST EMAIL SERVICE - TU OFICIO
echo ============================================
echo.

REM Set environment variables for local testing
set EMAIL_USERNAME=tuoficiopracticasupervisada@gmail.com
set EMAIL_PASSWORD=twzf lugx xoyd aswk
set BACKEND_URL=http://localhost:8081
set FRONTEND_URL=http://localhost:4200

echo Testing email configuration...
echo.
echo EMAIL_USERNAME: %EMAIL_USERNAME%
echo EMAIL_PASSWORD: ****
echo BACKEND_URL: %BACKEND_URL%
echo FRONTEND_URL: %FRONTEND_URL%
echo.

REM Test SMTP connection with curl
echo Testing SMTP connection to smtp.gmail.com:587...
powershell -Command "Test-NetConnection -ComputerName smtp.gmail.com -Port 587"
echo.

echo ============================================
echo   To test email sending:
echo   1. Start the backend application
echo   2. Register a new user
echo   3. Check backend logs for email status
echo   4. Check email inbox
echo ============================================
echo.
pause
