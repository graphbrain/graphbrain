from IPython.core.display import display, HTML
from graphbrain import *


SYMBOL_COLOR = '#404040'
NAMESPACE_COLOR = '#7F7F6F'
EDGE_COLORS = ['#a65628', '#377eb8', '#4daf4a', '#984ea3', '#ff7f00',
               '#ffff33', '#e41a1c', '#f781bf']


def _ent2html(entity, roots_only=True, formatting='indented', indent=False,
              close=True, depth=0, color='#000', conn=False, close_html=''):
    """Convert an entity to an html representation, internal implementation."""

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
    # ... entity is an edge
    if is_edge(entity):
        et = entity_type(entity)[0]
        arity = len(entity)
        contains_edges = any(is_edge(child) for child in entity)
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
            # first entity (connector)?
            if i == 0:
                child_indent = False
                child_color = color
                child_conn = True
                sep = ''
            # not connector
            else:
                # indent depending on formatting
                if formatting == 'indented':
                    child_indent = contains_edges if is_atom(child) else True
                elif formatting == 'compact':
                    # edges with only two items are rendered in one line
                    child_indent = arity > 2
                    if child_indent and is_atom(child) and not contains_edges:
                        child_indent = False
                elif formatting == 'oneline':
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
                                                         formatting=formatting,
                                                         indent=child_indent,
                                                         close=child_close,
                                                         depth=depth + 1,
                                                         conn=child_conn)
                # accumulate close_html of child
                close_html = '{}{}'.format(child_close_html, close_html)
            else:
                child_html, _ = _ent2html(child,
                                          roots_only=roots_only,
                                          formatting=formatting,
                                          indent=child_indent,
                                          depth=depth,
                                          color=child_color,
                                          conn=child_conn)
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
    # ... entity is an atom
    else:
        # connectors are rendered in bold
        bold_style = 'font-weight:bold;' if conn else ''

        # render atom root
        html = """
        <span style="{}{}">{}</span>
        """.format(bold_style, color_html, str(root(entity)).strip()).strip()

        # render non-root part, if requested
        if not roots_only:
            html = """
            {}<span style="color:{};font-size:8pt">/{}</span>
            """.format(html,
                       NAMESPACE_COLOR,
                       str('/'.join(atom_parts(entity)[1:])).strip()).strip()

        # render atom
        html = """
        <{} style="{}{}">{}</{}>
        """.format(main_tag, margin, font_size, html, main_tag).strip()

    return html, ret_close_html


def ent2html(entity, roots_only=True, formatting='indented'):
    """Convert an entity to an html representation."""
    return _ent2html(entity, roots_only=roots_only, formatting=formatting)[0]


def show(entity, roots_only=True, formatting='indented'):
    """Show a representation of the entity in the notebook."""
    html = ent2html(entity, roots_only=roots_only, formatting=formatting)
    display(HTML(html))
