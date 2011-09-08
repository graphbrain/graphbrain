import nltk
import sys


class ParseError(Exception):
    def __init__(self, msg):
        self.msg = msg

    def __str__(self):
        return 'Parse error: %s' % self.msg


def parse_argument(tokens):
    phrase = ''
    first = True
    for t in tokens:
        if first:
            first = False
        else:
            phrase += ' '
        phrase += t[0]

    return phrase


def parse_predicate(tokens):
    phrase = ''
    first = True
    for t in tokens:
        if t[1][:2] != 'DT':
            if first:
                first = False
            else:
                phrase += ' '
            phrase += t[0]

    return phrase


def remove_ponctuation(tagged_tokens):
    res = []
    for t in tagged_tokens:
        if t[0] not in '.,;!':
            res.append(t)

    return res


def parse(sentence):
    tokens = nltk.word_tokenize(sentence)
    tagged_tokens = nltk.pos_tag(tokens)

    tagged_tokens = remove_ponctuation(tagged_tokens)

    if len(tagged_tokens) == 0:
        raise ParseError('Empty sentence.')

    orig_tokens = []
    rel_tokens = []
    targ_tokens = []

    pos = 0

    # parsing origin
    quoted = False
    if tagged_tokens[0][0] == '"':
        quoted = True
        pos += 1
    while pos < len(tagged_tokens):
        if quoted:
            if tagged_tokens[pos][0] == '"':
                pos += 1
                break
        else:
            if tagged_tokens[pos][1][:2] == 'VB':
                break
        
        orig_tokens.append(tagged_tokens[pos])
        pos += 1

    # parsing relationship
    quoted = False
    while pos < len(tagged_tokens):
        if tagged_tokens[pos][0] == '"':
            pos += 1
            quoted = True
            break
        if (tagged_tokens[pos][1][:2] == 'NN') or (tagged_tokens[pos][1][:2] == 'JJ'):
            break
        rel_tokens.append(tagged_tokens[pos])
        pos += 1

    # parsing target
    while pos < len(tagged_tokens):
        if quoted:
            if tagged_tokens[pos][0] == '"':
                break
        
        targ_tokens.append(tagged_tokens[pos])
        pos += 1

    if (len(orig_tokens) == 0) or (len(rel_tokens) == 0) or (len(targ_tokens) == 0):
        raise ParseError('Could not understand sentence.')

    # raw relationshipt
    rel_raw = ''
    first = True
    for t in rel_tokens:
        if first:
            first = False
        else:
            rel_raw += ' '
        rel_raw += t[0]

    result = {}
    result['orig'] = parse_argument(orig_tokens)
    result['rel'] = parse_predicate(rel_tokens)
    result['targ'] = parse_argument(targ_tokens)
    result['rel_raw'] = rel_raw
    result['sentence'] = sentence

    return result


if __name__ == '__main__':
    while True:
        print '?'
        line = sys.stdin.readline()
        try:
            r = parse(line)
        except ParseError, p:
            print p
            continue
        print "'%s' [%s] '%s'" % (r['orig'], r['rel'], r['targ'])
        print "raw predicate: %s" % r['rel_raw']
