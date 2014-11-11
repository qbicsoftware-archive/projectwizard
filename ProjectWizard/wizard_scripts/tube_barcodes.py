# creates *.eps and *pdf barcodes from input file sample_ids.tsv
# the .eps files include the sample IDs+checksum and the actual barcode
# the .pdf files include the "QBIC Dr.Sven Nahnsen etc" and 
# are ready to print with the label printer: 26x18mm

#Thu Sep 05, 2013 

import sys
import os
import csv
import math

#assumes the following folders exist
#TO DO: create if not exists

pathname = os.path.dirname(sys.argv[0])
PRJ = sys.argv[1][1:5]
BASEDIR = pathname+"/"+PRJ
pdfdir = BASEDIR+"/barcodes/pdf"

path = pathname+"/barcodes/"
tmp = path+'tmp/'
pdf = path+'pdf/'
eps = path+'eps/'
os.system('mkdir -p '+tmp)
os.system('mkdir -p '+pdf)
os.system('mkdir -p '+eps)
os.system('mkdir -p '+pdfdir)


# this function attaches variable barcode information to the
# standard "blank" barcode ps-script, so barcodes can be created
def create_barcode(name):
	pscode = open(tmp+"var.ps","w")
	pscode.write("15 15 moveto ("+name+") () /qrcode /uk.co.terryburton.bwipp findresource exec\n")
	pscode.write("showpage")
	pscode.close()
	os.system('cat '+path+'barcode.ps '+tmp+'var.ps > '+tmp+name+'.ps')
	os.system('ps2eps -f '+tmp+name+'.ps')

def create_barcodes(sample_names,sTypes,species):
	print sample_names
	print sTypes
	print species
	for i in range(len(sTypes)):
		sTypes[i] = sTypes[i].replace("_","\_")
	for i in range(len(sample_names)):
		name = sample_names[i].strip()
		create_barcode(name)
		cSpecies = species[i]
		cType = sTypes[i]
		tex = open(tmp+name+".tex","w")
		print name
		print cSpecies
		print cType
		tex.write('''\documentclass[a4paper]{article}
\usepackage{graphicx}
 \usepackage[paperwidth=39mm,paperheight=9.8mm]{geometry}
 \\begin{document}
 \hoffset=-5.0mm
\\thispagestyle{empty} 
 \\begin{table} [ht] 
  \\begin{tabular}{l}
	  \\begin{minipage}[t]{40mm}  
	  \\begin{center} 
	  \\begingroup
		\\renewcommand*\\rmdefault{arial}
		\\ttfamily
		    \\fontsize{8pt}{11pt}\selectfont
		\\vskip -0.36cm
               \\hskip -0.12cm
		    '''+name+'''
	   \endgroup
	   \end{center} 
	\\vskip -0.35cm
	 \\begin{tabular}{p{9mm}p{17mm}p{9mm}}
	\\hskip -0.1cm
		\includegraphics[width=.62cm, height=.62cm]{''' +tmp+name+ '''.eps} & 
	 
			  \\begingroup
				\\renewcommand*\\rmdefault{arial}
				\\ttfamily
				    \\fontsize{5pt}{8pt}\selectfont
				\\vskip -0.65cm
				 \\hskip -0.7cm
				 \\begin{minipage}[t]{1.4cm}  
				   ''' +cSpecies+ '''\\vskip -0.12cm ''' +cType+ '''
				 \end{minipage}
	   		  \endgroup   
	               \\begingroup
				\\renewcommand*\\rmdefault{arial}
				\\ttfamily
				    \\fontsize{4pt}{6pt}\selectfont
				\\vskip -0.06cm
				 \\hskip -0.4cm
				 \\begin{minipage}[t]{1cm}  
				   QBiC-Dr.Nahnsen \\vskip -0.05cm  +4970712972163 
				 \end{minipage}
			  \endgroup   &
\\hskip -0.8cm
	 \includegraphics[width=.62cm, height=.62cm]{'''+tmp+name+'''.eps} 
	    \end{tabular}
 
        \end{minipage}
 
 
 \end{tabular}
 \end{table}

\end{document}\n''')
		tex.close()
		os.system("pdflatex -shell-escape -output-directory="+tmp+" "+tmp+name+".tex")
		os.system("mv "+tmp+name+".pdf " +pdfdir)
	print "done"

