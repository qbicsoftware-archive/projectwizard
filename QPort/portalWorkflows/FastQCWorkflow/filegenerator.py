#!/usr/bin/env python

# usage
#   ./filegenerator.py <input file> <output file base name>
# for every line in the input file, this script will generate a file using the provided base name
# example
# ./filegenerator.py input.txt output.txt
# where input.txt is a text file with the following content:
####### BEGIN
# one
# two
# three
# four five six
####### END
# will generate the following files with the given content:
# output.txt_0 - one
# output.txt_1 - two
# output.txt_2 - three
# output.txt_3 - four five six

import sys

if len(sys.argv) < 3:
	sys.stderr.write("usage: ./filegenerator.py <input file> <output file base name>\n\n")
	sys.exit(1)
	
file_number = 0
for line in open(sys.argv[1], "r"):
	output = open(sys.argv[2] + '_' + str(file_number), 'w')
	output.write(line)
	output.close()
	file_number += 1
	
print("Generated " + str(file_number) + " files.")