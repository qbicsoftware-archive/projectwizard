__author__ = 'wojnar'
from ruffus import *
import sys
import os
from cStringIO import StringIO
import multiprocessing
from apps.misc.init import Initiator
from apps.misc.DSSClient import DSSClient
from apps.toppTools.ToppBase import ToppBase
from apps.misc.Merge import Merge  # from appliapps.flow.merge import Merge
from apps.misc.RenameKeyInIniFile import RenameKeyInIniFile
from apps.R_Scripts.R_Script import R_Script
import time
import glob

current_milli_time = lambda: int(round(time.time() * 1000))
current_datetime = lambda: str(time.strftime("%y-%m-%d %H:%M", time.localtime()))
input_and_parameters = [['input.ini', 'parameter.ini']]

def basic_command(executable, __in, __out):
    return 'python ToppBase.py --EXECUTABLE %s --in %s --out %s' % (executable, __in, __out)

def add_output(command, output):
    return command + ' --OUTPUT ' + output

def add_additional_parameters(command, additional_parameters):
    return command + ' ' + additional_parameters

#def execute(command):
#    p = Popen(command, shell=True, stdout=PIPE, stderr=PIPE)
#    return p.communicate()


def setup_input_files():
    with open("input.ini", "w") as f:
        f.write("""FILE = /home/wojnar/QBiC/workflows/WorkflowsBasedOnAppli2ake/test/labelfreeExampleData/Example_Data/tiny/velos005614.mzML
        FILE = /home/wojnar/QBiC/workflows/WorkflowsBasedOnAppli2ake/test/labelfreeExampleData/Example_Data/tiny/velos005615.mzML
        FILE = /home/wojnar/QBiC/workflows/WorkflowsBasedOnAppli2ake/test/labelfreeExampleData/Example_Data/tiny/velos005616.mzML""")

def setup_parameter():
    with open("parameter.ini", "w") as f:
        f.write("""WORKFLOW = labelfreeQuantQT_%d

OMSSAAdapter.out_delete_this_tag = idXML

OMSSAAdapter.omssa_executable = /home/wojnar/QBiC/Software/omssa-2.1.9.linux/omssacl

OMSSAAdapter.database = /home/wojnar/QBiC/workflows/WorkflowsBasedOnAppli2ake/test/labelfreeExampleData/Example_Data/FASTA/uniprot_sprot_101104_human_concat.fasta

XTandemAdapter.out_delete_this_tag = idXML

XTandemAdapter.xtandem_executable = /home/wojnar/QBiC/Software/tandem-linux-13-09-01-1/bin/tandem.exe

XTandemAdapter.database = /home/wojnar/QBiC/workflows/WorkflowsBasedOnAppli2ake/test/labelfreeExampleData/Example_Data/FASTA/uniprot_sprot_101104_human_concat.fasta

IDPosteriorErrorProbability.out_delete_this_tag = idXML

FeatureFinderCentroided.out_delete_this_tag = featureXML

IDMerger.out_delete_this_tag = idXML

ConsensusID.out_delete_this_tag = idXML

PeptideIndexer.out_delete_this_tag = idXML

PeptideIndexer.fasta = /home/wojnar/QBiC/workflows/WorkflowsBasedOnAppli2ake/test/labelfreeExampleData/Example_Data/FASTA/uniprot_sprot_101104_human_concat.fasta

PeptideIndexer.decoy_string = sw

PeptideIndexer.prefix = True

PeptideIndexer.enzyme:specificity = none

FalseDiscoveryRate.out_delete_this_tag = idXML

IDFilter.out_delete_this_tag = idXML

IDMapper.out_delete_this_tag = featureXML

QCCalculator.out_delete_this_tag = qcML

QCExtractor.outputParameter = -out_csv

QCExtractor.out_delete_this_tag = csv

fractionalMass.out_delete_this_tag = png

QCEmbedder.out_delete_this_tag = qcML

QCShrinker.out_delete_this_tag = qcML

bwa_align.n = 0.04

bwa_align.o = 1

sam_to_bam.S = True

sam_to_bam.u = True

bwa_align.d = 10

Refgenome = EC_K12

SPACE = QBIC

PROJECT = BWAT

COMMENT = boolean parameter# started %s""" % (current_milli_time(), current_datetime()))

def setup():
    setup_input_files()
    setup_parameter()


