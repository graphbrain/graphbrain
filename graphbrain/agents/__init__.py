from graphbrain.agents.actors import Actors
from graphbrain.agents.claim_actors import ClaimActors
from graphbrain.agents.claims import Claims
from graphbrain.agents.conflicts import Conflicts
from graphbrain.agents.corefs_dets import CorefsDets
from graphbrain.agents.corefs_names import CorefsNames
from graphbrain.agents.corefs_onto import CorefsOnto
from graphbrain.agents.reddit_parser import RedditParser
from graphbrain.agents.corefs_unidecode import CorefsUnidecode
from graphbrain.agents.taxonomy import Taxonomy
from graphbrain.agents.txt_parser import TxtParser


def create_agent(name, hg, lang=None, sequence=None):
    """Creates and returns an instance of the agent identified by the given
    name. Throws an exception if no such agent is known."""
    if name == 'actors':
        return Actors(hg, lang, sequence)
    elif name == 'claim_actors':
        return ClaimActors(hg, lang, sequence)
    elif name == 'claims':
        return Claims(hg, lang, sequence)
    elif name == 'conflicts':
        return Conflicts(hg, lang, sequence)
    elif name == 'corefs_dets':
        return CorefsDets(hg, lang, sequence)
    elif name == 'corefs_names':
        return CorefsNames(hg, lang, sequence)
    elif name == 'corefs_onto':
        return CorefsOnto(hg, lang, sequence)
    elif name == 'corefs_unidecode':
        return CorefsUnidecode(hg, lang, sequence)
    elif name == 'reddit_parser':
        return RedditParser(hg, lang, sequence)
    elif name == 'taxonomy':
        return Taxonomy(hg, lang, sequence)
    elif name == 'txt_parser':
        return TxtParser(hg, lang, sequence)
    else:
        RuntimeError('unknown agent: {}'.format(name))
