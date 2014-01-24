__author__ = 'wojnar'

import unittest
import sys
import os
from apps.misc.RenameKeyInIniFile import RenameKeyInIniFile
from applicake.coreutils.info import get_handler


class MyTestCase(unittest.TestCase):
    def test1_RenameKeyInIniFIle(self):
        sys.argv = ['--RENAME', '--INPUT', 'rawData/featurefinder.ini_0']
        #RenameKeyInIniFile.run()
        renames_lis = []
        nu_renames = 0
        self.assertRaises(KeyError, RenameKeyInIniFile.test_keys_are_pairs, renames_lis, nu_renames)

    def test2_RenameKeyInIniFIle(self):
        sys.argv = ['--RENAME', 'FILE IDMapper.id COMMENT', '--INPUT', 'rawData/featurefinder.ini_0']
        #RenameKeyInIniFile.run()
        renames_lis = ['FILE', 'IDMapper.id', 'COMMENT']
        nu_renames = 3
        self.assertRaises(KeyError, RenameKeyInIniFile.test_keys_are_pairs, renames_lis, nu_renames)

    def test3_RenameKeyInIniFIle(self):
        sys.argv = ['--RENAME', 'FILE', '--INPUT', 'rawData/featurefinder.ini_0']
        #RenameKeyInIniFile.run()
        renames_lis = ['FILE']
        nu_renames = 1
        self.assertRaises(KeyError, RenameKeyInIniFile.test_keys_are_pairs, renames_lis, nu_renames)

    def test4_RenameKeyInIniFIle(self):
        sys.argv = ['--RENAME', 'FILE IDMapper.id COMMENT KOMMENTAR', '--INPUT', 'rawData/featurefinder.ini_0', '--OUTPUT', 'renamed.featurefinder.ini_0']
        RenameKeyInIniFile.run()

        golden_dic = {}
        golden_dic['IDMapper.id'] = '/home/wojnar/QBiC/workflows/WorkflowsBasedOnAppli2ake/test/tmp/1401151322/0/ToppBase_FeatureFinderCentroided/velos005614.mzML.FeatureFinderCentroided.featureXML'
        golden_dic['KOMMENTAR'] = 'boolean parameter'
        golden_dic['EXECUTABLE'] = 'FeatureFinderCentroided'
        golden_dic['LOG_LEVEL'] = 'DEBUG'
        golden_dic['LOG_STORAGE'] = 'memory'
        golden_dic['SUBJOBLIST'] = ['FILE__0__3']
        golden_dic['BASEDIR'] = '/home/wojnar/QBiC/workflows/WorkflowsBasedOnAppli2ake/test/tmp'
        golden_dic['INPUT_OUTPUT_RELATIONSHIP'] = 'transform'
        golden_dic['JOB_IDX'] = '1401151322'
        golden_dic['WORKDIR'] = '/home/wojnar/QBiC/workflows/WorkflowsBasedOnAppli2ake/test/tmp/1401151322/0/ToppBase_FeatureFinderCentroided/'


        ih1 = get_handler("renamed.featurefinder.ini_0")
        featureFinder_ini_0 = ih1.read("renamed.featurefinder.ini_0")

        golden_and_merge_are_equal = True
        for key in golden_dic:
            if(not featureFinder_ini_0.get(key) or featureFinder_ini_0[key] != golden_dic[key]):
                golden_and_merge_are_equal = False
                print key
                print featureFinder_ini_0.get(key)
                print golden_dic[key]
                break

        for key in featureFinder_ini_0:
            if(not golden_dic.get(key) or golden_dic[key] != featureFinder_ini_0[key]):
                golden_and_merge_are_equal = False
                print key
                print featureFinder_ini_0[key]
                print golden_dic.get(key)
                break


        self.assertEquals(golden_and_merge_are_equal, True)


    def tearDown(self):
        if os.path.exists('renamed.featurefinder.ini_0'):
            os.remove('renamed.featurefinder.ini_0')


if __name__ == '__main__':
    unittest.main()
