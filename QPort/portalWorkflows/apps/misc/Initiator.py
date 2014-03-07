#!/usr/bin/env python
import sys

__author__ = 'wojnar'

from appliapps.flow.split import Split
from applicake.coreutils.arguments import Argument, parse_arglist
import os
from applicake.coreutils.info import get_handler
from applicake.coreutils.keys import Keys
from applicake.apputils import dicts


class Initiator(Split):

    args = [Argument('PARAMETERS', help="Parameters config file location"),
            Argument('INPUTFILES', help="Input config file location"),
            Argument('WORKDIR'),
            Argument('ADDITIONAL_PARAMETERS', help='additional parameter for the executable are forwarded here, e.g. -threads 4', default='')]

    def _setup_info(self):
        #parse the command line arguments
        runnerargs = [Argument("OUTPUT", help="Output config file location"),
                      Argument("NAME", help="Name of node", default=self.__class__.__name__),
                      Argument('LOG_STORAGE', help="Storage type for out/err/log", default="memory"),
                      Argument('LOG_LEVEL', help="Logging level", default="DEBUG"),
                      Argument('BASEDIR', help="Base directory used to store files produced by the application. IMPORTANT: it is first checked whether it is an environ variable. if not it is assumed to be a path."),
                      Argument('MODULE', help="Module to load")] #requires installation of http://modules.sf.net
        appargs = self.args
        defaults, cliargs = parse_arglist(runnerargs + appargs)
        #construct info from defaults < info < commandlineargs
        FILE = []
        CODE = []
        ifileinfo = {}
        for line in open(cliargs.get("INPUTFILES", None), "r"):
            if line.strip():  # is not empty i guess
                splittedLine = line.split('=')
                splittedLine[0] = splittedLine[0].strip()
                splittedLine[1] = splittedLine[1].strip()
                if 'FILE' == splittedLine[0]:
                    FILE.append(splittedLine[1])
                elif 'CODE' == splittedLine[0]:
                    CODE.append(splittedLine[1])
        if FILE:
            ifileinfo['FILE'] = FILE
            ifileinfo[Keys.SPLIT_KEY] = 'FILE'
        elif CODE:
            ifileinfo['CODE'] = CODE
            ifileinfo[Keys.SPLIT_KEY] = 'CODE'
        ifileinfo[Keys.SPLIT] = ''
        ihP = get_handler(cliargs.get("PARAMETERS", None))
        parameterinfo = ihP.read(cliargs.get("PARAMETERS", None))
        self.info = dicts.merge(parameterinfo, ifileinfo)
        self.info = dicts.merge(cliargs, dicts.merge(self.info, defaults))
        #self.info[Keys.BASEDIR] += os.path.sep + self.info["WORKFLOW"]
        if not self.info[Keys.SPLIT]:
            self.info[Keys.SPLIT] = self.info[Keys.OUTPUT]
        basedir = os.environ.get(self.info[Keys.BASEDIR])
        if basedir:
            self.info[Keys.BASEDIR] = basedir


    def _create_wdir(self):
        #if not os.path.exists(self.info[Keys.BASEDIR]):
        #    os.mkdir(self.info[Keys.BASEDIR],0775)
        super(Initiator, self)._create_wdir()

#use this class as executable
if __name__ == "__main__":
    #tmpFolder = os.environ['TMP']#["GUSE_TMP"]

    #additional = ["--WORKDIR",  "--BASEDIR", tmpFolder]
    #sys.argv.extend(additional)
    Initiator.run()