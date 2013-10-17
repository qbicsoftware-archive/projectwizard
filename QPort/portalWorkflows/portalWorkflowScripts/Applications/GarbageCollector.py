#!/usr/bin/env python

#removes files and folders which are no longer needed
import sys, os,shutil
saveCmd = 'mv '
deleteCmd = 'rm -rf '
prefix = 'QBIC.-'
dropbox = os.environ["DROPBOX"]
resultsFolder = 'results/'
if len(sys.argv) != 2 :
	sys.stderr.write("usage: ./GarbageCollector.py <input file> \n Warning: Do not use this script in a root directory!\n\n")
	sys.exit(1)

#It is assumed, that the file comes directly from the initiator node
path =''
try:
	print 'Reading input files'
	with open(sys.argv[1], 'r') as f:
	 	path = f.readline()
except IOError as e:
	sys.stderr.write('No input file found\n')

if (not os.path.exists(path)):
	sys.stderr.write('working temp director %s does not exist' % (path))
	sys.exit(-1)

#write result to dropbox
resultsFolder = "%s/%s"% (path,resultsFolder)  

for f in os.listdir(resultsFolder):
	try:
		destination = '%s/%s' % (dropbox,f)
		source = '%s/%s' % (resultsFolder,f)
		if(os.path.isdir(source)):
			shutil.copytree(source,destination)
		else:
			shutil.copyfile(source,destination)
	except IOError as e:
		print "something is definitely going wrong"
shutil.rmtree(path)
