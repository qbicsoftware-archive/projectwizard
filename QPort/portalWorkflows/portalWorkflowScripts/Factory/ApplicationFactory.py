import sys
#sys.path.append('/home/guseuser/workflows/portalWorkflows/portalWorkflowScripts/Applications/')
from Applications.BwaAln   import BwaAln
from Applications.BwaSampe import BwaSampe
from Applications.SamToBam import SamToBam
from Applications.BamIndexer import BamIndexer
from Applications.SamtoolsMappingStatistics import SamtoolsMappingStatistics
class ApplicationFactory:
	factories ={}
	def addFactory(id, runnerFactory):
		ApplicationFactory.factories[id] = runnerFactory
	addFactory = staticmethod(addFactory)
	#A template method:
	def createRunner(idList):
		id =''
		if(isinstance(idList,str)):
			id = idList
		else:
			for i in idList:
				id += i
				id += ' '
		id = id.strip()
		#iif not ApplicationFactory.factories.has_key(id):
		#	ApplicationFactory.factories[id] = \
		#		eval(id + '.Factory()')
		return ApplicationFactory.factories[id].create()
	createRunner = staticmethod(createRunner) 
	#creates all available Factories, hardcoded for the moment
	def addAllAvailableFactories():
		ApplicationFactory().addFactory('bwa aln', BwaAln.Factory('bwa aln'))
		ApplicationFactory().addFactory('bwa sampe', BwaSampe.Factory('bwa sampe'))
		ApplicationFactory().addFactory('sam-to-bam',SamToBam.Factory('sam-to-bam'))
		ApplicationFactory().addFactory('samtools index', BamIndexer.Factory('samtools index'))
		ApplicationFactory().addFactory('mapping statistics', SamtoolsMappingStatistics.Factory('mapping statistics'))
	addAllAvailableFactories = staticmethod(addAllAvailableFactories)	
