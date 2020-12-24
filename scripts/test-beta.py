import json
from graphbrain import hedge, build_atom
from graphbrain.cli import wrapper
from graphbrain.parsers import create_parser, print_tree


def simple_edge(edge):
    if edge.is_atom():
        roles = [edge.type()[0]]
        root = edge.root()
        argroles = edge.argroles()
        if argroles:
            roles.append(argroles)
        return build_atom(root, '.'.join(roles))
    else:
        return hedge([simple_edge(subedge) for subedge in edge])


def test_beta(args):
    parser = create_parser(name=args.lang)

    with open(args.infile) as f:
        for line in f:
            case = json.loads(line)
            if not case['ignore']:
                sentence = case['sentence']
                tedge = simple_edge(hedge(case['hyperedge']))
                source = case['source']
                correct = case['correct']

                parser_output = parser.parse(sentence)
                parsed_sentence = parser_output['parses'][0]
                pedge = simple_edge(parsed_sentence['main_edge'])
                sent = parsed_sentence['spacy_sentence']

                print()
                print()
                print(sentence)
                print(tedge)
                print(pedge)
                print(source)
                print(correct)
                print_tree(sent.root)


if __name__ == '__main__':
    wrapper(test_beta, text='test beta stage output')
