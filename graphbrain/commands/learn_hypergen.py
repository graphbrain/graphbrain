import graphbrain.parsers.stages.hypergen as hypergen


def run(params):
    infile = params['infile']
    model_type = params['model_type']
    outfile = params['outfile']
    hypergen.learn(infile, model_type=model_type, outfile=outfile)