@follows(setup)
@split(input_and_parameters, 'output.init.ini_*')
def init(input_and_parameters, ini_file):
    tmpFolder = os.environ["GUSE_TMP"]
    sys.argv = ['--PARAMETERS', 'parameter.ini', '--INPUTFILES',
                'input.ini', '--OUTPUT', 'output.init.ini', '--BASEDIR', tmpFolder]
    Initiator.run()
    #TODO
    #Init generates an ini file for each mzML file. Splitting happens in FILE or CODE??.
    #additionally, it creates the parameters for each tool -> bwa_align = -n 0.04 -o 1 -d 10.

#regex(r'...'): raw string notation for regular expressions;
# backslashes are not handled in any special way in a string literal prefixed with 'r'
@transform(init, regex('output.init.ini_*'), 'dss.ini_')  # regex(r'.init.ini_[0-9]*'), r'.dss.ini')
def dss(input_file, output_file):
    sys.argv = ['--OUTPUT', output_file, '--INPUT', input_file]
    DSSClient.run()
    #TODO
    #Execute DSS_Client. ADD Parameter dataset_location {OPENBIS, PATH}.
    #If Openbis download from openbis. IF path, use given path.
    #returns ini file with path(s) to the downloaded file(s) in parameter out.

@follows(dss)
def OpenMSInfo():
    sys.argv = ['--WORKDIR']
    ToppBase.run()

@follows(OpenMSInfo)
@transform(dss, regex('dss.ini'), 'omssa.ini_')  # suffix(".dss.ini"), '.OMSSAAdapter.ini')
def OMSSAAdapter(input_file, output_file):
    #f_name = sys._getframe().f_code.co_name
    #cmdLine = basic_command(f_name, input_file, output_file)
    #cmdLine = add_additional_parameters(cmdLine, '')
    sys.argv = ['--OUTPUT', output_file, '--INPUT', input_file, "--EXECUTABLE", "OMSSAAdapter"]  # , '--WORKDIR']
    ToppBase.run()
    #output, error = execute(cmdLine)
    #output_stream = StringIO(output)
    #error_stream = StringIO(error)

@follows(OpenMSInfo)
@transform(dss, regex('dss.ini'), 'tandem.ini_')  # suffix(".dss.ini"), '.OMSSAAdapter.ini')
def XTandemAdapter(input_file, output_file):
    sys.argv = ['--OUTPUT', output_file, '--INPUT', input_file , "--EXECUTABLE", "XTandemAdapter"]
    ToppBase.run()

@transform(dss, regex('dss.ini'), 'featurefinder.ini_')
def FeatureFinderCentroided(input_file, output_file):
    sys.argv = ['--OUTPUT', output_file, '--INPUT', input_file , "--EXECUTABLE", "FeatureFinderCentroided"]
    ToppBase.run()

@transform(OMSSAAdapter, regex('omssa.ini'), 'idpost.ini')
def IDPosteriorErrorProbability(input_file, output_file):
    sys.argv = ['--OUTPUT', output_file, '--INPUT', input_file , "--EXECUTABLE", "IDPosteriorErrorProbability", "--ADDITIONAL_PARAMETERS", '-output_name gnuplot_file.txt', '--WORKDIR']
    ToppBase.run()
@transform(XTandemAdapter, regex('tandem.ini'), 'idpost1.ini')
def IDPosteriorErrorProbability2(input_file, output_file):
    sys.argv = ['--OUTPUT', output_file, '--INPUT', input_file , "--EXECUTABLE", "IDPosteriorErrorProbability", "--ADDITIONAL_PARAMETERS", '-output_name gnuplot_file1.txt', '--WORKDIR']
    ToppBase.run()

#@collate([IDPosteriorErrorProbability, IDPosteriorErrorProbability2], regex('idpost'), 'merge.ini_*')
#@split/@merge([IDPosteriorErrorProbability, IDPosteriorErrorProbability2], 'merge.ini_*')
@merge([IDPosteriorErrorProbability, IDPosteriorErrorProbability2], 'merge.ini_0')
def MergeInis(input_files, output_files):
    #input =[[input_files[0], input_files[3]], [input_files[1], input_files[4]], [input_files[2], input_files[5]]]
    #j = 0
    #for i in input:
    #    print i
    sys.argv = ['--MERGE', 'idpost', '--SPLIT', 'merge.ini']
    Merge.run()
    #output_files = glob.glob('merge.ini_*')
    #print output_files
    #    newname = 'merge.ini_%d' % j
    #    os.rename('merge.ini_0', newname)
    #    j += 1

