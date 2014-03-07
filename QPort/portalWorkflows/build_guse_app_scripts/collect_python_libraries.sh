#!/usr/bin/env bash
if [ $# -ne 1  ]; then
  echo "Illegal number of parameters"
  echo "usage: script app_folder"

else
  mkdir $1 || { echo "collection failed"; exit 1; }
  cp /usr/local/lib/python2.7/dist-packages/configobj.py $1/ 
  cp /home/wojnar/QBiC/Software/CTDopts-master/CTDopts.py $1/ 
  cp -r /usr/local/lib/python2.7/dist-packages/pytz $1/ 
  cp -r $HOME/QBiC/Software/applicake-read-only/branches/appli2ake/applicake $1/ 
  cp -r $HOME/QBiC/Software/applicake-read-only/branches/appli2ake/appliapps $1/ 
  cp -r $HOME/QBiC/workflows/WorkflowRepository/apps $1/ 
  touch $1/__init__.py
  mv $1/apps/Runner.py $1/__main__.py
fi