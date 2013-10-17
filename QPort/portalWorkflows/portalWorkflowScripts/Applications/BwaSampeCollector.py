import sys, os
from Utils.workflow_config import workflow_config
OUTPUT = workflow_config.workflow_output

if len(sys.argv) < 3:
	sys.stderr.write("usage: ./BwaSampeCollector.py <input file> <input file> \n\n")
	sys.exit(1)

sai = []
fastq = []

input_file1 = "%s_1"%(sys.argv[1])
input_file2 = "%s_0"%(sys.argv[1])
input_file3 = "%s_0"%(sys.argv[2])
input_file4 = "%s_1"%(sys.argv[2])
input_files = [input_file1,input_file2,input_file3,input_file4]

print(input_file1)
print(input_file2)

try:
	for input_file in input_files:
		for l in open(input_file, "r"):
			line = l.strip() 
			if (line):
				fileName, fileExtension = os.path.splitext(line)
				if(fileExtension == '.sai'):
					sai.append(line)
				else:
					fastq.append(line)
except IOError as e:
	print "No files to collect: \n" 

sai.sort()
fastq.sort()

print(sai)
print(fastq)
try:
	with open(OUTPUT, 'w') as output:
		for s in sai:
			output.write(s + '\n')
		for f in fastq:
			output.write(f + '\n')
except IOError as e:
	print 'Unable to save collection for bwa sampe'
