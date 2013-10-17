import sys,shlex,subprocess, os
from Utils.workflow_config import workflow_config
from Wrapper.Bwa  import Bwa
from Factory.IFactory import IFactory
class SamtoolsMappingStatistics(Bwa):
	def __init__(self,program,tool):
		Bwa.__init__(self,program,tool)
		self.input = ''	
		self.additionalParameters = ''
		self.outputFileName = ''	
	def createOutputFileName(self):
		print "inside createOutputFileName"
		print self.input
		if(len(self.input) != 0):
			path,file_name = os.path.split(self.input)
			output_file_name = file_name.rstrip() + ".statistics.txt"
			self.createOutputFilePath(path, output_file_name)
	def setInput(self):
		try:
			print 'Reading input files'
			with open(workflow_config.input_file, 'r') as f:
 			 	self.input = f.readline()
		except IOError as e:
			sys.stderr.write('No input file found\n')
		print self.input
		return self.input

	def buildCommand(self):
		print self.outputFileName 
		tmp1 = "samtools idxstats  %s" % (self.input)			
		tmp2 = "awk \'BEGIN {a=0;b=0} {a += $3; b+=$4 } END{print a \" mapped \" b \" unmapped \" a+b \" all \" a/(a+b)*100 \" percent mapped\" }\'" 
		return [shlex.split(tmp1),shlex.split(tmp2)]
	def execute(self):
		cmd_line = self.buildCommand()
		print cmd_line
		f = open(self.outputFileName, 'w')
		p1 = subprocess.Popen(cmd_line[0], stdout=subprocess.PIPE)
		p2 = subprocess.Popen(cmd_line[1], stdin=p1.stdout,stdout=subprocess.PIPE)
		for line in p2.stdout:
			f.write(line)
		#p1.stdout.close()  # Allow p1 to receive a SIGPIPE if p2 exits.
		#print p2.communicate() 
		f.close()
	
	class Factory(IFactory):
		def create(self): return SamtoolsMappingStatistics(self.program,self.tool)
