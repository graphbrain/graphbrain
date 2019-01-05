import graphbrain.parsers.stages.hypergen_case_generator as hypergen_cg


def run(params):
    infile = params['infile']
    outfile = params['outfile']
    hypergen_cg.generate_cases(infile, outfile)
