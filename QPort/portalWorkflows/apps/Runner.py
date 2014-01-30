#!/usr/bin/env python
__author__ = 'wojnar'

import argparse
import sys
from apps.version import __version__
#from apps import *


def get_class(kls):
    parts = kls.split('.')
    module = ".".join(parts[:-1])
    m = __import__(module)
    for comp in parts[1:]:
        m = getattr(m, comp)
    return getattr(m, parts[1])

parser = argparse.ArgumentParser(description='Executes one of the wished wrappers, e.g. ToppBase which by default executes OpenMSInfo.')
parser.add_argument('--wrapper', dest='tool', nargs='*',
                    default='ToppBase',
                    help='defines the program that will be run.')
parser.add_argument('--version', action='version', version=__version__)
parser.add_argument('--args', nargs=argparse.REMAINDER, dest='args', default='',
                    help='additional arguments')  # all remainder arguments
#parser.add_argument(--verbose,
args = parser.parse_args()

######
#apps is the current main folder of the workflow package
#####
apps = "apps."
if isinstance(args.tool, list):
    apps += args.tool[0]
else:
    apps += args.tool
sys.argv = args.args
tool = get_class(apps)
tool.run()