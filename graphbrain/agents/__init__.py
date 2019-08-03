from graphbrain.agents.actors import Actors
from graphbrain.agents.claims import Claims
from graphbrain.agents.conflicts import Conflicts
from graphbrain.agents.corefs_atoms import CorefsAtoms
from graphbrain.agents.corefs_names import CorefsNames
from graphbrain.agents.corefs_onto import CorefsOnto
from graphbrain.agents.reddit_parser import RedditParser
from graphbrain.agents.taxonomy import Taxonomy


def create_agent(name, hg):
    if name == 'actors':
        return Actors(hg)
    elif name == 'claims':
        return Claims(hg)
    elif name == 'conflicts':
        return Conflicts(hg)
    elif name == 'corefs_atoms':
        return CorefsAtoms(hg)
    elif name == 'corefs_names':
        return CorefsNames(hg)
    elif name == 'corefs_onto':
        return CorefsOnto(hg)
    elif name == 'reddit_parser':
        return RedditParser(hg)
    elif name == 'taxonomy':
        return Taxonomy(hg)
    else:
        RuntimeError('unknown agent: {}'.format(name))
