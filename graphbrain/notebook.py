from IPython.core.display import display, HTML
from graphbrain.hypergraph import *
from graphbrain.funs import *
from graphbrain.meaning.vis import print_tree


SYMBOL_COLOR = '#404040'
NAMESPACE_COLOR = '#7F7F6F'
EDGE_COLORS = ['#a65628', '#377eb8', '#4daf4a', '#984ea3', '#ff7f00',
               '#ffff33', '#e41a1c', '#f781bf']


def _ent2html(entity, roots_only=True, compact=False, indent=False, close=True,
              depth=0, color='#000', conn=False):
    """Convert an edge to an html representation."""
    closes = 0
    if compact:
        font_size = 11
    else:
        font_size = 14 - depth
    if is_edge(entity):
        color = EDGE_COLORS[depth % len(EDGE_COLORS)]
        closes = 1
        html = """
        <span style="font-weight:bold;font-size:{}pt">(</span>
        """.format(str(font_size)).strip()
        for i in range(len(entity)):
            if i == 0:
                inner_indent = False
                inner_color = color
                inner_conn = True
                sep = ''
            else:
                inner_indent = True
                inner_color = SYMBOL_COLOR
                inner_conn = conn
                sep = ' '
            if len(entity) <= 2:
                inner_indent = False
            item = entity[i]
            if is_edge(item):
                inner_close = i < len(entity) - 1
                inner_html, inner_closes = _ent2html(item,
                                                     roots_only=roots_only,
                                                     compact=compact,
                                                     indent=inner_indent,
                                                     close=inner_close,
                                                     depth=depth + 1,
                                                     conn=inner_conn)
                closes += inner_closes
            else:
                inner_html, _ = _ent2html(item, roots_only=roots_only,
                                          compact=compact,
                                          depth=depth,
                                          color=inner_color,
                                          conn=inner_conn)
            html = '{}{}{}'.format(html, sep, inner_html)
        if indent:
            margin = 20
        else:
            margin = 0
        close_html = ''
        if close:
            if compact:
                close_html = '</span>' * closes
            else:
                close_html = '</div>' * closes
        html = """
        {}<span style="color:{}">
            <span style="font-weight:bold;font-size:{}pt">)</span>
        </span>
        """.format(html, color, str(font_size)).strip()
        if compact or (not indent):
            html = """
            <span style="color:{}">{}{}
            """.format(color, html, close_html).strip()
        else:
            html = """
            <div style="margin-left:{}px;color:{}">{}{}
            """.format(str(margin), color, html, close_html).strip()
    else:
        if conn:
            bold_style = 'font-weight:bold;'
        else:
            bold_style = ''
        html = """
        <span style="{}color:{}">{}</span>
        """.format(bold_style, color, str(root(entity)).strip()).strip()
        if not roots_only:
            html = """
            {}<span style="color:{};font-size:8pt">/{}</span>
            """.format(html,
                       NAMESPACE_COLOR,
                       str('/'.join(atom_parts(entity)[1:])).strip()).strip()
        html = """
        <span style="font-size:{}pt">{}</span>
        """ .format(str(font_size), html).strip()

    if close:
        return html, 0
    else:
        return html, closes


def ent2html(entity, roots_only=True, compact=False):
    return _ent2html(entity, roots_only=roots_only, compact=compact)[0]


def show(entity, roots_only=True, compact=False):
    html = ent2html(entity, roots_only=roots_only, compact=compact)
    display(HTML(html))


def parse_and_show(parser, text, roots_only=True, compact=False, tree=False):
    parses = parser.parse(text)
    for parse in parses:
        if tree:
            display(HTML('<h3>Parse Tree</h3>'))
            print_tree(parse['spacy_sentence'].root)
        show(parse['main_edge'], roots_only=roots_only, compact=compact)
