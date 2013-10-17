import os
from Wrapper.Bwa import Bwa
from Factory.IFactory import IFactory

class BwaSampe(Bwa):
        def __init__(self,program,tool):
                Bwa.__init__(self,program,tool)
                self.hasReferenceGenome = True
                self.outputFileParameter = '-f'

        def createOutputFileName(self):
                if(self.input):
                        path,file_name = os.path.split(self.input[0])
                        file_name = file_name[0:15]
                        output_file_name = file_name.rstrip() + ".sam"
                        self.createOutputFilePath(path, output_file_name)
        class Factory(IFactory):
                def create(self): return BwaSampe(self.program,self.tool)
