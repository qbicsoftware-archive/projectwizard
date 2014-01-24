#!/usr/bin/env python
__author__ = 'wojnar'
import glob
import applicake.coreutils.info as infohandler
from applicake.app import BasicApp
from applicake.coreutils.arguments import Argument
from applicake.apputils.dicts import merge, unify

from applicake.coreutils.keys import Keys


class Merge(BasicApp):
    args = [Argument(Keys.MERGE),
            Argument(Keys.SPLIT)
    ]


    #TODO
    #Sort ini files according to subjob.
    # Merge them. Similarly to the code below

    def execute_run(self):
        paths = []
        to_merge = self.info[Keys.MERGE].split(' ')
        if isinstance(to_merge, list) and len(to_merge) > 1:
            for key in to_merge:
                self.log.debug(glob.glob(key + "*_*"))
                key_paths = sorted(glob.glob(key + "*_*"))
                paths.extend(key_paths)
        else:
            self.log.debug(glob.glob(self.info[Keys.MERGE] + "*_*"))
            paths = sorted(glob.glob(self.info[Keys.MERGE] + "*_*"))
        self.log.debug("sorted")
        config_container = {}
        merge_candidates = set()
        for path in paths:
            self.log.debug("Merging " + path)
            config = infohandler.get_handler(path).read(path)
            config['path'] = path

            #merge_candidates.add(config[Keys.SUBJOBLIST])
            subjob = config[Keys.SUBJOBLIST]
            #append the current config to the one in container which is in the same subjob
            subjobkey = ''
            for job in subjob:
                subjobkey += job
            if subjobkey in config_container:
                config_container[subjobkey] = merge(config_container[subjobkey], config, priority='append')
            else:
                config_container[subjobkey] = config
        self.log.debug("config_container contains " + str(len(config_container)) + " configObjs.")
        #unify (only possible after all collected)
        for config in config_container.values():
            for key in config.keys():
                if key == Keys.SUBJOBLIST:
                    config[key] = unify(config[key], unlist_single=False)
                    continue
                if isinstance(config[key], list):
                    config[key] = unify(config[key])

        #write back
        self.log.debug("writing back files.")
        for config in config_container.values():
            self.log.debug(config['path'])
            i = ''
            if isinstance(config['path'], list) and len(config['path']) > 0:
                i = config['path'][0][-1]
            elif len(config['path']) > 0:
                i = config['path'][-1]

            #clean up
            for key in ['path']:
                del config[key]
            config[Keys.WORKDIR] = 'None'
            path = self.info[Keys.SPLIT] + '_' + i
            self.log.debug("Writing out " + path)
            infohandler.get_handler(path).write(config, path)

if __name__ == "__main__":
    Merge.run()