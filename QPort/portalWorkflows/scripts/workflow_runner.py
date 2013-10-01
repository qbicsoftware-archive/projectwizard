import sys
sys.path.append('/home/guseuser/workflows/BwaWorkflow')

import argparse, subprocess
from workflow_program_factory import WorkflowRunnerFactory as factory
parser = argparse.ArgumentParser(description='Process something.')
parser.add_argument('--program', dest='tool',nargs='*',
		default='dummy',
		help='defines the program that will be run.')
parser.add_argument('--version', action='version', version='%(prog)s 0.1a')
parser.add_argument('--args', nargs=argparse.REMAINDER, dest='args',default='',
		help='additional arguments')#all remainder arguments
#parser.add_argument(--verbose,
args = parser.parse_args()
print args

factory.addAllAvailableFactories()

runner = factory.createRunner(args.tool)

runner.handleInput(args.args)

runner.run()

#runner.writeOutput()


