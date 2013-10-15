import os
from Wrapper.Bwa  import Bwa
from Factory.IFactory import IFactory
class OMSSAAdapter(Bwa):
	def __init__(self,program,tool):
		Bwa.__init__(self,program,tool)
		self.inputFileParameter = '-in'
		self.outputFileParamter = '-out'
	def createOutputFileName(self):
		print "inside createOutputFileName"
		print self.input
		if(len(self.input) != 0):
			path,file_name = os.path.split(self.input)
			output_file_name = file_name.rstrip() +".idXML"
			self.createOutputFilePath(output_file_name)
	def setInput(self):
		try:
			print 'Reading input files'
			with open(workflow_config.input_file, 'r') as f:
 			 	self.input = f.readline()
		except IOError as e:
			sys.stderr.write('No input file found\n')
		return self.input
	
	class Factory(IFactory):
		def create(self): return OMSSAAdapter(self.program,self.tool)
