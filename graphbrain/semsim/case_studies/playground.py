import logging
from _base import get_hgraph

logger = logging.getLogger()

hg = get_hgraph()


def search_for_pattern():
    # search_pattern = '((semsim say/P.{s-}) mother/C VAR)'
    # search_pattern = '((lemma say/P.{s-}) mother/C VAR)'
    # search_pattern = '((semsim fight/P.{s-}) (semsim mother/C) VAR)'
    # search_pattern = '((semsim fight/P.{s-} mother/C) VAR)'  # what happens in this case? is mother ignored?

    search_pattern = '((semsim [party,fight,survive]/P.{s-}) (semsim mother/C) VAR)'

    search_results = list(hg.search(search_pattern))
    # search_results = list(hg.match(search_pattern))

    output_str = f"Pattern: {search_pattern}\n" \
                 f"N of results: {len(search_results)}\n" \
                 f"Results:\n"

    print(output_str)
    for result in search_results:
        print(result)


if __name__ == "__main__":
    # search_for_pattern()
    # hg.sequence("mother")

    search_for_pattern()
