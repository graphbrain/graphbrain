from flask import Flask
app = Flask(__name__)


# https://flask.palletsprojects.com/en/2.0.x/patterns/packages/
import graphbrain.web.views
