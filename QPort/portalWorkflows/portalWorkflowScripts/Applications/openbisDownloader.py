#!/usr/bin/env python
import sys, os
tmpFolder = '' 
OUTPUT = 'workflow.input' 

print 'DSS-Client'

if len(sys.argv) < 2:
	sys.stderr.write("usage: ./openbisDownloader.py <input file> <result folder file>\n\n")
	sys.exit(1)

if len (sys.argv) < 3:
	print 'No result folder information given'
	tmpFolder = os.environ["TMP"] 
else:
	try:
		print sys.argv[2]
		with open(sys.argv[2],'r') as f:
				tmpFolder = f.readline()
	except IOError as e:
		print 'tmp Folder could not be found'


if(not os.path.exists(tmpFolder)):
	print 'tmpFolder %s could not be found' % tmpFolder
	sys.exit(-1)
cmdLine = 'getdataset -o '+ tmpFolder + ' -r ' 
file_count = 0
for line in open(sys.argv[1], "r"):
	print '****************************************************'
	print line
	if (line.strip()): # is not empty i guess
		splittedLine = line.split('=')
		print(splittedLine[0])
		print(splittedLine[1])
		workflowOut = "%s_%s" % (OUTPUT, str(file_count))
		cmd = "%s %s %s" % (cmdLine, workflowOut, splittedLine[1])
		print cmd
		exit_code = os.system(cmd)
		file_count += 1
		if exit_code != 0:
			sys.stderr.write(cmdLine + " exited with status: " + str(exit_code) + "\n")
		else:
			try:
				with open(workflowOut,'r+') as f:
					line = f.readline()
					rawFile = line.split('\t')[1]
					line = rawFile
					f.seek(0)
					f.write(rawFile)
					f.truncate()
					f.close()
			except IOError as e:
				print 'Could not prepare file for next node'

#print("Downloaded " + str(file_number) + " files.")
