from Wrapper.Bwa  import Bwa
from Factory.IFactory import IFactory
from Utils.workflow_config import workflow_config
import os,shlex
class BamIndexer(Bwa):
	def __init__(self,program,tool):
		Bwa.__init__(self,program,tool)
		
	def createOutputFileName(self):
		print "inside createOutputFileName"
		print 'path from input file is %s' % self.input
		if(len(self.input) != 0):
			path,file_name = os.path.split(self.input)
			file_name = file_name.rstrip() + '.bai'
			self.outputFileName = self.createOutputFilePath(path,file_name)
	def setInput(self):
		try:
			print 'Reading input files'
			with open(workflow_config.input_file, 'r') as f:
 			 	self.input = f.readline()
		except IOError as e:
			sys.stderr.write('No input file found\n')
		return self.input
	def buildCommand(self):
		tmp = "%s %s %s" % (self.program,self.input.rstrip(), self.outputFileName)	
		return shlex.split(tmp)
	class Factory(IFactory):
		def create(self): return BamIndexer(self.program,self.tool)
