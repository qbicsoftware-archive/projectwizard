#!/usr/bin/env python
__author__ = 'wojnar'
import applicake.apputils.dicts as dicts
from applicake.coreutils.arguments import Argument
from applicake.coreutils.info import get_handler
from applicake.app import BasicApp
from applicake.coreutils.keys import Keys
import glob


class Collate(BasicApp):
    args = [Argument(Keys.COLLATE)]

    def execute_run(self):
        paths = []
        to_merge = self.info[Keys.COLLATE].split(' ')
        if isinstance(to_merge, list) and len(to_merge) > 1:
            for key in to_merge:
                self.log.debug(glob.glob(key + "*_*"))
                key_paths = sorted(glob.glob(key + "*_*"))
                paths.extend(key_paths)
        else:
            self.log.debug(glob.glob(self.info[Keys.COLLATE] + "*_*"))
            paths = sorted(glob.glob(self.info[Keys.COLLATE] + "*_*"))
        del self.info[Keys.COLLATE]
        collector_config = self.info.copy()

        #read in
        for path in paths:
            self.log.debug('collating file [%s]' % path)
            config = get_handler(path).read(path)
            collector_config = dicts.merge(collector_config, config, priority='append')

        #unify
        for key in collector_config.keys():
            collector_config[key] = dicts.unify(collector_config[key])

        #write back
        self.info = collector_config

if __name__ == "__main__":
    Collate.run()