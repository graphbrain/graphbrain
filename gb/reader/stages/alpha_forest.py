#   Copyright (c) 2016 CNRS - Centre national de la recherche scientifique.
#   All rights reserved.
#
#   Written by Telmo Menezes <telmo@telmomenezes.com>
#
#   This file is part of GraphBrain.
#
#   GraphBrain is free software: you can redistribute it and/or modify
#   it under the terms of the GNU Affero General Public License as published by
#   the Free Software Foundation, either version 3 of the License, or
#   (at your option) any later version.
#
#   GraphBrain is distributed in the hope that it will be useful,
#   but WITHOUT ANY WARRANTY; without even the implied warranty of
#   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#   GNU Affero General Public License for more details.
#
#   You should have received a copy of the GNU Affero General Public License
#   along with GraphBrain.  If not, see <http://www.gnu.org/licenses/>.


import pickle
import pandas as pd
from sklearn.ensemble import RandomForestClassifier


def learn(infile, outfile):
    train = pd.read_csv(infile)
    train = pd.get_dummies(train, columns=train.columns.values[1:])

    feature_cols = train.columns.values[1:]
    target_cols = [train.columns.values[0]]

    features = train.as_matrix(feature_cols)
    targets = train.as_matrix(target_cols)

    rf = RandomForestClassifier(n_estimators=100)
    rf.fit(features, targets)

    # results = rf.predict(features)
    score = rf.score(features, targets)
    print('score: %s' % score)

    with open(outfile, 'wb') as f:
        pickle.dump(rf, f)


if __name__ == '__main__':
    learn('cases.csv', 'alpha_forest.model')
