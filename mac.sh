#!/bin/bash
BUHEHE=1

if [ "X$1" == "Xb" ] || [ "X$2" == "Xb" ]; then
	BUHEHE=0
	ant compile

	if [ "$?" -eq "0" ]; then
		ant jar
			
		if [ "$?" -eq "0" ]; then
			BUHEHE=1
		fi
	fi
fi

if [ "X$BUHEHE" == "X1" ] && [ "X$1" != "Xb" ]; then
	open -a /Applications/Google\ Chrome.app http://chrome.angrybirds.com/
	find ./img -name "*.png" -exec rm {} \;
	sleep 10
	java -jar ABServer.jar &
	sleep 5
	java -jar ABSoftware.jar 127.0.0.1 # "$1"
	PID=$(ps -A | grep '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome' | head -1 | sed 's/^[ \t]*//' | cut -d" " -f 1,1)
	kill -9 "$PID"
	PID=$(ps -A | grep 'ABServer.jar' | head -1 | sed 's/^[ \t]*//' | cut -d" " -f 1,1)
	kill -9 "$PID"
fi
