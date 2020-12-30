from sklearn.ensemble import RandomForestClassifier
from sklearn.preprocessing import OneHotEncoder


class Alpha(object):
    def __init__(self, cases_str):
        X = []
        y = []

        for line in cases_str.strip().split('\n'):
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

        self.clf = RandomForestClassifier(random_state=777)
        self.clf.fit(X_, y_)

    def predict(self, X):
        X_ = self.encX.transform(X).toarray()
        preds = self.ency.inverse_transform(self.clf.predict(X_))
        return tuple(pred[0] if pred else 'C' for pred in preds)
