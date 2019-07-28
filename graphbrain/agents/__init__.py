from graphbrain.agents.claims import Claims
from graphbrain.agents.coref_atoms import CorefAtoms
from graphbrain.agents.coref_names import CorefNames
from graphbrain.agents.coref_onto import CorefOnto
from graphbrain.agents.taxonomy import Taxonomy


def create_agent(name, hg):
    if name == 'claims':
        return Claims(hg)
    elif name == 'coref_atoms':
        return CorefAtoms(hg)
    elif name == 'coref_names':
        return CorefNames(hg)
    elif name == 'coref_onto':
        return CorefOnto(hg)
    elif name == 'taxonomy':
        return Taxonomy(hg)
    else:
        RuntimeError('unknown agent: %s')
