@echo off
cd /d "%~dp0"
java -cp . ScapeMarLauncher
if errorlevel 1 pause
