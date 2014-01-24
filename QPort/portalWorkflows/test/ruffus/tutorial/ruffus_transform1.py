from ruffus import *

first_task_params = ['job1.input',
										 'job2.input',
										 'job3.input'
										]
for input_file in first_task_params:
#make sure the input file is there
	open(input_file, 'w')


@transform(first_task_params,suffix('.input'), '.output1','some_extra.string.for_example',14)
def first_task(input_file, output_file, extra_parameter_str,extra_parameter_num):
	open(output_file,'w')

@transform(first_task, suffix('.output1'),'.output2')
def second_task(input_file,output_file):
	open(output_file,'w')

open("job1.output1","w")
pipeline_run([second_task],verbose=2,multiprocess = 4)
