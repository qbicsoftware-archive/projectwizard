import sys,shlex,subprocess, os
from Utils.workflow_config import workflow_config
from Wrapper.Bwa  import Bwa
from Factory.IFactory import IFactory
class SamToBam(Bwa):
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
			output_file_name = file_name.rstrip() + ".sorted"
			self.createOutputFilePath(output_file_name)
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
		tmp1 = "samtools view %s %s %s" % ( self.parameters, self.additionalParameters,self.input)			
		tmp2 = "samtools sort - %s" % (self.outputFileName)
		return [shlex.split(tmp1),shlex.split(tmp2)]
	def execute(self):
		cmd_line = self.buildCommand()
		print cmd_line
		p1 = subprocess.Popen(cmd_line[0], stdout=subprocess.PIPE)
		p2 = subprocess.Popen(cmd_line[1], stdin=p1.stdout)#,stdout=subprocess.PIPE)
		#p1.stdout.close()  # Allow p1 to receive a SIGPIPE if p2 exits.
		#print p2.communicate() 
				
	
	class Factory(IFactory):
		def create(self): return SamToBam(self.program,self.tool)
