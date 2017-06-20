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


import json


def read(input_file):
    items = []
    with open(input_file, 'r') as inf:
        for line in inf:
            item = json.loads(line)
            items.append(item)
    return items


def extract_fields(input_file, output_file, field_names):
    fields = field_names.split(',')

    outf = open(output_file, 'w')
    with open(input_file, 'r') as inf:
        for line in inf:
            item = json.loads(line)
            values = [item[field] for field in fields]
            if None not in values:
                outf.write('%s\n' % ' '.join(values))
    outf.close()
