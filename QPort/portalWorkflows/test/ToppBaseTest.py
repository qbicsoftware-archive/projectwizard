__author__ = 'wojnar'
from apps.toppTools.ToppBase import ToppBase
import unittest


class ToppBaseCase(unittest.TestCase):
    def test_parse_parameters_standardInput_ParameterStringCorrect(self):
        t = ToppBase()
        t.info = {'EXECUTABLE': 'PeptideIndexer', 'PeptideIndexer.out_delete_this_tag': 'idXML',
                  'TextEditor.blu': 'bla', 'FILE': 'input.feature', 'WORKDIR': '/some/where/',
                  'PeptideIndexer.fasta': 'database.fasta'}
        parameters = t.parse_parameters()
        golden_parameters = '-fasta database.fasta -in input.feature -out /some/where/input.idXML'
        self.assertIn(golden_parameters, parameters)

    def test_parse_parameters_standardInputWithAdditionalParameters_ParameterStringCorrect(self):
        t = ToppBase()
        t.info = {'EXECUTABLE': 'PeptideIndexer', 'PeptideIndexer.out_delete_this_tag': 'idXML',
                  'TextEditor.blu': 'bla', 'FILE': 'input.feature', 'WORKDIR': '/some/where/',
                  'PeptideIndexer.fasta': 'database.fasta', 'ADDITIONAL_PARAMETERS': '-a 2 -b 3'}
        parameters = t.parse_parameters()
        golden_parameters = '-fasta database.fasta -in input.feature -out /some/where/input.idXML'
        self.assertIn(golden_parameters, parameters)

    def test_parse_parameters_outParameterMissing_OutputHasSameExtensionAsInput(self):
        t = ToppBase()
        t.info = {'EXECUTABLE': 'PeptideIndexer',
                  'TextEditor.blu': 'bla', 'FILE': 'input.feature', 'WORKDIR': '/some/where/',
                  'PeptideIndexer.fasta': 'database.fasta', 'ADDITIONAL_PARAMETERS': '-a 2 -b 3'}
        parameters = t.parse_parameters()
        golden_parameters = '-fasta database.fasta -in input.feature -out /some/where/input.feature'
        self.assertIn(golden_parameters, parameters)

    def test_parse_parameters_FILEParameterMissing_ThrowsException(self):
        t = ToppBase()
        t.info = {'EXECUTABLE': 'PeptideIndexer',
                  'TextEditor.blu': 'bla', 'WORKDIR': '/some/where/',
                  'PeptideIndexer.fasta': 'database.fasta', 'ADDITIONAL_PARAMETERS': '-a 2 -b 3'}
        parameters = t.parse_parameters()
        golden_parameters = '-fasta database.fasta'
        self.assertIn(golden_parameters, parameters)

    def test_parse_parameters_outputParameterGiven_out_csvInsteadOfOut(self):
        t = ToppBase()
        t.info = {'EXECUTABLE': 'PeptideIndexer',
                  'TextEditor.blu': 'bla', 'FILE': 'input.feature', 'WORKDIR': '/some/where/',
                  'PeptideIndexer.fasta': 'database.fasta', 'ADDITIONAL_PARAMETERS': '-a 2 -b 3',
                  'PeptideIndexer.outputParameter': '-out_csv', 'PeptideIndexer.out_delete_this_tag': 'idXML'}
        parameters = t.parse_parameters()
        golden_parameters = '-fasta database.fasta -in input.feature -out_csv /some/where/input.idXML'
        self.assertIn(golden_parameters, parameters)

    def test_parse_parameters_use_as_keyParameterSet_first_key_instead_of_second_in_parameter(self):
        t = ToppBase()
        t.info = {'EXECUTABLE': 'PeptideIndexer',
                  'TextEditor.blu': 'bla', 'FILE': 'input.feature', 'WORKDIR': '/some/where/',
                  'WORKFLOW.DATABASE': 'database.fasta', 'ADDITIONAL_PARAMETERS': '-a 2 -b 3', 'PeptideIndexer.outputParameter': '-out_csv',
                  'use_as_key': 'PeptideIndexer.fasta WORKFLOW.DATABASE'}
        parameters = t.parse_parameters()
        golden_parameters = '-fasta database.fasta -in input.feature -out_csv /some/where/input.feature'
        golden_info = {'EXECUTABLE': 'PeptideIndexer',
                  'TextEditor.blu': 'bla', 'FILE': '/some/where/input.feature', 'WORKDIR': '/some/where/',
                  'WORKFLOW.DATABASE': 'database.fasta', 'ADDITIONAL_PARAMETERS': '-a 2 -b 3', 'PeptideIndexer.outputParameter': '-out_csv',
                  'use_as_key': 'PeptideIndexer.fasta WORKFLOW.DATABASE', 'outputParameter': '-out_csv', 'PeptideIndexer.fasta': 'database.fasta'}

        {'PeptideIndexer.fasta': 'database.fasta', 'EXECUTABLE': 'PeptideIndexer', 'WORKDIR': '/some/where/',
         'use_as_key': 'PeptideIndexer.fasta WORKFLOW.DATABASE', 'FILE': 'input.feature',
         'ADDITIONAL_PARAMETERS': '-a 2 -b 3', 'TextEditor.blu': 'bla', 'outputParameter': '-out_csv',
         'PeptideIndexer.outputParameter': '-out_csv', 'WORKFLOW.DATABASE': 'database.fasta'}
        self.assertIn(golden_parameters, parameters)
        golden_and_merge_are_equal = True
        for key in golden_info:
            if(not t.info.get(key) or t.info[key] != golden_info[key]):
                golden_and_merge_are_equal = False
                #print key
                #print t.info.get(key)
                #print golden_info[key]
                break

        for key in t.info:
            if(not golden_info.get(key) or golden_info[key] != t.info[key]):
                golden_and_merge_are_equal = False
                #print key
                #print t.info[key]
                #print golden_info.get(key)
                break


        self.assertEquals(golden_and_merge_are_equal, True)

    def test_parse_parameters_use_as_keyParameterSet_first_key_instead_of_second_in_parameter_FILE(self):
        t = ToppBase()
        t.info = {'EXECUTABLE': 'DecoyDatabase',
                  'TextEditor.blu': 'bla', 'FILE': 'input.mzML', 'WORKDIR': '/some/where/',
                  'DecoyDatabase.out_delete_this_tag': 'fasta', 'DATABASE': 'database.fasta',
                  'ADDITIONAL_PARAMETERS': '-a 2 -b 3', 'use_as_key': 'FILE DATABASE', 'DecoyDatabase.append': 'True'}
        parameters = t.parse_parameters()
        golden_parameters = '-append -in database.fasta -out /some/where/database.fasta'
        golden_info = {'EXECUTABLE': 'DecoyDatabase',
                  'TextEditor.blu': 'bla', 'FILE': 'input.mzML', 'WORKDIR': '/some/where/',
                  'DecoyDatabase.out_delete_this_tag': 'fasta', 'DATABASE': '/some/where/database.fasta',
                  'ADDITIONAL_PARAMETERS': '-a 2 -b 3', 'use_as_key': 'FILE DATABASE', 'DecoyDatabase.append': 'True', 'outputParameter': '-out'}
        self.assertIn(golden_parameters, parameters)
        golden_and_merge_are_equal = True
        for key in golden_info:
            if(not t.info.get(key) or t.info[key] != golden_info[key]):
                golden_and_merge_are_equal = False
                #print key
                #print t.info.get(key)
                #print golden_info[key]
                break

        for key in t.info:
            if(not golden_info.get(key) or golden_info[key] != t.info[key]):
                golden_and_merge_are_equal = False
                #print key
                #print t.info[key]
                #print golden_info.get(key)
                break
        self.assertEquals(golden_and_merge_are_equal, True)


if __name__ == '__main__':
    unittest.main()
