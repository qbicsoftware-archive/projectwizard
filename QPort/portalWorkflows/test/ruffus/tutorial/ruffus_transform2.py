from ruffus import *

#---------------------------------------------------------------
#   Create pairs of input files
#
first_task_params = [
                     ['job1.a.input', 'job1.b.input'],
                     ['job2.a.input', 'job2.b.input'],
                     ['job3.a.input', 'job3.b.input'],
                    ]

for input_file_pairs in first_task_params:
    for input_file in input_file_pairs:
        open(input_file, "w")


#---------------------------------------------------------------
#
#   first task
#
@transform(first_task_params, suffix(".input"),
                        [".output.1",
                         ".output.extra.1"],
                       "some_extra.string.for_example", 14)
def first_task(input_files, output_file_pairs,
                extra_parameter_str, extra_parameter_num):
    # make both pairs of output files
    for output_file in output_file_pairs:
        open(output_file, "w")


#---------------------------------------------------------------
#
#   second task
#
@transform(first_task, suffix(".output.1"), ".output2")
def second_task(input_files, output_file):
    # make output file
    open(output_file, "w")

#---------------------------------------------------------------
#
#       Run
#
pipeline_run([second_task])
