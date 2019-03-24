from IPython.core.display import display, HTML
from graphbrain.hypergraph import *
from graphbrain.funs import *


SYMBOL_COLOR = '#404040'
NAMESPACE_COLOR = '#7F7F6F'
EDGE_COLORS = ['#a65628', '#377eb8', '#4daf4a', '#984ea3', '#ff7f00',
               '#ffff33', '#e41a1c', '#f781bf']


def _ent2html(entity, roots_only=True, compact=False, indent=False, close=True,
              depth=0, color='#000', conn=False, close_html=''):
    """Convert an entity to an html representation, internal implementation."""

    # default value for close html string that is returned to caller
    ret_close_html = ''

    # font size
    if compact:
        font_size = 11
    else:
        font_size = 14 - depth

    # different renders depending on weather:
    # ... entity is an edge
    if is_edge(entity):
        et = entity_type(entity)[0]
        arity = len(entity)
        color = EDGE_COLORS[depth % len(EDGE_COLORS)]

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
        <span style="font-weight:bold;font-size:{}pt">{}</span>
        """.format(str(font_size), open_symbol).strip()

        # html that closes this edge render
        if compact or (not indent):
            close_html = '</span>'
        else:
            close_html = '</div>'

        # render edge children
        for i in range(arity):
            # first entity (connector)?
            if i == 0:
                child_indent = False
                child_color = color
                child_conn = True
                sep = ''
            # not connector
            else:
                # edges with only two items are rendered in one line
                if arity > 2:
                    child_indent = True
                else:
                    child_indent = False
                child_color = SYMBOL_COLOR
                child_conn = conn
                sep = ' '

            child = entity[i]
            if is_edge(child):
                # Do not render closing html of children that are the last
                # entity in a hyperedge. Instead, let some higher-level edge
                # render it to avoid ugly multi-line staircases when closing
                # a sequence of edges.
                child_close = i < arity - 1

                child_html, child_close_html = _ent2html(child,
                                                         roots_only=roots_only,
                                                         compact=compact,
                                                         indent=child_indent,
                                                         close=child_close,
                                                         depth=depth + 1,
                                                         conn=child_conn)
                # accumulate close_html of child
                close_html = '{}{}'.format(child_close_html, close_html)
            else:
                child_html, _ = _ent2html(child,
                                          roots_only=roots_only,
                                          compact=compact,
                                          depth=depth,
                                          color=child_color,
                                          conn=child_conn)
            html = '{}{}{}'.format(html, sep, child_html)

        if indent:
            margin = 20
        else:
            margin = 0

        # if closing html should not be rendered at the end o this edge
        # representation, then return it to the parent
        if not close:
            ret_close_html = close_html
            close_html = ''

        # render close symbol
        html = """
        {}<span style="color:{}">
            <span style="font-weight:bold;font-size:{}pt">{}</span>
        </span>
        """.format(html, color, str(font_size), close_symbol).strip()

        # render edge
        if compact or (not indent):
            html = """
            <span style="color:{}">{}{}
            """.format(color, html, close_html).strip()
        else:
            html = """
            <div style="margin-left:{}px;color:{}">{}{}
            """.format(str(margin), color, html, close_html).strip()
    # ... entity is an atom
    else:
        # connectors are rendered in bold
        if conn:
            bold_style = 'font-weight:bold;'
        else:
            bold_style = ''

        # render atom root
        html = """
        <span style="{}color:{}">{}</span>
        """.format(bold_style, color, str(root(entity)).strip()).strip()

        # render non-root part, if requested
        if not roots_only:
            html = """
            {}<span style="color:{};font-size:8pt">/{}</span>
            """.format(html,
                       NAMESPACE_COLOR,
                       str('/'.join(atom_parts(entity)[1:])).strip()).strip()

        # render atom with font size
        html = """
        <span style="font-size:{}pt">{}</span>
        """.format(str(font_size), html).strip()

    return html, ret_close_html


def ent2html(entity, roots_only=True, compact=False):
    """Convert an entity to an html representation."""
    return _ent2html(entity, roots_only=roots_only, compact=compact)[0]


def show(entity, roots_only=True, compact=False):
    """Show a representation of the entity in the notebook."""
    html = ent2html(entity, roots_only=roots_only, compact=compact)
    display(HTML(html))
