from html import escape
from IPython.core.display import display, HTML
from graphbrain import *


SYMBOL_COLOR = '#404040'
NAMESPACE_COLOR = '#7F7F6F'
EDGE_COLORS = ['#a65628', '#377eb8', '#4daf4a', '#984ea3', '#ff7f00',
               '#ffff33', '#e41a1c', '#f781bf']


def _edge2html_r(edge, roots_only=True, formatting='indented', indent=False,
                 close=True, depth=0, color='#000', conn=False, close_html=''):
    assert(formatting in {'indented', 'compact', 'oneline'})

    # default value for close html string that is returned to caller
    ret_close_html = ''

    # font size
    if formatting == 'indented':
        fs = 14 - depth
    elif formatting == 'compact':
        fs = 12
    elif formatting == 'oneline':
        fs = 11
    font_size = 'font-size:{}pt;'.format(fs)

    # indent-dependent variables
    if indent:
        main_tag = 'div'
        margin = 'margin-left:20px;'
    else:
        main_tag = 'span'
        margin = ''

    # color
    color_html = 'color:{}'.format(color)

    # different renders depending on weather:
    # ... edge is an atom:
    if edge.is_atom():
        # connectors are rendered in bold
        bold_style = 'font-weight:bold;' if conn else ''

        # render atom root
        html = """
        <span style="{}{}">{}</span>
        """.format(bold_style,
                   color_html,
                   escape(str(edge.root()).strip())).strip()

        # render non-root part, if requested
        if not roots_only:
            escaped_codes = '/'.join(edge.parts()[1:])
            escaped_codes = escape(escaped_codes).strip()
            html = """
            {}<span style="color:{};font-size:8pt">/{}</span>
            """.format(html,
                       NAMESPACE_COLOR,
                       escaped_codes)

        # render atom
        html = """
        <{} style="{}{}">{}</{}>
        """.format(main_tag, margin, font_size, html, main_tag).strip()
    # ... edge is an edge
    else:
        et = edge.type()[0]
        arity = len(edge)
        contains_edges = any(not child.is_atom() for child in edge)
        color = EDGE_COLORS[depth % len(EDGE_COLORS)]
        color_html = 'color:{}'.format(color)

        # surroundings symbols depending on edge type
        if et == 'c':
            open_symbol = '['
            close_symbol = ']'
        elif et in {'s', 'x'}:
            open_symbol = '{'
            close_symbol = '}'
        else:
            open_symbol = '('
            close_symbol = ')'

        # render opening symbol
        html = """
        <span style="font-weight:bold;{}">{}</span>
        """.format(font_size, open_symbol).strip()

        close_html = '</{}>'.format(main_tag)

        # render edge children
        for i in range(arity):
            # first edge (connector)?
            if i == 0:
                child_indent = False
                child_color = color
                child_conn = True
                sep = ''
            # not connector
            else:
                # indent depending on formatting
                if formatting == 'indented':
                    child_indent = contains_edges if child.is_atom() else True
                elif formatting == 'compact':
                    # edges with only two items are rendered in one line
                    child_indent = arity > 2
                    if child_indent and child.is_atom() and not contains_edges:
                        child_indent = False
                elif formatting == 'oneline':
                    child_indent = False

                child_color = SYMBOL_COLOR
                child_conn = conn
                sep = ' '

            child = edge[i]
            if child.is_atom():
                child_html, _ = _edge2html_r(child,
                                             roots_only=roots_only,
                                             formatting=formatting,
                                             indent=child_indent,
                                             depth=depth,
                                             color=child_color,
                                             conn=child_conn)
            else:
                # Do not render closing html of children that are the last
                # edge in a hyperedge. Instead, let some higher-level edge
                # render it to avoid ugly multi-line staircases when closing
                # a sequence of edges.
                child_close = i < arity - 1

                child_html, child_cl_html = _edge2html_r(child,
                                                         roots_only=roots_only,
                                                         formatting=formatting,
                                                         indent=child_indent,
                                                         close=child_close,
                                                         depth=depth + 1,
                                                         conn=child_conn)
                # accumulate close_html of child
                close_html = '{}{}'.format(child_cl_html, close_html)
            html = '{}{}{}'.format(html, sep, child_html)

        # if closing html should not be rendered at the end o this edge
        # representation, then return it to the parent
        if not close:
            ret_close_html = close_html
            close_html = ''

        # render close symbol
        html = """
        {}<span style="{}">
            <span style="font-weight:bold;{}">{}</span>
        </span>
        """.format(html, color_html, font_size, close_symbol).strip()

        # render edge
        html = """
        <{} style="{}{}">{}{}
        """.format(main_tag, margin, color_html, html, close_html).strip()
    # ... edge is an atom

    return html, ret_close_html


def _edge2html(edge, roots_only=True, formatting='indented'):
    return _edge2html_r(edge, roots_only=roots_only, formatting=formatting)[0]


def show(edge, roots_only=True, style='indented'):
    """Displays a representation of the edge in the notebook.

    Keyword arguments:

    roots_only -- only show roots of atoms (default: True)

    style -- render style ('indented', 'compact' or 'oneline')
    (default: 'indented')
    """
    html = _edge2html(edge, roots_only=roots_only, formatting=style)
    display(HTML(html))
