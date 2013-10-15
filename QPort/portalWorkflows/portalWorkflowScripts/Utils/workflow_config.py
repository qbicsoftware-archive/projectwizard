import os
class workflow_config(object):

	input_file= 'workflow.input'
	parameter_file = 'parameter.input'
	tmp_folder = os.environ["TMP"]
	workflow_waste = "workflow.waste"
	workflow_output = "workflow.output"
	ref_genome = "Refgenome" 
	result_folder = tmp_folder#"/abi-projects/QBiC/GenomicsMG/"

	tools = {'bwa aln' : 'bwa_align','bwa sampe':'bwa_sampe','fastqc':'fastqc','dummy':'dummy','sam-to-bam':'sam-to-bam-converter','samtools index': 'samtools index'}
