@echo off
REM ============================================================
REM  build_and_run.bat — Windows version
REM  Compiles and runs the Traffic Signal Optimization System
REM ============================================================

set SRC_DIR=src
set BIN_DIR=bin
set MAIN_CLASS=main.Main

echo === Building Real-Time Traffic Signal Optimization System ===

if not exist "%BIN_DIR%" mkdir "%BIN_DIR%"

echo Compiling all Java files...
for /r "%SRC_DIR%" %%f in (*.java) do (
    javac -d "%BIN_DIR%" -sourcepath "%SRC_DIR%" "%%f"
)

echo Compilation done. Running application...
java -cp "%BIN_DIR%" %MAIN_CLASS%

pause