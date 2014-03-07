#!/usr/bin/env bash
if [ $# -ne 1  ]; then
	echo "Illegal number of parameters"
	echo "usage: script.sh standalone_app_name "

else
	dotsh=".sh";
	executeScriptName=$1$dotsh
	cp executeScript.sh $executeScriptName
	tar -zcvf $executeScriptName".app.tgz" $executeScriptName $1
fi
