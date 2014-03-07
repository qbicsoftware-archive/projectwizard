#!/usr/bin/env bash
if [ "x$MOABHOMEDIR" != "z" ]; then
  . /usr/share/Modules/init/bash
  module load devel/python 
fi
python main_app "$@"
