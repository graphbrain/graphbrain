from os import listdir
from os.path import isfile, join
import json
from termcolor import colored
from graphbrain import build_atom
from graphbrain.cli import wrapper
from graphbrain.parsers import create_parser


# '.' here means that the atom should be discarded
ATOM_TYPES = ['C', 'P', 'M', 'B', 'J', 'T', 'X']


TYPE_COLOR = {
    'C': 'blue',
    'P': 'red',
    'M': 'cyan',
    'B': 'green',
    'J': 'yellow',
    'T': 'magenta',
    'X': ''
}


def colored_type(atom_type, full_string=None):
    color = TYPE_COLOR[atom_type[0]]
    if color == '':
        return atom_type
    else:
        return colored(atom_type, color)


def with_type_color(atom_type, text):
    color = TYPE_COLOR[atom_type[0]]
    if color == '':
        return atom_type
    else:
        return colored(text, color)


def colored_role(atom):
    role = atom.role()
    crole = [colored_type(atom.type())]
    crole += role[1:]
    return '.'.join(crole)


def colored_atom(atom):
    parts = [with_type_color(atom.type(), atom.root()), colored_role(atom)]
    parts += atom.parts()[2:]
    return '/'.join(parts)


def colored_edge(edge):
    if edge.is_atom():
        return colored_atom(edge)
    else:
        return '({})'.format(
            ' '.join([colored_edge(subedge) for subedge in edge]))


def load_sentences(file_path):
    sentences = set()
    try:
        with open(file_path, 'r') as f:
            for line in f.readlines():
                case = json.loads(line)
                sentences.add(case['sentence'])
    except FileNotFoundError:
        pass
    return sentences


class TrainingDataGenerator:
    def __init__(self, lang):
        self.parser = create_parser(name=lang)
        self.sentence = None
        self.source = None
        self.atoms = None
        self.token2atom = None
        self.input_files = None

    def print_status(self):
        print()
        print('{} {}'.format(colored('sentence .. > ', 'blue'),
                             colored(self.sentence, 'white')))
        tokens = ' '.join([colored_atom(atom) for atom in self.atoms])
        print('{} {}'.format(colored('tokens .... > ', 'blue'), tokens))
        print('{} {}'.format(colored('source .... > ', 'blue'), self.source))
        print()

    def annotate_token(self, token):
        token_type = self.parser.token2type(token)
        if token_type is None:
            suggested_type = 'X'
        else:
            suggested_type = token_type[0]

        atom_type = None
        while atom_type is None:
            self.print_status()
            prompt_msg = '{} ({}) [{}] ? '.format(
                colored(str(token), 'white'),
                '/'.join([colored_type(atype) for atype in ATOM_TYPES]),
                colored_type(suggested_type))
            atom_type = input(prompt_msg).upper()
            if atom_type == '':
                atom_type = suggested_type
            elif atom_type not in ATOM_TYPES:
                print(colored('Invalid type. Please try again.', 'red'))
                atom_type = None
        atom = build_atom(str(token), atom_type)
        if atom_type != 'X':
            self.token2atom[token] = atom
        return atom

    def edge_correct(self, edge):
        correct = None
        while correct is None:
            print()
            print('Parsed edge:')
            print(colored_edge(edge))
            yes = colored('y', 'green')
            no = colored('n', 'red')
            answer = input(
                'is edge parse correct ({}/{}) [{}] ? '.format(yes, no, yes))
            if answer in {'', 'y'}:
                correct = True
            elif answer == 'n':
                correct = False
            else:
                print(colored('Invalid answer. Please try again.', 'red'))
        return correct

    def accept_ignore_restart(self):
        while True:
            print()
            accept = colored('a', 'green')
            reject = colored('i', 'red')
            restart = colored('r', 'yellow')
            answer = input(
                'accept, ignore or restart ({}/{}/{}) ? '.format(
                    accept, reject, restart))
            if answer in {'a', 'i', 'r'}:
                return answer
            else:
                print(colored('Invalid answer. Please try again.', 'red'))

    def annotate_sentence(self, sentence, source):
        self.sentence = sentence
        self.source = source
        self.atoms = []
        self.token2atom = {}
        parse_results = self.parser.parse(sentence)
        parse = parse_results['parses'][0]
        spacy_sentence = parse['spacy_sentence']
        for token in spacy_sentence:
            self.atoms.append(self.annotate_token(token))

        self.parser.reset(sentence)
        atom_seq = self.parser.build_atom_sequence(
            spacy_sentence, self.token2atom)
        parse = self.parser.parse_spacy_sentence(spacy_sentence, atom_seq)

        edge = parse['main_edge']
        correct = self.edge_correct(edge)

        air = self.accept_ignore_restart()
        ignore = True
        if air == 'a':
            ignore = False
        elif air == 'r':
            return None

        return {'sentence': sentence,
                'source': self.source,
                'atoms': list(atom.to_str() for atom in self.atoms),
                'hyperedge': edge.to_str(),
                'correct': correct,
                'ignore': ignore}

    def _open_input_files(self, indir):
        self.input_files = {}

        file_names = [f for f in listdir(indir) if isfile(join(indir, f))]

        for file_name in file_names:
            name = file_name.split('.')[0]
            self.input_files[name] = open(join(indir, file_name), 'r')

    def _close_input_files(self):
        for name in self.input_files:
            self.input_files[name].close()

    def _sentences(self):
        for name in self.input_files:
            f = self.input_files[name]
            yield f.readline(), name

    def generate(self, indir, outfile):
        self._open_input_files(indir)

        sentences = load_sentences(outfile)

        with open(outfile, 'a') as outfile:
            for sentence, source in self._sentences():
                sentence = sentence.strip()
                if sentence not in sentences:
                    case = None
                    while case is None:
                        case = self.annotate_sentence(sentence, source)
                    outfile.write('{}\n'.format(json.dumps(case)))
                    sentences.add(sentence)

        self._close_input_files()


def generate_parser_training_data(args):
    TrainingDataGenerator(args.lang).generate(args.indir, args.outfile)


if __name__ == '__main__':
    wrapper(
        generate_parser_training_data, text='generate parser training data')
