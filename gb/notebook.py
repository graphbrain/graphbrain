from IPython.core.display import display, HTML
from gb.hypergraph import *
from gb.funs import *


EDGE_COLORS = ['#008cff', '#891100', '#028401', '#002eff', '#ff8802', '#008688']


def _edge2html(edge, namespaces=True, compact=False, indent=False, close=True, depth=0):
    """Convert an edge to an html representation."""
    closes = 0
    color = EDGE_COLORS[depth % len(EDGE_COLORS)]
    if compact:
        font_size = 11
    else:
        font_size = 14 - depth
    if symbol_type(edge) == SymbolType.EDGE:
        closes = 1
        html = '<span style="font-size:%spt">(</span>' % str(font_size)
        after_atom = False
        for i in range(len(edge)):
            item = edge[i]
            if symbol_type(item) == SymbolType.EDGE:
                inner_close = i < (len(edge) - 1)
                inner_html, inner_closes = _edge2html(item, namespaces=namespaces, compact=compact, indent=True,
                                                      close=inner_close, depth=depth + 1)
                closes += inner_closes
                sep = ''
                if i > 0:
                    sep = ' '
                html = '%s%s%s' % (html, sep, inner_html)
                after_atom = False
            else:
                sep = ''
                if after_atom:
                    sep = ' '
                inner_html, _ = _edge2html(item, namespaces=namespaces, compact=compact, depth=depth)
                html = '%s%s%s' % (html, sep, inner_html)
                after_atom = True
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
        html = '%s<span style="color:%s"><span style="font-size:%spt">)</span></span>' % (html, color, str(font_size))
        if compact:
            html = '<span style="color:%s">%s%s' % (color, html, close_html)
        else:
            html = '<div style="margin-left:%spx;color:%s">%s%s' % (str(margin), color, html, close_html)
    else:
        html = '<span style="font-weight:bold">%s</span>' % str(symbol_root(edge)).strip()
        if namespaces:
            html = '%s<span style="color:#9F9F8F;font-size:7pt">/%s</span>'\
                   % (html, str(symbol_namespace(edge)).strip())
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
        if show_stages:
            for i in range(len(reader.stages)):
                stage_label = 'stage #%s: %s' % (i + 1, reader.stages[i])
                display(HTML('<h3>%s</h3>' % stage_label))
                edge = output[1].stage_outputs[i]
                show(edge, namespaces=namespaces, compact=True)
        edge = output[1].main_edge
        show(edge, compact=compact)