@transform(MergeInis, regex('merge.ini'), 'idmerger.ini')
def IDMerger(input_file, output_file):
    input_files = glob.glob('merge.ini_*')
    output_file = []
    for f in input_files:
        o = 'idmerger.ini_' + f[-1]
        sys.argv = ['--OUTPUT', o, '--INPUT', f, "--EXECUTABLE", "IDMerger", '--INPUT_OUTPUT_RELATIONSHIP', 'merge', '--WORKDIR']
        ToppBase.run()
        output_file.append(o)

@transform(IDMerger, regex('idmerger.ini'), 'consensusID.ini')
def ConsensusID(input_file, output_file):
    input_files = glob.glob('idmerger.ini_*')
    output_file = []
    for f in input_files:
        o = 'consensusID.ini_' + f[-1]
        sys.argv = ['--OUTPUT', o, '--INPUT', f, "--EXECUTABLE", "ConsensusID", '--INPUT_OUTPUT_RELATIONSHIP', 'transform', '--WORKDIR']
        ToppBase.run()
        output_file.append(o)

@transform(ConsensusID, regex('consensusID.ini'), 'peptideIndexer.ini')
def PeptideIndexer(input_file, output_file):
    input_files = glob.glob('consensusID.ini_*')
    output_file = []
    for f in input_files:
        o = 'peptideIndexer.ini_' + f[-1]
        sys.argv = ['--OUTPUT', o, '--INPUT', f, "--EXECUTABLE", "PeptideIndexer", '--INPUT_OUTPUT_RELATIONSHIP', 'transform', '--WORKDIR']
        ToppBase.run()
        output_file.append(o)

@transform(PeptideIndexer, regex('peptideIndexer.ini'), 'fdr.ini')
def FalseDiscoveryRate(input_file, output_file):
    input_files = glob.glob('peptideIndexer.ini_*')
    output_file = []
    for f in input_files:
        o = 'fdr.ini_' + f[-1]
        sys.argv = ['--OUTPUT', o, '--INPUT', f, "--EXECUTABLE", "FalseDiscoveryRate", '--INPUT_OUTPUT_RELATIONSHIP', 'transform', '--WORKDIR']
        ToppBase.run()
        output_file.append(o)

@transform(FalseDiscoveryRate, regex('fdr.ini'), 'idfilter.ini')
def IDFilter(input_file, output_file):
    input_files = glob.glob('fdr.ini_*')
    output_file = []
    for f in input_files:
        o = 'idfilter.ini_' + f[-1]
        sys.argv = ['--OUTPUT', o, '--INPUT', f, "--EXECUTABLE", "IDFilter", '--INPUT_OUTPUT_RELATIONSHIP', 'transform', '--WORKDIR']
        ToppBase.run()
        output_file.append(o)


@transform(IDFilter, regex('idfilter.ini'), 'idfilter_renamed.ini')
def RenameIDFilterForIDMapper(input_file, output_file):
    input_files = glob.glob('idfilter.ini_*')
    output_file = []
    for f in input_files:
        o = 'idfilter_renamed.ini_' + f[-1]
        sys.argv = ['--RENAME', 'FILE IDMapper.id', '--INPUT', f, '--OUTPUT', o]
        RenameKeyInIniFile.run()
        output_file.append(o)

@merge([RenameIDFilterForIDMapper, FeatureFinderCentroided], 'mergedIDFWithFFC.ini')
def MergeIDFWithFFC(input_files, output_files):
    #input =[[input_files[0], input_files[3]], [input_files[1], input_files[4]], [input_files[2], input_files[5]]]
    #j = 0
    #for i in input:
    #    print i
    sys.argv = ['--MERGE', 'idfilter_renamed featurefinder', '--SPLIT', 'mergedIDFWithFFC.ini']
    Merge.run()


@follows(FeatureFinderCentroided)
@follows(MergeIDFWithFFC)
def IDMapper():
    input_files = glob.glob('mergedIDFWithFFC.ini_*')
    output_file = []
    for f in input_files:
        o = 'idmapper.ini_' + f[-1]
        sys.argv = ['--OUTPUT', o, '--INPUT', f, "--EXECUTABLE", "IDMapper", '--INPUT_OUTPUT_RELATIONSHIP', 'transform', '--WORKDIR']
        ToppBase.run()
        output_file.append(o)

