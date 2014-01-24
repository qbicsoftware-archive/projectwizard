#!/usr/bin/env python
__author__ = 'wojnar'

from applicake.app import BasicApp
from applicake.coreutils.arguments import Argument
from applicake.coreutils.keys import Keys
import os

class R_Script(BasicApp):
    args = [Argument("EXECUTABLE", help='script that should be executed', default='--version'),
            Argument("Rscript", help='Rscript command',default='Rscript'),
            #Argument("in", help='input file for executable', default=''),
            #Argument("out", help='input file for executable', default=''),
            Argument('ADDITIONAL_PARAMETERS', help='additional parameter are forwarded to the executable, e.g. -threads 4', default=''),
            Argument('INPUT_OUTPUT_RELATIONSHIP', help='inputs and output can have many_to_one(merge) or one_to_one(transform, includes lists) relationships', default='transform')]

    def _setup_info(self):
        super(R_Script, self)._setup_info()
        self.name = os.path.basename(self.info['EXECUTABLE'])
        if '.R' in self.name:
            self.name = self.name.split('.')[0]
        self.info[Keys.NAME] += '_' + self.name


    def prepare_run(self):
        self.log.debug("Preparing")
        parameters = self.parse_parameters()
        self.command = self.info['Rscript'] + ' ' + self.info['EXECUTABLE'] + ' ' + parameters + ' ' + self.info['ADDITIONAL_PARAMETERS']

    def validate_run(self):
        super(R_Script, self).validate_run()
        self.info['ADDITIONAL_PARAMETERS'] = ''
        #if(self.info.get(Keys.WORKDIR)):
        #    del self.info[Keys.WORKDIR]
        #if self.info['out'] and not os.stat(self.info['out']).st_size > 0:
        #    raise RuntimeError("result file not found or empty")
    #TODO change delete into ignore
    #Assumes that parameters have the following format: self.info[NAME].someParameter = someValue
    #changes self.info['FILE'] to new result file
    def parse_parameters(self):
        parameters = ''
        for key in self.info:
            if "delete_this_tag" in key:
                continue
            if self.name in key:
                splitsplit = key.split('.')
                parameter = ''
                if len(splitsplit) > 1:
                    parameter = ''.join((' -', splitsplit[1]))
                option = self.info[key].strip()
                if (option == 'True'):
                    parameters += parameter
                elif (option != 'False'):
                    option_env = os.environ.get(option)
                    if (not option_env):
                        parameter = ''.join((parameter, ' ', option))
                    else:
                        parameter = ''.join((parameter, ' ', option_env))
                    parameters += parameter
        if self.info.get('FILE'):
            parameters = self._parse_input_output(parameters, self.name)
        return parameters

    def _parse_input_output(self, parameters, name):

        if isinstance(self.info['FILE'], list) and self.info['INPUT_OUTPUT_RELATIONSHIP'] == 'transform':
            in_files = ''
            out_files_lis = []
            out_files = ''
            for f in self.info['FILE']:
                in_files += ' ' + f
                out_file = self.info[Keys.WORKDIR] + os.path.basename(f) + '.' + name + '.' + self.info[name+'.out_delete_this_tag']
                out_files += ' ' + out_file
                out_files.append(out_file)
            parameters += ' ' + in_files
            self.info['FILE'] = out_files_lis
            parameters += ' ' + out_files

        elif isinstance(self.info['FILE'], list) and self.info['INPUT_OUTPUT_RELATIONSHIP'] == 'merge':
            in_files = ''
            for f in self.info['FILE']:
                in_files += ' ' + f
            out_file = self.info[Keys.WORKDIR] + os.path.basename(self.info['FILE'][0]) + '.' + name + '.' + self.info[name+'.out_delete_this_tag']
            parameters += ' ' + in_files
            self.info['FILE'] = out_file
            parameters += ' ' + out_file

        elif not isinstance(self.info['FILE'], list): # same for transform and merge
            parameters += ' ' + self.info['FILE']
            self.info['FILE'] = self.info[Keys.WORKDIR] + os.path.basename(self.info['FILE']) + '.' + name + '.' + self.info[name+'.out_delete_this_tag']
            parameters += ' ' + self.info['FILE']

        return parameters

#use this class as executable
if __name__ == "__main__":
    R_Script.run()