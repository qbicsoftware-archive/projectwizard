#!/usr/bin/env bash
if [ -z "$MOABHOMEDIR" ]; then
	. /usr/share/Modules/init/bash
	module load devel/python 
fi
python main_app "$@"
