#!/usr/bin/env python
__author__ = 'wojnar'

import argparse
import sys
#from apps import *


def get_class(kls):
    print kls
    parts = kls.split('.')
    print parts
    print parts[:-1]
    module = ".".join(parts[:-1])
    print module
    m = __import__(module)
    print m
    for comp in parts[1:]:
        m = getattr(m, comp)
        print m
    return getattr(m, parts[1])

parser = argparse.ArgumentParser(description='Process something.')
parser.add_argument('--program', dest='tool', nargs='*',
                    default='dummy',
                    help='defines the program that will be run.')
parser.add_argument('--version', action='version', version='%(prog)s 0.2a')
parser.add_argument('--args', nargs=argparse.REMAINDER, dest='args', default='',
                    help='additional arguments')  # all remainder arguments
#parser.add_argument(--verbose,
args = parser.parse_args()
print args
print args.args

######
#apps is the current main folder of the workflow package
#####
apps = "apps."
apps += args.tool[0]
sys.argv = args.args
tool = get_class(apps)
tool.run()
