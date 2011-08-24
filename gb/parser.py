import nltk
import sys


def parse_orig(tokens, start, end):
    phrase = ''
    pos = start
    first = True
    while pos < end:
        if first:
            first = False
        else:
            phrase += ' '
        phrase += tokens[pos][0]
        pos += 1

    return phrase


def parse_rel(tokens, start, end):
    phrase = ''
    pos = start
    first = True
    while pos < end:
        if first:
            first = False
        else:
            phrase += ' '
        phrase += tokens[pos][0]
        pos += 1

    return phrase


def parse_targ(tokens, start, end):
    phrase = ''
    pos = start
    first = True
    while pos < end:
        if first:
            first = False
        else:
            phrase += ' '
        phrase += tokens[pos][0]
        pos += 1

    return phrase


def parse(sentence):
    tokens = nltk.word_tokenize(sentence)
    tagged_tokens = nltk.pos_tag(tokens)

    print tagged_tokens

    rel_index = -1
    targ_index = -1

    pos = 0

    while (pos < len(tagged_tokens)) and (rel_index <= 0):
        if tagged_tokens[pos][1][:2] == 'VB':
            rel_index = pos
        pos += 1

    while (pos < len(tagged_tokens)) and (targ_index <= 0):
        if tagged_tokens[pos][1][:2] == 'NN':
            targ_index = pos
        pos += 1

    orig = parse_orig(tagged_tokens, 0, rel_index)
    rel = parse_rel(tagged_tokens, rel_index, targ_index)
    targ = parse_targ(tagged_tokens, targ_index, len(tagged_tokens))

    return orig, rel, targ


if __name__ == '__main__':
    while True:
        print '?'
        line = sys.stdin.readline()
        orig, rel, targ = parse(line)
        print '%s [%s] %s' % (orig, rel, targ)
