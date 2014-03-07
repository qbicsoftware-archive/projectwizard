__author__ = 'wojnar'

from apps.misc.Initiator import Initiator
import sys
import os
import unittest

class DictDiffer(object):
    """
    A dictionary difference calculator
    Originally posted as:
    http://stackoverflow.com/questions/1165352/fast-comparison-between-two-python-dictionary/1165552#1165552
    Calculate the difference between two dictionaries as:
    (1) items added
    (2) items removed
    (3) keys same in both but changed values
    (4) keys same in both and unchanged values
    """
    def __init__(self, current_dict, past_dict):
        self.current_dict, self.past_dict = current_dict, past_dict
        self.current_keys, self.past_keys = [
            set(d.keys()) for d in (current_dict, past_dict)
        ]
        self.intersect = self.current_keys.intersection(self.past_keys)

    def differ(self):
        isAEmpty = not self.added()
        isREmpty = not self.removed()
        isCEmpty = not self.changed()
        return not isAEmpty or not isREmpty or not isCEmpty

    def added(self):
        return self.current_keys - self.intersect

    def removed(self):
        return self.past_keys - self.intersect

    def changed(self):
        return set(o for o in self.intersect
                   if self.past_dict[o] != self.current_dict[o])

    def unchanged(self):
        return set(o for o in self.intersect
                   if self.past_dict[o] == self.current_dict[o])



class MyTestCase(unittest.TestCase):



    def test_SETUP_info_NO_workdir_flag_set_CREATES_wordir_anyhow(self):
        initi = Initiator()
        sys.argv = ['--INPUTFILES', '/home/wojnar/QBiC/workflows/WorkflowRepository/apps/misc/input.ini',
                      '--PARAMETERS','/home/wojnar/QBiC/workflows/WorkflowRepository/apps/misc/parameter.ini',
                      '--OUTPUT', 'delete_me.ini', '--BASEDIR', '.']
        initi._setup_info()

        golden_info = {'PARAMETERS': '/home/wojnar/QBiC/workflows/WorkflowRepository/apps/misc/parameter.ini',
                  'INPUTFILES': '/home/wojnar/QBiC/workflows/WorkflowRepository/apps/misc/input.ini', 'ADDITIONAL_PARAMETERS': '',
                  'OUTPUT': 'delete_me.ini', 'NAME': 'Initiator',
                  'LOG_STORAGE': 'memory', 'LOG_LEVEL': 'DEBUG', 'BASEDIR': '.', 'WORKFLOW': 'labelfreeQuantQT_1389276927869',
                  'Refgenome': 'EC_K12', 'SPACE': 'QBIC', 'PROJECT': 'BWAT', 'COMMENT': 'boolean parameter',
                  'FILE': ['20130828152157222-557'], 'SPLIT': 'delete_me.ini', 'SPLIT_KEY': 'FILE'}

        dictDiffer = DictDiffer(golden_info, initi.info)
        workdir = set()
        workdir.add('WORKDIR')


        self.assertEqual(dictDiffer.removed(), workdir)

    def test_SETUP_info_BASEDIR_is_initially_env_variable_BASEDIR_is_path(self):
        os_environ = '___OsEnvironForInitiatorAPPUnitTEst__'
        path_os_environ = '/path/is/set'
        initi = Initiator()
        os.environ[os_environ] = path_os_environ
        sys.argv = ['--INPUTFILES', '/home/wojnar/QBiC/workflows/WorkflowRepository/apps/misc/input.ini',
                      '--PARAMETERS','/home/wojnar/QBiC/workflows/WorkflowRepository/apps/misc/parameter.ini',
                      '--OUTPUT', 'delete_me.ini', '--BASEDIR', os_environ]
        initi._setup_info()

        golden_info = {'PARAMETERS': '/home/wojnar/QBiC/workflows/WorkflowRepository/apps/misc/parameter.ini',
                  'INPUTFILES': '/home/wojnar/QBiC/workflows/WorkflowRepository/apps/misc/input.ini', 'ADDITIONAL_PARAMETERS': '',
                  'OUTPUT': 'delete_me.ini', 'NAME': 'Initiator',
                  'LOG_STORAGE': 'memory', 'LOG_LEVEL': 'DEBUG', 'BASEDIR': path_os_environ, 'WORKFLOW': 'labelfreeQuantQT_1389276927869',
                  'Refgenome': 'EC_K12', 'SPACE': 'QBIC', 'PROJECT': 'BWAT', 'COMMENT': 'boolean parameter',
                  'FILE': ['20130828152157222-557'], 'SPLIT': 'delete_me.ini', 'SPLIT_KEY': 'FILE', 'WORKDIR': True}

        dictDiffer = DictDiffer(golden_info, initi.info)

        self.assertEqual(dictDiffer.differ(), False)

if __name__ == '__main__':
    unittest.main()
