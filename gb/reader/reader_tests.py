from gb.reader.reader import Reader


def reader_tests(hg, infile, show_namespaces, lang='en', model_file=None):
    extractor = Reader(hg, show_namespaces=show_namespaces, lang=lang, model_file=model_file)
    extractor.debug = True
    with open(infile, 'r', encoding='utf-8') as f:
        for line in f:
            extractor.read_text(line)
            print('\n\n')
