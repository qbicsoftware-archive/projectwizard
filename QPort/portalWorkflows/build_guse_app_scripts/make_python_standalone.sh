#!/usr/bin/env bash
if [ $# -ne 2  ]; then
	echo "Illegal number of parameters"
	echo "usage: script.sh standalone_app_name python_folder"

else

	cd $2
	zip -r ../$1.zip *
	echo '#!/usr/bin/env python' | cat - ../$1.zip > ../$1
	chmod +x ../$1
	cd -
fi

