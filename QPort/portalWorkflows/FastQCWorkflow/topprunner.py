#!/usr/bin/env python

import os, sys, time

start_time = time.time()

#### environment variables ###

os.environ["PATH"] = "/opt/OpenMS/OpenMS-dev/Release1.10/bin:" + (os.environ["PATH"] if "PATH" in os.environ.keys() else "")
os.environ["LD_LIBRARY_PATH"] = "/opt/OpenMS/OpenMS-dev/contrib/lib:/opt/OpenMS/OpenMS-dev/Release1.10/lib:" + (os.environ["LD_LIBRARY_PATH"] if "LD_LIBRARY_PATH" in os.environ.keys() else "")

### input, output, and ini files ###

input_data_path = "/opt/Labelfree_Workflow_QBiC/Example_Data/tiny/" #tiny / small / orig
input_files = ["velos005614.mzML", "velos005615.mzML", "velos005616.mzML"] #peackpicked raw data, containing MS1 and MS2
ini_dir_path = "/opt/Labelfree_Workflow_QBiC/Workflow/Python_Script/ini/" #contains the parameters for all involved TOPP tools
output_root_dir = "OUTPUT" #relative to input_data_path or absolute

# check that we have at least one parameter
if len(sys.argv) < 2:
	sys.stderr.write("At least one parameter must be provided, namely, the name of the TOPP tool that has to be executued.\n")
	sys.stderr.write("usage: ./topprunner.py <TOPP Tool Name> [command line for TOPP Tool]\n\n")
	sys.exit(1)

# we need to do some magic here... we will look for a file named "input" and if it exists
# we will assume that each of the lines contained in that file refer to an input file
# example: input file contains two lines
##### BEGIN
# /tmp/hugefile.zip
# /tmp/humongousfile.zip
##### END
# the TOPP tool will be executed as follows:
# <TOPP-Tool> -in /tmp/hugefile.zip /tmp/humongousfile.zip <...extra parameters>
extra_input = []
try:
	with open('input', 'r') as input:
		print 'Reading file for additional inputs'
		extra_input = input.read().splitlines()
except IOError as e:
	print 'No input file found'
	
# handle the case in which the user provided an extra "-in" parameter
extra_input_added = False
commands = []
for param in sys.argv[1:]:
	commands.append(param)
	if param == '-in' and extra_input:
		# append all elements of extra input
		commands.extend(extra_input)
		extra_input_added = True
# handle case in which no "-in" was passed
if not extra_input_added and extra_input:
	commands.append("-in")
	commands.extend(extra_input)
		
# add the ini file
commands.append("-ini")
commands.append(ini_dir_path + sys.argv[1] + ".ini")
	
# for the time being, all tools use the same ini file located at $(ini_dir_path + tool_name + '.ini')
cmd_line = ' '.join(commands)
print "Executing [" + cmd_line + "]"

exit_code = os.system(cmd_line)
if exit_code != 0:
	sys.stderr.write(cmd_line + " exited with status: " + str(exit_code) + "\n")
else:
	print(cmd_line + " executed successfully")