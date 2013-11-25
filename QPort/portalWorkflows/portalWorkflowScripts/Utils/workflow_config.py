import os
class workflow_config(object):

	input_file= 'workflow.input'
	parameter_file = 'parameter.input'
	tmp_folder = os.environ["TMP"]
	workflow_waste = "workflow.waste"
	workflow_output = "workflow.output"
	ref_genome = "Refgenome" 
	result_folder = tmp_folder#"/abi-projects/QBiC/GenomicsMG/"
	results_dropbox = os.environ["DROPBOX"]
	working_directory = "WORKINGDIRECTORY"

	tools = {'bwa aln' : 'bwa_align','bwa sampe':'bwa_sampe','fastqc':'fastqc','dummy':'dummy','sam-to-bam':'sam_to_bam','samtools index': 'samtools index','initiator':'initiator','mapping statistics': 'mapping statistics'}
