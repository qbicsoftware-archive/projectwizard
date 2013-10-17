import os
from Wrapper.Bwa import Bwa
from Factory.IFactory import IFactory
class BwaAln(Bwa):
	def __init__(self,program,tool):
		Bwa.__init__(self,program,tool)
		self.hasReferenceGenome = True
		self.outputFileParameter = '-f'

	def createOutputFileName(self):
		print "inside createOutputFileName"
		print self.input
		if(len(self.input) != 0):
			path,file_name = os.path.split(self.input[0])
			output_file_name = file_name.rstrip() +".sai"
			self.createOutputFilePath(path,output_file_name)
	class Factory(IFactory):
		def create(self): return BwaAln(self.program,self.tool)
