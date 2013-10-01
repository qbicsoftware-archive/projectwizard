#!/usr/bin/env python


import sys, os
cmdLine = 'mv -r '
if len(sys.argv) < 3:
	sys.stderr.write("usage: ./openbisUploader.py <input file> \n\n")
	sys.exit(1)

	
file_number = 0
for line in open(sys.argv[1], "r"):
	splittedLine = line.split('=')
	print(line)
	print(splittedLine[0])
	print(splittedLine[1])
	cmdLine = ''.join((cmdLine,splittedLine[1])) #cmdLine + splittedLine
	print cmdLine
	exit_code = os.system(cmdLine)
##	if exit_code != 0:
##		sys.stderr.write(cmdLine + " exited with status: " + str(exit_code) + "\n")
##	else:
##		print(cmdLine + " executed successfully")
	file_number += 1
