import graphbrain.parsers.stages.hypergen_case_generator as hypergen_cg


def run(params):
    outfile = params['outfile']
    lang = params['lang']
    hypergen_cg.interactive_edge_builder(outfile, lang=lang)
