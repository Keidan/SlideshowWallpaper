#!/bin/sh


ADB_HOME=/opt/android/sdk/platform-tools

${ADB_HOME}/adb shell dumpsys activity services SlideshowWallpaperService
