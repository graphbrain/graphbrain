# -*- coding: utf-8 -*-

import sys
from config import *


def nodetype(text):
    ntype = 0

    # VERY naive way to detect images...
    # temporary hack
    if text[:7] == 'http://':
        ntype = 1

    return ntype


if __name__ == '__main__':
    pass
