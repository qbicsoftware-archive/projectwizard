#!/usr/bin/env bash

if [ $# -ne 2  ]; then
  echo "Illegal number of parameters"
  echo "usage: script app_name python_folder"
  exit -1
fi
./collect_python_libraries.sh $2
./make_python_standalone.sh $1 $2
mv $1 main_app
./make_standalone_guse_conform.sh main_app
rm  $1  
rm $1".zip"
rm -r $2
