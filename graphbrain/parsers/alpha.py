from sklearn.ensemble import RandomForestClassifier
from sklearn.preprocessing import OneHotEncoder


class Alpha(object):
    def __init__(self, cases_file):
        X = []
        y = []

        with open(cases_file, 'r') as f:
            for line in f.readlines():
                row = line.strip().split('\t')
                true_value = row[0]
                tag = row[3]
                dep = row[4]
                hdep = row[8]

                y.append([true_value])
                X.append((tag, dep, hdep))

        self.encX = OneHotEncoder(handle_unknown='ignore')
        self.encX.fit(X)
        self.ency = OneHotEncoder(handle_unknown='ignore')
        self.ency.fit(y)

        X_ = self.encX.transform(X).toarray()
        y_ = self.ency.transform(y).toarray()

        self.clf = RandomForestClassifier(
            n_estimators=150, criterion='entropy', min_samples_split=25,
            bootstrap=False)
        self.clf.fit(X_, y_)

    def predict(self, X):
        X_ = self.encX.transform(X).toarray()
        preds = self.ency.inverse_transform(self.clf.predict(X_))
        return tuple(pred[0] if pred else 'C' for pred in preds)
