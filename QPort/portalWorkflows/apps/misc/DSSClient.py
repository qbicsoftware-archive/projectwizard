#!/usr/bin/env python
__author__ = 'wojnar'
from applicake.app import BasicApp
from applicake.coreutils.arguments import Argument
from applicake.coreutils.keys import Keys
from applicake.coreutils.info import IniInfoHandler
import os


class DSSClient(BasicApp):

    args = [Argument("EXECUTABLE", default='getdataset'),
            Argument("WORKDIR")]

    def prepare_run(self):
        self.log.debug("Preparing")
        executable = self.info['EXECUTABLE']
        if self.info.get('FILE'):
            self.command = 'cp ' + self.info['FILE'] + ' ' + self.info[Keys.WORKDIR]
        else:
            self.command = self.info['EXECUTABLE'] + ' -o ' + self.info[Keys.WORKDIR] + ' ' + self.info['CODE']

    def validate_run(self):
        self.log.debug('run validation.')
        if self.info.get('FILE'):
            self.info['FILE'] = self.info[Keys.WORKDIR] + os.path.basename(self.info['FILE'])
        else:
            try:
                for line in open('getdataset.out', 'r'):
                    splittedLine = line.split('\t')
                    self.info['FILE'] = splittedLine[1].rstrip()
            except IOError as e:
                self.log.error('Downloaded file lost\n')
        super(DSSClient, self).validate_run()
#use this class as executable
if __name__ == "__main__":
    DSSClient.run()