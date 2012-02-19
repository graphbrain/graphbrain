# -*- coding: utf-8 -*-

import random
import string
import time


def random_string(length):
    rg = random.SystemRandom()
    alphabet = string.letters[0:52] + string.digits
    return str().join(rg.choice(alphabet) for _ in range(length))


def timestamp():
    return int(time.time())


def force_utf8(s):
    """
    Forces string to utf-8 econding.
    """
    if not isinstance(s, basestring):
        try:
            return str(s)
        except UnicodeEncodeError:
            if isinstance(s, Exception):
                return ' '.join([force_utf8(x) for x in s])
            return unicode(s).encode('utf-8', 'strict')
    elif isinstance(s, unicode):
        return s.encode('utf-8', 'strict')
    else:
        return s
