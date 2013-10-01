#!/usr/bin/env python

import os, sys, time

start_time = time.time()

#### environment variables ###
fastqc = "fastqc --noextract "#"/share/opt/QBiC/Software/FastQC/fastqc"
transitionList = 'workflow.input'
tmpFolder = os.environ["TMP"]
workflow_waste = "workflow.waste"
workflow_output = "workflow.output"
result_folder = tmpFolder#"/abi-projects/QBiC/GenomicsMG/"
output_paramet = "-o "
space = " "
#Nothing to do here at the moment

# we need to do some magic here... we will look for a file named "workflow.input" and if it exists
# we will assume that each of the lines contained in that file refer to an input file
# example: input file contains two lines
##### BEGIN
# 390 /tmp/hugefile.zip
# 203 /tmp/humongousfile.zip
##### END
# fastqc will be executed as follows:
# 


#process Input File

input = []
try:
	print 'Reading input files'
	for line in open(transitionList, 'r'):
		splittedLine = line.split('\t')
		print(splittedLine)
		input.append(splittedLine[1].rstrip())
except IOError as e:
	print 'No input file found'

#startWorkflow
mkdir = 'mkdir ' + result_folder 
if(not os.path.exists(result_folder)):
	print(mkdir)
	exit_code = os.system(mkdir)
	if exit_code != 0:
		sys.stderr.write("Exited with status: " + str(exit_code) + "\n Unable to create result directory for fastqc\n")

commands = []
commands.append(fastqc)
commands.append(' ')
commands.extend(input)
commands.append(' -o ')
commands.append(result_folder)
cmd_line = ' '.join(commands)
print(cmd_line)
exit_code = os.system(cmd_line)
if exit_code != 0:
	sys.stderr.write(" exited with status "+ str(exit_code) +"\n")
else:
	print(" executed successfully")
	try:
		with open(workflow_waste, 'w') as output:
			for inpu in input:
				path, file = os.path.split(inpu)
				output.write(path)
				output.write("\n")
	except IOError as e:
		print 'Unable to save garbage collection information'
	try:
		with open(workflow_output,'w') as output:
			for inpu in input:
				path, file = os.path.split(inpu)
				fixes = os.path.splitext(file)
				output.write(result_folder + '/' + fixes[0] + '_fastqc.zip')
				output.write("\n")
	except IOError as e:
		print 'Unable to write output information'
#writeOutputFile



#def runFastQC2()



def runFastQC(result_dir,work_dir = os.getcwd()):
	filelist = os.listdir(work_dir)
	for file in filelist:
		if(file.count('fastq') > 0):
			com_mkdir = []
			com_mkdir.append(result_dir)
			com_mkdir.append(file)
			com_mkdir.append("_result")
			new_dir = ''.join(com_mkdir) 
			mkdir =  "mkdir "+ new_dir
			print(mkdir)
			exit_code = os.system(mkdir)
			if exit_code != 0:
				sys.stderr.write("Exited with status: " + str(exit_code) + "\n" + "Have you already analyzed the data?\n")
			commands = []
			commands.append(fastqc)
 			commands.append(space)
			commands.append(work_dir)
			commands.append("/")
			commands.append(file)
			commands.append(space)
			commands.append("-o ")
			commands.append(new_dir)
			cmd_line = ''.join(commands)
			print(cmd_line)
			exit_code = os.system(cmd_line)
			if exit_code != 0:
				sys.stderr.write(cmd_line + " exited with status: " + str(exit_code) + "\n")
			else:
				print(cmd_line + " executed successfully")

#runFastQC(result_folder,data_folder)
