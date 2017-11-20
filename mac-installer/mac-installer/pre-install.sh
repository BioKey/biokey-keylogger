#!/bin/sh

foundPackage=`/usr/sbin/pkgutil --volume "$3" --pkgs=com.uwo.keylogger.App`

if test -n "$foundPackage"; then
	launchctl load -w /Library/LaunchAgents/com.uwo.keylogger.App.plist 
	nohup /Applications/Keylogger.app/Contents/MacOS/JavaAppLauncher &
fi

exit 0