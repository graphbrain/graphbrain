#!/usr/bin/env python

#   Copyright (c) 2016 CNRS - Centre national de la recherche scientifique.
#   All rights reserved.
#
#   Written by Telmo Menezes <telmo@telmomenezes.com>
#
#   This file is part of GraphBrain.
#
#   GraphBrain is free software: you can redistribute it and/or modify
#   it under the terms of the GNU Affero General Public License as published by
#   the Free Software Foundation, either version 3 of the License, or
#   (at your option) any later version.
#
#   GraphBrain is distributed in the hope that it will be useful,
#   but WITHOUT ANY WARRANTY; without even the implied warranty of
#   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#   GNU Affero General Public License for more details.
#
#   You should have received a copy of the GNU Affero General Public License
#   along with GraphBrain.  If not, see <http://www.gnu.org/licenses/>.


import sys
import re
import sys


def output(line):
    sys.stdout.write(line)


def check_title(title):
    return ':' not in title


def main(argv):
    page = re.compile('<page>')
    close_page = re.compile('</page>')
    title = re.compile('<title>(.*)</title>')

    state = 'page_search'
    buffer = ''
    
    with open(argv[0]) as infile:
        for line in infile:
            if state == 'page_search':
                m = page.search(line)
                if m is None:
                    output(line)
                else:
                    state = 'title_search'
                    buffer = line
            elif state == 'title_search':
                m = title.search(line)
                if m is None:
                    buffer += line
                else:
                    if check_title(m.group(1)):
                        output(buffer)
                        output(line)
                        state = 'page_search'
                    else:
                        state = 'close_page_search'
                    buffer = ''
            elif state == 'close_page_search':
                m = close_page.search(line)
                if m is None:
                    pass
                else:
                    state = 'page_search'


if __name__ == "__main__":
    main(sys.argv[1:])
