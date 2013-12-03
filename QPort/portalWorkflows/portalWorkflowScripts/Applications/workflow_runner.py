#!/usr/bin/env python
import sys
#sys.path.append('/home/guseuser/workflows/portalWorkflows/portalWorkflowScripts/Applications/')
#sys.path.append('/home/guseuser/workflows/portalWorkflows/portalWorkflowScripts/Factory/')
import argparse
from Factory.ApplicationFactory import ApplicationFactory as factory
parser = argparse.ArgumentParser(description='Process something.')
parser.add_argument('--program', dest='tool',nargs='*',
		default='dummy',
		help='defines the program that will be run.')
parser.add_argument('--version', action='version', version='%(prog)s 0.2a')
parser.add_argument('--result', action='store_true',help='if set, output files will be saved in the database')
parser.add_argument('--args', nargs=argparse.REMAINDER, dest='args',default='',
		help='additional arguments')#all remainder arguments
#parser.add_argument(--verbose,
args = parser.parse_args()
print args

factory.addAllAvailableFactories()

runner = factory.createRunner(args.tool)

runner.saveResults(args.result)
runner.handleInput(args.args)

runner.execute()

runner.writeOutputInformation()

sys.exit(runner.validateOutput())


