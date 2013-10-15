from Wrapper.Bwa  import Bwa
from Factory.IFactory import IFactory
from Utils.workflow_config import workflow_config
import os,shlex
class BamIndexer(Bwa):
	def __init__(self,program,tool):
		Bwa.__init__(self,program,tool)
		
	def createOutputFileName(self):
		print "inside createOutputFileName"
		print self.input
		if(len(self.input) != 0):
			self.outputFileName = self.input.rstrip() + ".bai"
	def setInput(self):
		try:
			print 'Reading input files'
			with open(workflow_config.input_file, 'r') as f:
 			 	self.input = f.readline()
		except IOError as e:
			sys.stderr.write('No input file found\n')
		return self.input
	def buildCommand(self):
		tmp = "%s %s %s" % (self.program,self.input, self.outputFileName)	
		return shlex.split(tmp)
	class Factory(IFactory):
		def create(self): return BamIndexer(self.program,self.tool)
