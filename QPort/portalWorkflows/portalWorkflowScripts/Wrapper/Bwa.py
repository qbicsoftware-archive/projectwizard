import sys, subprocess,shlex
from IWrapper import IWrapper
from Utils.workflow_config import workflow_config
class Bwa(IWrapper):
	def __init__(self,program,tool):
		self.additionalParameters =''
		self.parameters = ''
		self.input = []
		self.outputFileName= ''
		self.hasReferenceGenome = False
		self.program = program
		self.tool = tool
		self.outputFileParameter = '-o'
		self.inputFileParameter = ''
		self.resultFolder = ''
		self.working_directory = workflow_config.tmp_folder
	def setInput(self):
		try:
			print 'Reading input files'
			for line in open(workflow_config.input_file, 'r'):
	#			splittedLine = line.split('\t')
	#			print(splittedLine)
				self.input.append(line.rstrip())#splittedLine[1].rstrip())
		except IOError as e:
			sys.stderr.write('No input file found\n')
		return input

	def setAdditionalParameters(self,args):	
		for arg in args:
			self.additionalParameters += arg
			self.additionalParameters += ' '
	def setParameters(self):
		try:
			for line in open(workflow_config.parameter_file,'r'):
				splittedLine = line.split('=');
				print "We are in parameterFile"
				print splittedLine
				#print self.hasReferenceGenome
				#print splittedLine[0] == workflow_config.ref_genome
				#print (self.hasReferenceGenome and splittedLine[0] == workflow_config.ref_genome)
				if(splittedLine[0] == self.tool or (self.hasReferenceGenome and splittedLine[0] == workflow_config.ref_genome)):
					self.parameters += splittedLine[1].rstrip()
					self.parameters += ' '
				if(splittedLine[0] == workflow_config.working_directory):
					self.working_directory = splittedLine[1].rstrip()
		except IOError as e:
			sys.stderr.write('Parameter List not found\n')
	def createOutputFileName(self): pass
	
	def handleInput(self,args):
		#handle file input
		self.setInput()	
		#handle addition Parameters from command line
		self.setAdditionalParameters(args)
		#handle parameter file
		self.setParameters()
		self.createOutputFileName()
		
	def createOutputFilePath(self,path, output_file_name):
		print "inside createOutputFilePath"
		if(self.working_directory.endswith('/')):
			self.outputFileName = self.working_directory + self.resultFolder +  output_file_name
		else:
			self.outputFileName = self.working_directory + '/' + self.resultFolder +  output_file_name
		return self.outputFileName
	def buildCommand(self):
		tmp = "%s %s %s %s %s %s %s" % (self.program, self.parameters, self.additionalParameters,self.inputFileParameter,' '.join(self.input), self.outputFileParameter, self.outputFileName)			
		return shlex.split(tmp)
	def execute(self):
		cmd_line = self.buildCommand()
		print cmd_line
		exit_code = subprocess.call(cmd_line)
		#commands = []
		#commands.append(self.program)
		#commands.append(' ')
		#commands.append(self.parameters)
		#commands.append(self.additionalParameters)
		#commands.extend(self.input)
		#commands.append(' ' + self.outputFileParameter + ' ')
		#commands.append(self.outputFileName)
		#print "commands ***************************************"
		#print commands
		#cmd_line = ' '.join(commands)
		#print "command line \n"
		#print(cmd_line)
		#exit_code = os.system(cmd_line)
		if exit_code != 0:
			sys.stderr.write(" exited with status "+ str(exit_code) +"\n")
		else:
			print "seems to be a success!"
	def writeOutputInformation(self):
		try:	
			with open(workflow_config.workflow_output,'w') as output:
					output.write(''.join([self.outputFileName,'\n']))
		except IOError as e:
			print 'Unable to write output information'
	def saveResults(self, saveresult):
		if(saveresult):
			self.resultFolder = 'results/'
