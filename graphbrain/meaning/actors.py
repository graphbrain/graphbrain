from graphbrain.meaning.corefs import main_coref


def is_actor(hg, edge):
    """Checks if the edge is a coreference to an actor."""
    if edge.type()[0] == 'C':
        return hg.exists(('actor/P/.', main_coref(hg, edge)))
    else:
        return False


def find_actors(hg, edge):
    """Returns set of all coreferences to actors found in the edge."""
    actors = set()
    if is_actor(hg, edge):
        actors.add(main_coref(hg, edge))
    if not edge.is_atom():
        for item in edge:
            actors |= find_actors(hg, item)
    return actors


def actors(hg):
    """"Returns an iterator over all actors."""
    return [edge[1] for edge in hg.search('(actor/P/. *)')]
