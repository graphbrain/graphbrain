#!/usr/bin/env python


from flask import Flask
from flask import render_template


app = Flask(__name__)


@app.route("/")
def hello():
    nodes_json = '[{"id":0, "parent":"", "text":"Hello World!"}]'
    return render_template('node.html', nodes_json=nodes_json, links_json='[]')


if __name__ == "__main__":
    app.debug = True
    app.run()
