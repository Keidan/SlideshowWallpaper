
@echo off

set ADB_HOME=D:\android\sdk\platform-tools

%ADB_HOME%\adb.exe shell dumpsys activity services SlideshowWallpaperService
pause