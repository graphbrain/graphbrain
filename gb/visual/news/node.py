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


def head():
    return """
<script src="https://d3js.org/d3.v4.min.js"></script>
    """


def html(symbol):
    return """
<div role="main" class="container">
  <div class="row">
    <div class="col"><h1 id="title"></h1></div>
  </div>
  <div class="row">
    <div class="col" id="table" style="height:500px; overflow: scroll;"></div>
    <div class="col" id="graph"></div>
  </div>
</div>    
   
<script src="/static/js/news.js"></script>

<script>
    generate_visualisation('%s');
</script>
    """ % symbol
