import os
import sys
import csv
import time
import datetime
from PyRTFloc import *

ts = time.time()
st = datetime.datetime.fromtimestamp(ts).strftime('_%Y%b%d')

pathname = os.path.dirname(sys.argv[0])

PRJ = sys.argv[3][1:5]
BASEDIR=pathname+"/"+PRJ
png_path=BASEDIR+"/barcodes/png"
sheet_path=BASEDIR+"/documents/sample_sheets"
outfile=sheet_path+"/sample_sheet_"+PRJ+st

os.system('mkdir -p '+sheet_path)

def MakeDoc() :
	doc     = Document()
	ss      = doc.StyleSheet
	section = Section()
	doc.Sections.append( section )

	#	text can be added directly to the section
	#	a paragraph object is create as needed

	ts = time.time()
	st = datetime.datetime.fromtimestamp(ts).strftime('%d %b, %Y')
	section.append( 'Sample sheet QBiC project: ' + PRJ + " "+st)
	thin_edge  = BorderPS( width=20, style=BorderPS.DOUBLE )
	thin_frame  = FramePS( thin_edge,  thin_edge,  thin_edge,  thin_edge )
	no_edge = BorderPS( width=1, style=BorderPS.HAIRLINE )
	no_frame = FramePS ( thin_edge, no_edge, no_edge, no_edge)
	TabPS.DEFAULT_WIDTH = 800
	table = Table( TabPS.DEFAULT_WIDTH * 4, TabPS.DEFAULT_WIDTH * 3, TabPS.DEFAULT_WIDTH * 3, TabPS.DEFAULT_WIDTH * 2 )
	c1 = Cell( Paragraph( 'Barcode' ), thin_frame )
	c2 = Cell( Paragraph( 'Treatment' ), thin_frame )
	c3 = Cell( Paragraph( 'Derived From' ), thin_frame )
	c4 = Cell( Paragraph( 'Notes' ), thin_frame )
	table.AddRow(c1, c2, c3, c4)
	#table.AddRow(c2,c3,c4)
	i = -1
	for name in sample_name:
		i+=1
		section.append(table)
		table = Table (TabPS.DEFAULT_WIDTH * 12)
		if(name in cats):
			c1 = Cell( Paragraph( cats[name] ), thin_frame ) 
			table.AddRow(c1)
		image = Image( png_path+'/'+ name +'.png' , width=2*72)
		c1 = Cell( Paragraph( image ), thin_frame ) 
		section.append(table)
		table = Table( TabPS.DEFAULT_WIDTH * 4, TabPS.DEFAULT_WIDTH * 3, TabPS.DEFAULT_WIDTH * 3, TabPS.DEFAULT_WIDTH * 2 )
		c2 = Cell(Paragraph (description[i]), thin_frame )
		c3 = Cell(Paragraph (derived[i]), thin_frame )
		c4 = Cell(Paragraph (str("")), thin_frame )
		table.AddRow(c1, c2, c3, c4)
	section.append(table)
	return doc

def OpenFile( name ) :
	return file( '%s.rtf' % name, 'w' )

sample_name=[]
description=[]
derived=[]
cats={}
cat = False
currCat = ""
i = 0
for word in sys.argv[1:]:
	if(word.startswith("-c")):
		cat = True
	elif(cat):
		cat = False
		currCat = word
	elif(i==0):
		word = word.strip()
		if(currCat!=""):
			cats[word] = currCat
			currCat=""
		sample_name.append(word)
		i+=1
	elif(i==1):
		description.append(word)
		i+=1
	else:
		derived.append(word)
		i=0

DR = Renderer()

doc1 = MakeDoc()

DR.Write( doc1, OpenFile( outfile ) )

os.system("mv "+outfile+".rtf "+outfile+".doc")

print "\nDone. Sample sheet created in: "+outfile+"\n";

