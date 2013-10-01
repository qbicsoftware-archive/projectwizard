#!/usr/bin/env python

#removes files and folders which are no longer needed
import sys, os
saveCmd = 'mv '
deleteCmd = 'rm -rf '
prefix = 'QBIC.-'
dropbox = os.environ["DROPBOX"]
if len(sys.argv) != 2!= 2 :
	sys.stderr.write("usage: ./WorkflowReaper.py <garbage input file> <save input file> \n\n")
	sys.exit(1)

	
file_number = 0
for line in open(sys.argv[1], "r"):
	print(line)
	cmd = ''.join((deleteCmd,line)) #cmdLine + splittedLine
	print cmd
	exit_code = os.system(cmd)
	if exit_code != 0:
		sys.stderr.write(cmd + " exited with status: " + str(exit_code) + "\n")
	else:
		print "Clean up successful"
	file_number += 1

for line in open(sys.argv[2], 'r'):
	print(line)
	line_strip = line.strip()
	cmd = ''.join((saveCmd,line_strip))
	path, file = os.path.split(line_strip)
	fileName = ''.join((dropbox,'/',prefix,file))
	cmd = ''.join((cmd,' ',fileName))
	print cmd
	exit_code = os.system(cmd)
	if exit_code != 0:
		sys.stderr.write(cmd + " exited with status: " + str(exit_code) + "\n")
	else:
		print "successfully transfered to dropbox" 
