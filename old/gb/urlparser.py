# -*- coding: utf-8 -*-

import sys
from config import *


def nodetype(text):
    ntype = 'text'

    # VERY naive way to detect images...
    # temporary hack
    if text[:7] == 'http://':
        ntype = 'image'

    return ntype


if __name__ == '__main__':
    pass
