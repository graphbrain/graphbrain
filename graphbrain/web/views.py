from urllib.parse import quote

from flask import render_template, request, redirect, session

from graphbrain.hyperedge import hedge
from graphbrain.learner.pattern_ops import remove_variables
from graphbrain.notebook import _edge2html_blocks
from graphbrain.web import app


def enrich_case(hg, case):
    case['edge_html'] = _edge2html_blocks(case['edge'])
    variables = []
    for variable in case['variables']:
        new_var = (
            variable[0],
            (variable[1][0], hg.get_str_attribute(variable[1][1], 'text') if variable[1][1] else ''))
        variables.append(new_var)
    case['variables'] = variables
    return case


@app.route('/')
def classifiers_route():
    classifiers = []
    for cls_name, classifier in app.config['LEARNER'].classifiers.items():
        classifiers.append(
            {'name': cls_name,
             'npatterns': len(classifier.rules),
             'ncases': len(classifier.cases)})
    return render_template(
        'classifiers.html',
        cls_name=session['classifier'] if 'classifier' in session else None,
        hg=app.config['HG'],
        nav='classifiers',
        classifiers=classifiers)


@app.route('/classifier/<name>')
def classifier_route(name):
    session['classifier'] = name

    classifier = app.config['LEARNER'].classifiers[name]
    classifier.assign_rule_case_matches()
    matches, percentage, est_total_matches = app.config['LEARNER'].find_matches(name, 20)
    for match in matches:
        match['edge'] = quote(str(match['edge']))
    return render_template('classifier.html',
                           nav='classifiers',
                           cls_name=name,
                           hg=app.config['HG'],
                           classifier=classifier,
                           matches=matches,
                           percentage=percentage,
                           est_total_matches=est_total_matches)


@app.route('/relearn', methods=['POST'])
def relearn_route():
    # a classifier must be selected first
    if 'classifier' not in session:
        return redirect('/')

    cls_name = session['classifier']
    classifier = app.config['LEARNER'].classifiers[cls_name]

    classifier.learn()
    classifier.save()
    return redirect('/classifier/{}'.format(cls_name))


@app.route('/new_classifier', methods=['POST'])
def new_classifier_route():
    new_cls_name = request.form['new_class_name']
    app.config['LEARNER'].new_classifier(new_cls_name)
    return redirect('/')


@app.route('/case', methods=['GET', 'POST'])
def case_route():
    # a classifier must be selected first
    if 'classifier' not in session:
        return redirect('/')

    cls_name = session['classifier']
    classifier = app.config['LEARNER'].classifiers[cls_name]

    if len(request.form) > 0:
        edge = hedge(request.form['edge'])
        positive = 'positive' in request.form

        variables = {}
        i = 0
        while True:
            var_key = 'variable{}'.format(i)
            if var_key in request.form:
                variable = request.form[var_key]
                content = request.form['content{}'.format(i)]
                vedge = app.config['LEARNER'].text2subedge(edge, content)
                variables[variable] = vedge
                i += 1
            else:
                break

        classifier.add_case(edge, positive, variables=variables)
        classifier.learn()
        classifier.save()
        redirect('/case')
    case = enrich_case(app.config['LEARNER'].hg, app.config['LEARNER'].generate_case(cls_name))
    return render_template(
        'case.html',
        cls_name=cls_name,
        hg=app.config['HG'],
        classifier=classifier,
        nav='learn',
        case=case)


@app.route('/case/edge')
def case_edge_route():
    # a classifier must be selected first
    if 'classifier' not in session:
        return redirect('/')

    cls_name = session['classifier']
    classifier = app.config['LEARNER'].classifiers[cls_name]

    edge = request.args.get('edge')
    pattern = request.args.get('pattern')

    case = enrich_case(
        app.config['LEARNER'].hg,
        app.config['LEARNER'].generate_case(cls_name, edge=hedge(edge), pattern=hedge(pattern)))
    return render_template(
        'case.html',
        cls_name=cls_name,
        hg=app.config['HG'],
        classifier=classifier,
        nav='learn',
        case=case)


@app.route('/cases')
def cases_route():
    # a classifier must be selected first
    if 'classifier' not in session:
        return redirect('/')

    cls_name = session['classifier']
    classifier = app.config['LEARNER'].classifiers[cls_name]
    hg = app.config['LEARNER'].hg

    cases = []
    for case in classifier.cases:
        edge = remove_variables(case[0])
        sentence = hg.get_str_attribute(edge, 'text')
        match = classifier.classify(edge)
        rules_triggered = classifier.rules_triggered(edge)
        variables = []
        if match:
            match = match[0]
            for variable in match:
                variables.append((
                    variable,
                    hg.get_str_attribute(match[variable], 'text')))
        cases.append(
            {'edge': quote(str(edge)),
             'sentence': sentence,
             'positive': case[1],
             'rules_triggered': rules_triggered,
             'variables': variables})

    return render_template(
        'cases.html',
        cls_name=cls_name,
        hg=app.config['HG'],
        classifier=classifier,
        nav='cases',
        cases=cases)


@app.route('/verbs')
def verbs_route():
    # a classifier must be selected first
    if 'classifier' not in session:
        return redirect('/')

    hg = app.config['LEARNER'].hg
    learner = app.config['LEARNER']

    predicates = learner.top_predicates()
    verbs = []
    for predicate, count in predicates:
        if count >= 20:
            examples = learner.select_edge_predicates(predicate)
        else:
            examples = []
        if len(examples) > 0:
            edge = examples[0]
            sentence = hg.get_str_attribute(edge, 'text')
        else:
            edge = None
            sentence = ''
        verbs.append(
            {'verb': predicate,
             'count': count,
             'edge': edge,
             'sentence': sentence})
        
    return render_template(
        'verbs.html',
        cls_name=session['classifier'],
        hg=app.config['HG'],
        nav='verbs',
        verbs=verbs)


@app.route('/verb/<verb>')
def verb_route(verb):
    # a classifier must be selected first
    if 'classifier' not in session:
        return redirect('/')

    hg = app.config['LEARNER'].hg
    edges = app.config['LEARNER'].select_edge_predicates(predicate=verb, count=20)
    cases = [(quote(str(edge)), hg.get_str_attribute(edge, 'text')) for edge in edges]

    return render_template(
        'verb.html',
        cls_name=session['classifier'],
        hg=app.config['HG'],
        nav='verbs',
        verb=verb,
        cases=cases)


@app.route('/expand')
def expand_route():
    edge = hedge(request.args.get('edge'))
    edge, pattern = app.config['LEARNER'].generate_expansion_case(edge)
    return redirect('/case/edge?edge={}&pattern={}'.format(quote(str(edge)), quote(str(pattern))))
