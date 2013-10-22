#!/usr/bin/env python


import sys, os
tmpFolder = os.environ["TMP"] 
resultFolder = tmpFolder
OUTPUT = 'parameter.input' 
additionalInformation = []
dic = {} #dict()

if len(sys.argv) < 2:
	sys.stderr.write("usage: ./parameterHandler.py <input file> \n\n")
	sys.exit(1)

	
file_number = 0

for line in open(sys.argv[1], "r"):
	#print '****************************************************'
	#print line
	if (line.strip()):
		splittedLine = line.split('=')
		#print(splittedLine[0])
		#print(splittedLine[1])
		if(splittedLine[0].strip() == 'WORKFLOW' or splittedLine[0].strip()== 'SPACE' or splittedLine[0].strip()  == 'PROJECT' or splittedLine[0].strip() == 'COMMENT'or splittedLine[0].strip() == 'WORKINGDIRECTORY'):
			adI = ''.join((splittedLine[0].strip(),'=',splittedLine[1].strip()))
			additionalInformation.append(adI)
		else:
			splitsplit = splittedLine[0].split('.')
			#print splitsplit
			
			if(splitsplit[0] not in dic):
				dic[splitsplit[0]] = ''
			parameter = ''
			if(len(splitsplit) > 1 and "delete_this_tag" not in splitsplit[1]): 
				parameter = ''.join((' -',splitsplit[1]))
			option = splittedLine[1].strip()
			option_env = os.environ.get(option)
			if(not option_env):
				parameter = ''.join((parameter, ' ', option))
			else:
				parameter = ''.join((parameter, ' ', option_env))
			dic[splitsplit[0]] += parameter

try:
	with open(OUTPUT, 'w') as output:
		for key in dic: #per definition it iterates through keys!
			parameterLine = ''.join((key.strip(),'= ',dic[key])) 
			output.write(parameterLine)
			output.write("\n")
		for addI in additionalInformation:
			output.write(addI)
			output.write("\n") 
except IOError as e:
	print 'Unable to save parameters'

#print dic
#print additionalInformation
			
	