@follows(IDMapper)
def RenameIDMForQCalculator():
    input_files = glob.glob('idmapper.ini_*')
    output_file = []
    for f in input_files:
        o = 'idfAndIdmRenamedForQcc.ini_' + f[-1]
        sys.argv = ['--RENAME', 'FILE QCCalculator.feature IDMapper.id QCCalculator.id', '--INPUT', f, '--OUTPUT', o]
        RenameKeyInIniFile.run()
        output_file.append(o)

@follows(dss)
@follows(RenameIDMForQCalculator)
def MergeForQCC():
    sys.argv = ['--MERGE', 'idfAndIdmRenamedForQcc dss', '--SPLIT', 'merged_IDF_IDM_DSS.ini']
    Merge.run()



@follows(MergeForQCC)
def QCCalculator():
    input_files = glob.glob('merged_IDF_IDM_DSS.ini_*')
    output_file = []
    for f in input_files:
        o = 'qcc.ini_' + f[-1]
        sys.argv = ['--OUTPUT', o, '--INPUT', f, "--EXECUTABLE", "QCCalculator", '--INPUT_OUTPUT_RELATIONSHIP', 'transform', '--WORKDIR']
        ToppBase.run()
        output_file.append(o)

@follows(QCCalculator)
def QCExtractor():
    input_files = glob.glob('qcc.ini_*')
    output_file = []
    for f in input_files:
        o = 'qcex.ini_' + f[-1]
        sys.argv = ['--OUTPUT', o, '--INPUT', f, "--EXECUTABLE", "QCExtractor", '--INPUT_OUTPUT_RELATIONSHIP', 'transform', '--WORKDIR', '--ADDITIONAL_PARAMETERS', '-qp QC:0000047']
        ToppBase.run()
        output_file.append(o)


@follows(QCExtractor)
def fractionalMass():
    input_files = glob.glob('qcex.ini_*')
    output_file = []
    for f in input_files:
        o = 'fm.ini_' + f[-1]
        sys.argv = ['--OUTPUT', o, '--INPUT', f, "--EXECUTABLE", "/home/wojnar/QBiC/workflows/WorkflowsBasedOnAppli2ake/apps/R_Scripts/fractionalMass.R", '--WORKDIR']
        R_Script.run()
        output_file.append(o)

@follows(fractionalMass)
def renameFractionalMass():
    input_files = glob.glob('fm.ini_*')
    output_file = []
    for f in input_files:
        o = 'fm_renamed.ini_' + f[-1]
        sys.argv = ['--RENAME', 'FILE QCEmbedder.plot', '--INPUT', f, '--OUTPUT', o]
        RenameKeyInIniFile.run()
        output_file.append(o)

@follows(renameFractionalMass)
def mergeRenamedFractionalMassWithQCCalculator():
    sys.argv = ['--MERGE', 'fm_renamed qcc', '--SPLIT', 'merged_FM_QCC.ini']
    Merge.run()


@follows(mergeRenamedFractionalMassWithQCCalculator)
def QCEmbedder():
    input_files = glob.glob('merged_FM_QCC.ini_*')
    output_file = []
    for f in input_files:
        o = 'qcem.ini_' + f[-1]
        sys.argv = ['--OUTPUT', o, '--INPUT', f, "--EXECUTABLE", "QCEmbedder", '--INPUT_OUTPUT_RELATIONSHIP', 'transform', '--WORKDIR', '--ADDITIONAL_PARAMETERS', '-cv_acc QC:0000043 -qp_att_acc QC:0000007']
        ToppBase.run()
        output_file.append(o)

@follows(QCEmbedder)
def QCShrinker():
    input_files = glob.glob('qcem.ini_*')
    output_file = []
    for f in input_files:
        o = 'qcsh.ini_' + f[-1]
        sys.argv = ['--OUTPUT', o, '--INPUT', f, "--EXECUTABLE", "QCShrinker", '--INPUT_OUTPUT_RELATIONSHIP', 'transform', '--WORKDIR']
        ToppBase.run()
        output_file.append(o)


def GarbageCollector():
    raise NotImplementedError

#pipeline_printout(sys.stdout, [IDMerger])
pipeline_run([QCShrinker], multiprocess=multiprocessing.cpu_count())
#pipeline_printout_graph ('flowchart.svg','svg',[QCShrinker],no_key_legend = False)
