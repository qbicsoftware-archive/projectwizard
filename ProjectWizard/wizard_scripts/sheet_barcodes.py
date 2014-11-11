import os
import sys
import subprocess

pathname = os.path.dirname(sys.argv[0])

PRJ = sys.argv[1][1:5]
bcpath = pathname+"/barcodes/"
tmp = bcpath+'tmp/'
BASEDIR=pathname+"/"+PRJ
png_path=BASEDIR+"/barcodes/png"
os.system('mkdir -p '+png_path)

def create_barcode(name):
	pscode = open(tmp+"var.ps","w")
	pscode.write("15 15 moveto ("+name+") (includecheck includetext) /code128 /uk.co.terryburton.bwipp findresource exec\n")
	#  pscode.write("15 15 moveto ("+name+") /qrcode /uk.co.terryburton.bwipp findresource exec\n")
	#pscode.write("0 0 rmoveto (Code 128) show")
	pscode.write("showpage")
	pscode.close()
	os.system('cat '+bcpath+'blank.ps '+tmp+'var.ps > '+tmp+name+'.ps')
	#os.system('gs -r300 -dTextAlphaBits=4 -sDEVICE=png16m -sOutputFile='+tmp+name+'.png -dBATCH -dNOPAUSE -dDEVICEWIDTHPOINTS=55 '
	#'-dDEVICEHEIGHTPOINTS=30 '+tmp+name+'.ps')
	os.system('ps2eps -f '+tmp+name+'.ps')
	os.system('eps2png -h 2.3cm -w 5cm '+tmp+name+'.eps ')
	os.system("mv "+tmp+name+".png "+png_path)

for name in sys.argv[1:]:
	create_barcode(name)