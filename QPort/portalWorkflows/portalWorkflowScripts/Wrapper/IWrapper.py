
class IWrapper(object):
	def __init__(self,program,tool): pass
	def setInput(self): pass
	def setAdditionalParameters(self, args): pass
	def setParameters(self): pass
	def setOutput(self): pass
	def buildCommand(self): pass
	def execute(self): pass
	def writeOutputInformation(self): pass
	def writeGarbageInformation(self): pass
	def setLogger(self, logger): pass 
