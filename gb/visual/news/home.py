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


import operator


def actor_list(hg):
    actors = [edge[1] for edge in hg.pattern2edges(('is_actor/gb.inf', None))]
    data = [{'actor': actor,
             'h_mentioned_by': hg.get_float_attribute(actor, 'h_mentioned_by'),
             'h-conflict': hg.get_float_attribute(actor, 'h_conflict')} for actor in actors]
    return data


def actors_html(hg):
    data = actor_list(hg)
    scored_actors = {}
    for item in data:
        scored_actors[item['actor']] = item['h_mentioned_by']
    sorted_actors = sorted(scored_actors.items(), key=operator.itemgetter(1), reverse=True)
    str_list = ['<p><a href="/node/%s">%s</a> (%s)</p>' % (item[0], item[0], item[1]) for item in sorted_actors]
    return '\n'.join(str_list)


def html(hg):
    return """
<div class="container" role="main">
    %s
</div>
    """ % actors_html(hg)
