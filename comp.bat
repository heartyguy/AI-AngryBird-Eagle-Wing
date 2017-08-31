@echo off
if "%3"=="" (
	goto :aibirds
)
if "%3"=="b" (
	goto :label1
)
if "%3"=="r" (
	cd img
	del /s "*.png"
	cd ..	
	goto :aibirds
) 
if "%3"=="rb" (
	goto :label2
)else (
	goto :aibirds
)
:label2
cd img
del /s "*.png"
cd ..
:label1
call ant compile 
IF %ERRORLEVEL% NEQ 0 (
  goto :eof
)
call ant jar
IF %ERRORLEVEL% NEQ 0 (
  goto :eof
)
:aibirds
start  java -jar ABServer.jar
java -jar ABSoftware.jar 127.0.0.1 %1 %2