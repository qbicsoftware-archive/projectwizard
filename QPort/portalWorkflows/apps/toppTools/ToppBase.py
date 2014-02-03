#!/usr/bin/env python
__author__ = 'wojnar'
from applicake.app import BasicApp
from applicake.coreutils.arguments import Argument
from applicake.coreutils.keys import Keys
import os


class ToppBase(BasicApp):

    class keys:
        EXECUTABLE = "EXECUTABLE"
        FILE = "FILE"
        ADDITIONAL_PARAMETERS = "ADDITIONAL_PARAMETERS"
        OUTPUTPARAMETER = "outputParameter"
        use_as_key = "use_as_key"
        INPUT_OUTPUT_RELATIONSHIP = "INPUT_OUTPUT_RELATIONSHIP"

    args = [Argument(keys.EXECUTABLE, default='OpenMSInfo'),
            #Argument("in", help='input file for executable', default=''),
            #Argument("out", help='input file for executable', default=''),
            Argument(keys.ADDITIONAL_PARAMETERS, help='additional parameter for the executable are forwarded here, e.g. -threads 4', default=''),
            Argument(keys.INPUT_OUTPUT_RELATIONSHIP, help='inputs and output can have many_to_one(merge) or one_to_one(transform, includes lists) relationships', default='transform'),
            Argument(keys.use_as_key, help="""use_as_key key1 key2. For parameter handling and value of key2
                                            will be used as it would be the value of key1. Be aware that FILE someKey
                                            means. That the value of someKey will be used as the value of FILE. Also,
                                            the output value will be written to FILE.""", default='')]

    def _setup_info(self):
        super(ToppBase, self)._setup_info()
        self.info[Keys.NAME] += '_' + self.info[self.keys.EXECUTABLE]

    def prepare_run(self):
        self.log.debug("Preparing")
        parameters = self.parse_parameters()
        self.command = self.info[self.keys.EXECUTABLE] + ' ' + parameters + ' ' + self.info[self.keys.ADDITIONAL_PARAMETERS]

    def validate_run(self):
        super(ToppBase, self).validate_run()
        self.info[self.keys.ADDITIONAL_PARAMETERS] = ''
        del self.info[self.keys.OUTPUTPARAMETER]
        #if(self.info.get(Keys.WORKDIR)):
        #    del self.info[Keys.WORKDIR]
        #if self.info['out'] and not os.stat(self.info['out']).st_size > 0:
        #    raise RuntimeError("result file not found or empty")
    #TODO change delete into ignore
    def parse_parameters(self):
        """
        Assumes that parameters have the following format: self.info[NAME].someParameter = someValue
        changes self.info[self.keys.FILE] to new result file or other key if use_as_key parameter in use
        """
        parameters = ''
        executable_name = self.info[self.keys.EXECUTABLE]
        self.info[self.keys.OUTPUTPARAMETER] = '-out'
        use_as_key_dic = {}
        info_backup = {}
        if self.info.get(self.keys.use_as_key):
            use_as_key = self.info[self.keys.use_as_key]
            use_as_key_lis = use_as_key.split(' ')
            nu_use_as_key = len(use_as_key_lis)
            if not use_as_key or nu_use_as_key % 2 != 0:
                raise KeyError('number of original and replace keys does not match: ' + str(use_as_key_lis))
            info_backup = self.info.copy()
            for i in xrange(0, nu_use_as_key, 2):
                self.info[use_as_key_lis[i]] = self.info[use_as_key_lis[i+1]]
                use_as_key_dic[use_as_key_lis[i]] = use_as_key_lis[i+1]

        for key in self.info:
            if "delete_this_tag" in key:
                continue
            if executable_name in key:
                splitsplit = key.split('.')
                parameter = ''
                if len(splitsplit) > 1 and splitsplit[1] == self.keys.OUTPUTPARAMETER:
                    self.info[self.keys.OUTPUTPARAMETER] = self.info[key].strip()
                    continue
                if len(splitsplit) > 1:
                    p = splitsplit[1]
                    parameter = ''.join((' -', p))
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
        for key in use_as_key_dic:
            if key in info_backup:
                self.info[key] = info_backup[key]
        if self.keys.FILE in use_as_key_dic:
            self.keys.FILE = use_as_key_dic[self.keys.FILE]
        if self.info.get(self.keys.FILE):
            parameters = self._parse_input_output(parameters, executable_name)
        return parameters

    def _parse_input_output(self, parameters, name):
        out = ' ' + self.info[self.keys.OUTPUTPARAMETER] + ' '
        outputExtension = ''
        if name+'.out_delete_this_tag' in self.info:
            outputExtension = '.' + self.info[name+'.out_delete_this_tag']
        else:
            if isinstance(self.info[self.keys.FILE], list):
                outputExtension = os.path.splitext(os.path.basename(self.info[self.keys.FILE][0]))[1]
            else:
                outputExtension = os.path.splitext(os.path.basename(self.info[self.keys.FILE]))[1]


        if isinstance(self.info[self.keys.FILE], list) and \
                      self.info[self.keys.INPUT_OUTPUT_RELATIONSHIP] == 'transform':
            in_files = ''
            out_files_lis = []
            out_files = ''
            for f in self.info[self.keys.FILE]:
                in_files += ' ' + f
                out_file = self.info[Keys.WORKDIR] + os.path.splitext(os.path.basename(f))[0] + outputExtension
                out_files += ' ' + out_file
                out_files_lis.append(out_file)
            parameters += ' -in ' + in_files
            self.info[self.keys.FILE] = out_files_lis
            parameters += out + out_files

        elif isinstance(self.info[self.keys.FILE], list) and self.info[self.keys.INPUT_OUTPUT_RELATIONSHIP] == 'merge':
            in_files = ''
            for f in self.info[self.keys.FILE]:
                in_files += ' ' + f
            out_file = self.info[Keys.WORKDIR] + os.path.splitext(os.path.basename(self.info[self.keys.FILE][0]))[0] + outputExtension
            parameters += ' -in ' + in_files
            self.info[self.keys.FILE] = out_file
            parameters += out + out_file

        elif not isinstance(self.info[self.keys.FILE], list):  # same for transform and merge
            parameters += ' -in ' + self.info[self.keys.FILE]
            self.info[self.keys.FILE] = self.info[Keys.WORKDIR] + os.path.splitext(os.path.basename(self.info[self.keys.FILE]))[0] + outputExtension
            parameters += out + self.info[self.keys.FILE]

        return parameters

#use this class as executable
if __name__ == "__main__":
    ToppBase.run()