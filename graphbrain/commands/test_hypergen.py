import graphbrain.parsers.stages.hypergen as hypergen


def run(params):
    infile = params['infile']
    model_type = params['model_type']
    model_file = params['model_file']
    hypergen.test(infile, model_type=model_type, model_file=model_file)
