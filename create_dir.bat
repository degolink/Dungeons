@echo off
REM Create the integration test directory
mkdir "c:\Cosas\Rol\rol\src\test\java\com\dungeons_and_dragons\rol\integration"

REM Verify it was created
if exist "c:\Cosas\Rol\rol\src\test\java\com\dungeons_and_dragons\rol\integration" (
    echo Directory created successfully!
    dir /B "c:\Cosas\Rol\rol\src\test\java\com\dungeons_and_dragons\rol\"
) else (
    echo Failed to create directory
)
