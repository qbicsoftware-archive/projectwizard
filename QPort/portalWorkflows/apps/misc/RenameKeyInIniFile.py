#!/usr/bin/env python
__author__ = 'wojnar'
import glob
import applicake.coreutils.info as infohandler
from applicake.app import BasicApp
from applicake.coreutils.arguments import Argument
from applicake.apputils.dicts import merge, unify
import sys
from applicake.coreutils.keys import Keys


class RenameKeyInIniFile(BasicApp):
    args = [Argument('RENAME', help='pairs of old key name and new keyname e.g. --RENAME oldKey1 newKey1 oldKey2 newKey2')]

    def execute_run(self):
        renames = self.info['RENAME']
        #renames is a string
        renames_lis = renames.split(' ')
        nu_renames = len(renames_lis)
        self.test_keys_are_pairs(renames_lis, nu_renames)
        self.log.debug("renaming the following keys: " + renames)
        for i in xrange(0, nu_renames, 2):
            self.info[renames_lis[i+1]] = self.info.pop(renames_lis[i])

        del self.info['RENAME']

    @staticmethod
    def test_keys_are_pairs(renames_lis, nu_renames):
        if not renames_lis or nu_renames % 2 != 0:
            raise KeyError('number of old and new keys does not match.')
    def _create_wdir(self):
        pass

if __name__ == "__main__":
    RenameKeyInIniFile.run()