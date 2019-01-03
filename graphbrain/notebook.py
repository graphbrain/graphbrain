from IPython.core.display import display, HTML
from graphbrain.hypergraph import *
from graphbrain.funs import *


SYMBOL_COLOR = '#404040'
NAMESPACE_COLOR = '#7F7F6F'
EDGE_COLORS = ['#a65628', '#377eb8', '#4daf4a', '#984ea3', '#ff7f00', '#ffff33', '#e41a1c', '#f781bf']


def _edge2html(edge, namespaces=True, compact=False, indent=False, close=True, depth=0, color='#000', conn=False):
    """Convert an edge to an html representation."""
    closes = 0
    if compact:
        font_size = 11
    else:
        font_size = 14 - depth
    if symbol_type(edge) == SymbolType.EDGE:
        color = EDGE_COLORS[depth % len(EDGE_COLORS)]
        closes = 1
        html = '<span style="font-weight:bold;font-size:%spt">(</span>' % str(font_size)
        for i in range(len(edge)):
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
            if len(edge) <= 2:
                inner_indent = False
            item = edge[i]
            if symbol_type(item) == SymbolType.EDGE:
                inner_close = i < len(edge) - 1
                inner_html, inner_closes = _edge2html(item, namespaces=namespaces, compact=compact, indent=inner_indent,
                                                      close=inner_close, depth=depth + 1, conn=inner_conn)
                closes += inner_closes
            else:
                inner_html, _ = _edge2html(item, namespaces=namespaces, compact=compact, depth=depth, color=inner_color,
                                           conn=inner_conn)
            html = '%s%s%s' % (html, sep, inner_html)
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
        html = '%s<span style="color:%s"><span style="font-weight:bold;font-size:%spt">)</span></span>'\
               % (html, color, str(font_size))
        if compact or (not indent):
            html = '<span style="color:%s">%s%s' % (color, html, close_html)
        else:
            html = '<div style="margin-left:%spx;color:%s">%s%s' % (str(margin), color, html, close_html)
    else:
        if conn:
            bold_style = 'font-weight:bold;'
        else:
            bold_style = ''
        html = '<span style="%scolor:%s">%s</span>' % (bold_style, color, str(symbol_root(edge)).strip())
        if namespaces:
            html = '%s<span style="color:%s;font-size:8pt">/%s</span>'\
                   % (html, NAMESPACE_COLOR, str(symbol_namespace(edge)).strip())
        html = '<span style="font-size:%spt">%s</span>' % (str(font_size), html)

    if close:
        return html, 0
    else:
        return html, closes


def edge2html(edge, namespaces=True, compact=False):
    return _edge2html(edge, namespaces=namespaces, compact=compact)[0]


def show(edge, namespaces=True, compact=False):
    html = edge2html(edge, namespaces=namespaces, compact=compact)
    display(HTML(html))


def read_and_show(reader, text,  namespaces=True, compact=False, show_stages=False):
    outputs = reader.read_text(text)
    for output in outputs:
        if show_stages:
            display(HTML('<h3>Parse Tree</h3>'))
            output[1].sentence.print_tree()
            for i in range(len(reader.stages)):
                stage_label = 'stage #%s: %s' % (i + 1, reader.stages[i])
                display(HTML('<h3>%s</h3>' % stage_label))
                edge = output[1].stage_outputs[i]
                show(edge, namespaces=namespaces, compact=True)
        edge = output[1].main_edge
        show(edge, namespaces=namespaces, compact=compact)
