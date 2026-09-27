@echo off
cd /d "%~dp0"
java -cp . OpenRuneLauncher
if errorlevel 1 pause
