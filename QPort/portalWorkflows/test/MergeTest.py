__author__ = 'wojnar'

from twisted.trial.unittest import TestCase
import os
import sys
from apps.misc.Merge import Merge
from applicake.coreutils.info import get_handler
from StringIO import StringIO
from configobj import ConfigObj


class MyTestCase(TestCase):
    def MergeRun_correctIniFiles_Produces3iniOutputs(self):
        sys.argv = ['--MERGE', 'rawData/idpost', '--SPLIT', 'merge.ini']
        Merge.run()
        assert os.path.exists('merge.ini_0')
        assert os.path.exists('merge.ini_1')
        assert os.path.exists('merge.ini_2')

    def test_merge2(self):
        sys.argv = ['--MERGE', 'rawData/idpost', '--SPLIT', 'merge.ini']
        Merge.run()

        golden_merge_ini_0 = """FILE = /home/wojnar/QBiC/workflows/WorkflowsBasedOnAppli2ake/test/tmp/1401131833/0/ToppBase_IDPosteriorErrorProbability/velos005614.mzML.OMSSAAdapter.idXML.IDPosteriorErrorProbability.idXML, /home/wojnar/QBiC/workflows/WorkflowsBasedOnAppli2ake/test/tmp/1401131833/0/ToppBase_IDPosteriorErrorProbability/velos005614.mzML.XTandemAdapter.idXML.IDPosteriorErrorProbability.idXML
COMMENT = boolean parameter
EXECUTABLE = IDPosteriorErrorProbability
LOG_LEVEL = DEBUG
LOG_STORAGE = memory
SUBJOBLIST = FILE__0__3,
BASEDIR = /home/wojnar/QBiC/workflows/WorkflowsBasedOnAppli2ake/test/tmp
INPUT_OUTPUT_RELATIONSHIP = transform
JOB_IDX = 1401131833
WORKDIR = None
"""
        golden_dic = {}
        golden_dic['FILE'] = ['/home/wojnar/QBiC/workflows/WorkflowsBasedOnAppli2ake/test/tmp/1401131833/0/ToppBase_IDPosteriorErrorProbability/velos005614.mzML.OMSSAAdapter.idXML.IDPosteriorErrorProbability.idXML', '/home/wojnar/QBiC/workflows/WorkflowsBasedOnAppli2ake/test/tmp/1401131833/0/ToppBase_IDPosteriorErrorProbability/velos005614.mzML.XTandemAdapter.idXML.IDPosteriorErrorProbability.idXML']
        golden_dic['COMMENT'] = 'boolean parameter'
        golden_dic['EXECUTABLE'] = 'IDPosteriorErrorProbability'
        golden_dic['LOG_LEVEL'] = 'DEBUG'
        golden_dic['LOG_STORAGE'] = 'memory'
        golden_dic['SUBJOBLIST'] = ['FILE__0__3']
        golden_dic['BASEDIR'] = '/home/wojnar/QBiC/workflows/WorkflowsBasedOnAppli2ake/test/tmp'
        golden_dic['INPUT_OUTPUT_RELATIONSHIP'] = 'transform'
        golden_dic['JOB_IDX'] = '1401131833'
        golden_dic['WORKDIR'] = 'None'
        merge_ini_0 = ''
        ih1 = get_handler("merge.ini_0")
        merge_ini_0 = ih1.read("merge.ini_0")
        pseudo_file = StringIO()
        pseudo_file.write(golden_merge_ini_0)
        golden_merge_0 = ConfigObj(pseudo_file)
        golden_and_merge_are_equal = True
        for key in golden_dic:
            if(not merge_ini_0.get(key) or merge_ini_0[key] != golden_dic[key]):
                golden_and_merge_are_equal = False
                #print key
                #print merge_ini_0.get(key)
                #print golden_dic[key]
                break

        for key in merge_ini_0:
            if(not golden_dic.get(key) or golden_dic[key] != merge_ini_0[key]):
                golden_and_merge_are_equal = False
                #print key
                #print merge_ini_0[key]
                #print golden_dic.get(key)
                break


        self.assertEquals(golden_and_merge_are_equal, True)


    def tearDown(self):
        if os.path.exists('merge.ini_0'):
            os.remove('merge.ini_0')
        if os.path.exists('merge.ini_1'):
            os.remove('merge.ini_1')
        if os.path.exists('merge.ini_2'):
            os.remove('merge.ini_2')
#Test
# all files with same subjob should be merged together
#


#if __name__ == '__main__':
#    unittest.main()
