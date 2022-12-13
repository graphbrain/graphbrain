import logging

from graphbrain.semsim.utils import make_any_func_pattern, save_search_results
from graphbrain.semsim.case_studies._base import get_hgraph, result_dir


logger = logging.getLogger(__name__)

hg = get_hgraph()

# original pattern:
# '( PRED/P.{so,x} SOURCE/C TARGET/C [against,for,of,over]/T TOPIC/[RS] ) '
# '∧ ( lemma/J >PRED/P [accuse,arrest,clash,condemn,kill,slam,warn]/P )'

preds = ["accuse", "arrest", "clash", "condemn", "kill", "slam", "warn"]
preps = ["against", "for", "of", "over"]

# preds = ["accuse"]
# preps = ["of"]


pred_pattern = make_any_func_pattern(preds, inner_funcs=["atoms", "lemma"], arg_roles=["P.{so,x}"])
print(f"PRED pattern: {pred_pattern}")

prep_pattern = make_any_func_pattern(preps, arg_roles=["T"])
print(f"PREP pattern: {prep_pattern}")

topic_pattern = make_any_func_pattern(["TOPIC"], arg_roles=["R", "S"])
print(f"TOPIC pattern: {topic_pattern}")


conflict_pattern = f"( (var {pred_pattern} PRED) SOURCE/C TARGET/C ({prep_pattern} {topic_pattern}) )"
print("--- conflict pattern ---")
print(conflict_pattern)
print("------------------------")


results_dict = {
    "pattern": conflict_pattern,
    "results": []
}

for edge in hg.search(conflict_pattern):
    edge_str = str(edge)
    edge_text = hg.text(edge)

    result = {
        "edge": edge_str,
        "edge_text": edge_text
    }
    results_dict["results"].append(result)

    print(str(edge))
    print(hg.text(edge))
    print("-----")

save_search_results(results_dict, result_dir / "conflicts.json")
