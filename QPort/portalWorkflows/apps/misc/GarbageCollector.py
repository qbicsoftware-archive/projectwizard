#!/usr/bin/env python
__author__ = 'wojnar'

from applicake.app import BasicApp
from applicake.coreutils.arguments import Argument
from applicake.coreutils.keys import Keys
import os
import shutil

#Writes result file to the DROPBOX. Deletes Keys.BASEDIR directory and its content
class GarbageCollector(BasicApp):
        args = [Argument('RESULT_PARAMETER', help='defines which parameter holds the path to the result file.', default='FILE'),
                Argument('DROPBOX', help='environmental variable name alias to dropbox folder. e.g. in your .bashrc somthing like DROPBOX=/share/result-drpobox', default='DROPBOX')]

        def _setup_info(self):
            self.dropboxVariable = self.info["DROPBOX"]
            self.dropbox = os.environ[self.dropboxVariable]
            super(GarbageCollector, self)._setup_info()

        def execute_run(self):
            resultParameter = self.info['RESULT_PARAMETER']
            self.log.debug('resultParameter is ' + resultParameter)
            self.log.debug('writing into dropbox(' + self.dropbox + '):')
            if isinstance(self.info[resultParameter], list):
                for result in self.info[resultParameter]:
                    self.log.debug(self.log.debug(result))
                    self.copy_result_to_dropbox(result)
            else:
                self.log.debug(self.info[resultParameter])
                self.copy_result_to_dropbox(self.info[resultParameter])

        # copies result file or directory to dropbox
        def copy_result_to_dropbox(self, result):
            f = os.path.basename(result)
            try:
                destination = '%s/%s' % (self.dropbox, f)
                source = result
                if os.path.isdir(source):
                    shutil.copytree(source, destination)
                else:
                    shutil.copyfile(source, destination)
            except IOError as e:
                print "something is definitely going wrong"

            #shutil.rmtree(self.info[Keys.BASEDIR])

if __name__ == "__main__":
    GarbageCollector.run()