#!/usr/bin/env python

import sys, os
from Utils.workflow_config import workflow_config
tmpFolder = os.environ["TMP"] 
OUTPUT =  'PORT1'

if len(sys.argv) < 2:
	sys.stderr.write("usage: ./Initiator.py <input file> \n\n")
	sys.exit(1)
working_directory = '_'
print(working_directory)
for line in open(sys.argv[1], "r"):
	if (line.strip()):
		splittedLine = line.split('=')
		if(splittedLine[0].strip() == 'WORKFLOW'):
			workflow_name=splittedLine[1]
			working_directory="%s/%s_" % (tmpFolder.strip(),workflow_name.strip())
			for i in range(100000):
				tempWorkingDirectory = working_directory + str(i)
				if(not os.path.isdir(tempWorkingDirectory)):
					working_directory = tempWorkingDirectory
					break

print(working_directory)

if(working_directory[-1] == '_'):
	print('did not work')
	sys.exit(-1)
try:
	os.mkdir(working_directory)
	os.mkdir(working_directory+'/results')
except OSError as e:
	print 'How was that even possible'

try:
	with open(OUTPUT, 'w') as output:
			output.write(working_directory)
	with open(sys.argv[1],'a') as parameters:
		parameters.write("%s = %s" %(workflow_config.working_directory, working_directory))
except IOError as e:
	print 'Unable to save working directory path'
