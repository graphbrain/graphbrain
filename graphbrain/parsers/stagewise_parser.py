import logging
from graphbrain.funs import *
from graphbrain.nlp.parser import Parser
from graphbrain.sense.disambiguation import Disambiguation
from graphbrain.parsers.stages import *


class StagewiseParser(object):
    def __init__(self, hg=None, stages=('hypergen-forest', 'disamb-naive', 'merge', 'shallow', 'concepts'),
                 show_namespaces=False, lang='en', model_file=None):
        self.hg = hg
        self.stages = stages
        self.parser = None
        self.disamb = None
        self.debug = False
        self.aux_text = ''
        self.show_namespaces = show_namespaces
        self.lang = lang
        self.model_file = model_file

    def create_stage(self, name, output):
        if name == 'hypergen-forest':
            return Hypergen(model_type='rf', model_file=self.model_file)
        elif name == 'hypergen-nn':
            return Hypergen(model_type='nn', model_file=self.model_file)
        elif name == 'disamb':
            return Disamb(self.hg, self.parser, self.disamb, output, self.aux_text, lang=self.lang)
        elif name == 'disamb-simple':
            return DisambSimple(output)
        elif name == 'disamb-naive':
            return DisambNaive(output)
        elif name == 'merge':
            return Merge(output, lang=self.lang)
        elif name == 'shallow':
            return Shallow(output, lang=self.lang)
        elif name == 'concepts':
            return Concepts(output, lang=self.lang)
        else:
            raise RuntimeError('unknnown stage name: %s' % name)

    def debug_msg(self, msg):
        logging.info(msg)
        if self.debug:
            print(msg)

    def read_text(self, text, aux_text=None, reset_context=True):
        if self.parser is None:
            self.debug_msg('creating parser...')
            self.parser = Parser(lang=self.lang)
            self.disamb = Disambiguation(self.hg, self.parser)
        nlp_parses = self.parser.parse_text(text.strip())
        if reset_context:
            self.aux_text = text
            if aux_text:
                self.aux_text = '%s\n%s' % (text, aux_text)

        parses = [(p[0], self.read_sentence(p[1])) for p in nlp_parses]

        for p in parses:
            self.debug_msg('== extra ==')
            for edg in p[1].edges:
                self.debug_msg(edge2str(edg))

        return parses

    def read_sentence(self, sentence):
        self.debug_msg('parsing sentence: %s' % sentence)
        if self.debug:
            sentence.print_tree()

        last_stage_output = None
        stage_outputs = []
        first = True
        for name in self.stages:
            stage = self.create_stage(name, last_stage_output)
            self.debug_msg('executing %s stage...' % name)
            if first:
                last_stage_output = stage.process_sentence(sentence)
                first = False
            else:
                last_stage_output = stage.process()
            output = last_stage_output.tree.to_hyperedge(with_namespaces=self.show_namespaces)
            stage_outputs.append(output)
            self.debug_msg(edge2str(output))

        last_stage_output.main_edge = last_stage_output.tree.to_hyperedge()

        # TODO: ugly...
        last_stage_output.tree = None

        output = last_stage_output
        output.stage_outputs = stage_outputs

        return output