def old_create_barcodes(sample_names,sTypes,species):
	print sample_names
	print sTypes
	print species
	for i in range(len(sTypes)):
		sTypes[i] = sTypes[i].replace("_","\_")
	for i in range(len(sample_names)):
		name = sample_names[i].strip()
		currentSet = [name]
		create_barcode(name)
		cSpecies = [species[i]]
		cType = [sTypes[i]]
	#half = int(math.ceil(len(sample_names)/2.0))
	#x = 0
	#for i in range(half):
	#	currentSet = []
	#	cType = []
	#	cSpecies = []
	#	for n in range(2):
	#		try:
	#			name = sample_names[x+n].strip()
	#			currentSet.append(name)
	#			create_barcode(name)
	#			cSpecies.append(species[x+n])
	#			cType.append(sTypes[x+n])
	#		except:
	#			currentSet.append(currentSet[0]) #if there is no 2nd sample, print 1st twice
	#			cSpecies.append(cSpecies[0])
	#			cType.append(cType[0])
	#	x+=2
		tex = open(tmp+currentSet[0]+".tex","w")
		tex.write('''\documentclass[a4paper]{article}
\usepackage{graphicx}
 \usepackage[paperwidth=27mm,paperheight=19mm]{geometry}
 \\begin{document}
 \hoffset=-5.0mm
\\thispagestyle{empty} 
 \\begin{table} [ht] 
  \\begin{tabular}{l}
	  \\begin{minipage}[b]{2.7cm}  
	  \\begin{center} 
	  \\begingroup
		\\renewcommand*\\rmdefault{arial}
		\\ttfamily
		    \\fontsize{7pt}{10pt}\selectfont
		\\vskip -0.16cm
		    '''+currentSet[0]+'''
	   \endgroup
	   \end{center} 
	\\vskip -0.35cm
	 \\begin{tabular}{p{9mm}p{17mm}}
	\\hskip -0.1cm
		\includegraphics[width=.62cm, height=.62cm]{'''+tmp+currentSet[0]+'''.eps} & 
	 
			  \\begingroup
				\\renewcommand*\\rmdefault{arial}
				\\ttfamily
				    \\fontsize{5pt}{8pt}\selectfont
				\\vskip -0.6cm
				 \\hskip -0.4cm
				 \\begin{minipage}[t]{1cm}  
				   '''+cType[0]+''' \\vskip -0.12cm '''+cSpecies[0]+'''
				 \end{minipage}
	   		  \endgroup   
	               \\begingroup
				\\renewcommand*\\rmdefault{arial}
				\\ttfamily
				    \\fontsize{4pt}{6pt}\selectfont
				\\vskip -0.06cm
				 \\hskip -0.7cm
				 \\begin{minipage}[t]{1cm}  
				   QBiC-Dr.Nahnsen \\vskip -0.05cm  +4970712972163 
				 \end{minipage}
			  \endgroup   
	 
	    \end{tabular}
 
        \end{minipage}
 
 
 \end{tabular}



  \\begin{tabular}{l}
	  \\begin{minipage}[b]{2.7cm}  
	  \\begin{center} 
	  \\begingroup
		\\renewcommand*\\rmdefault{arial}
		\\ttfamily
		    \\fontsize{7pt}{10pt}\selectfont
		\\vskip -0.36cm
		    %'''+currentSet[0]+'''
	   \endgroup
	   \end{center} 
	\\vskip -0.35cm
	 \\begin{tabular}{p{9mm}p{17mm}}
	\\hskip -0.1cm
		%\includegraphics[width=.62cm, height=.62cm]{'''+tmp+currentSet[0]+'''.eps} & 
	 
			  \\begingroup
				\\renewcommand*\\rmdefault{arial}
				\\ttfamily
				    \\fontsize{5pt}{8pt}\selectfont
				\\vskip -0.6cm
				 \\hskip -0.4cm
				 \\begin{minipage}[t]{1cm}  
				%   '''+cType[0]+''' \\vskip -0.12cm '''+cSpecies[0]+'''
				 \end{minipage}
	   		  \endgroup   
	               \\begingroup
				\\renewcommand*\\rmdefault{arial}
				\\ttfamily
				    \\fontsize{4pt}{6pt}\selectfont
				\\vskip -0.06cm
				 \\hskip -0.7cm
				 \\begin{minipage}[t]{1cm}  
				%   QBiC-Dr.Nahnsen \\vskip -0.05cm  +4970712972163 
				 \end{minipage}
			  \endgroup   
	 
	    \end{tabular}
 
        \end{minipage}
 
 
 \end{tabular}
 \end{table}
\end{document}\n''')
		tex.close()
		os.system("pdflatex -shell-escape -output-directory="+tmp+" "+tmp+name+".tex")
		os.system("mv "+tmp+currentSet[0]+".pdf " +pdfdir)
		#os.system("mv "+tmp+currentSet[0]+".eps " +eps)

	#os.system("rm "+tmp+"*")
	print "done"

samples = []
sTypes = []
species = []

n = len(sys.argv[1:])/3
for arg in sys.argv[1:n+1]:
	samples.append(arg)
for arg in sys.argv[n+1:2*n+1]:
	sTypes.append(arg)
for arg in sys.argv[2*n+1:]:
	species.append(arg)
create_barcodes(samples,sTypes,species)
