import workflow_config

import os


class WorkflowRunnerFactory:
	factories ={}
	def addFactory(self,id, runnerFactory):
		WorkflowRunnerFactory.factories[id] = runnerFactory
	addFactor = staticmethod(addFactory)
	#A template method:
	def createRunner(idList):
		id =''
		for i in idList:
			id += i
			id += ' '
		id = id.strip()
		#iif not WorkflowRunnerFactory.factories.has_key(id):
		#	WorkflowRunnerFactory.factories[id] = \
		#		eval(id + '.Factory()')
		return WorkflowRunnerFactory.factories[id].create()
	createRunner = staticmethod(createRunner) 
	#creates all available Factories, hardcoded for the moment
	def addAllAvailableFactories():
		WorkflowRunnerFactory().addFactory('bwa aln', BwaAln.Factory('bwa aln'))
		WorkflowRunnerFactory().addFactory('bwa sampe', BwaSampe.Factory('bwa sampe'))
	addAllAvailableFactories = staticmethod(addAllAvailableFactories)	
	


class Runner(object):
	def __init__(self,program,tool):
		self.additionalParameters =''
		self.parameters = ''
		self.input = []
		self.outputFileName= ''
		self.hasReferenceGenome = False
		self.program = program
		self.tool = tool
	def inputFile(self):
		try:
			print 'Reading input files'
			for line in open(workflow_config.input_file, 'r'):
				splittedLine = line.split('\t')
				print(splittedLine)
				self.input.append(splittedLine[1].rstrip())
		except IOError as e:
			sys.stderr.write('No input file found\n')
		return input

	def addParam(self,args):	
		for arg in args:
			self.additionalParameters += arg
			self.additionalParameters += ' '
	def parameterFile(self):
		try:
			for line in open(workflow_config.parameter_file,'r'):
				splittedLine = line.split('=');
				print "We are in parameterFile"
				print splittedLine
				print self.hasReferenceGenome
				print splittedLine[0] == workflow_config.ref_genome
				print (self.hasReferenceGenome and splittedLine[0] == workflow_config.ref_genome)
				if(splittedLine[0] == self.tool or (self.hasReferenceGenome and splittedLine[0] == workflow_config.ref_genome)):
					self.parameters += splittedLine[1].rstrip()
					self.parameters += ' '
		except IOError as e:
			sys.stderr.write('Parameter List not found\n')
	def createOutputFileName(self): pass
	
	def handleInput(self,args):
		#handle file input
		self.inputFile()	
		#handle addition Parameters from command line
		self.addParam(args)
		#handle parameter file
		self.parameterFile()
		self.createOutputFileName()
		
	def createOutputFilePath(self,output_file_name):
		print "inside createOutputFilePath"
		if(workflow_config.result_folder.endswith('/')):
			self.outputFileName = workflow_config.result_folder + output_file_name
		else:
			self.outputFileName = workflow_config.result_folder + '/' + output_file_name
			
	def run(self):
		commands = []
		commands.append(self.program)
		commands.append(' ')
		commands.append(self.parameters)
		commands.append(self.additionalParameters)
		commands.extend(self.input)
		commands.append(' -f ')
		commands.append(self.outputFileName)
		print "commands ***************************************"
		print commands
		cmd_line = ' '.join(commands)
		print "command line \n"
		print(cmd_line)
		exit_code = os.system(cmd_line)
		if exit_code != 0:
			sys.stderr.write(" exited with status "+ str(exit_code) +"\n")
		else:
			print "seems to be a success!"

class IFactory():
	program = ''
	tool = ''
	def __init__(self,program):
		self.program = program
		self.tool = workflow_config.tools[program]

class BwaAln(Runner):
	def __init__(self,program,tool):
		Runner.__init__(self,program,tool)
		self.hasReferenceGenome = True
		

	def createOutputFileName(self):
		print "inside createOutputFileName"
		print self.input
		if(len(self.input) != 0):
			path,file_name = os.path.split(self.input[0])
			output_file_name = file_name +".sai"
			self.createOutputFilePath(output_file_name)
	class Factory(IFactory):
		def create(self): return BwaAln(self.program,self.tool)

class BwaSampe(Runner):
	def __init__(self,program,tool):
		Runner.__init__(self,program,tool)
		self.hasReferenceGenome = True
	
	def createOutputFileName(self):
		if(self.input):
			path,file_name = os.path.split(self.input[0])
			file_name = file_name[0:15]
			output_file_name = file_name + ".sam"
			self.createOutputFilePath(output_file_name)
	class Factory(IFactory):
		def create(self): return BwaSampe(self.program,self.tool)

class SamToBamConverter(Runner):
	def __init__(self,program,tool):
		Runner.__init__(self,program,tool)

class DSS_Client(Runner):
	def __init__(self,program,tool):
		Runner.__init__(self,program,tool)
	run(self):
		
