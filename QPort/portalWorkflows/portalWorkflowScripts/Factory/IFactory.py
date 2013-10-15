from Utils.workflow_config import workflow_config
class IFactory():
	program = ''
	tool = ''
	def __init__(self,program):
		self.program = program
		self.tool = workflow_config.tools[program]
