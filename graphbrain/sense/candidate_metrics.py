class CandidateMetrics(object):
    def __init__(self):
        self.score = 0.
        self.degree = 0

    def better_than(self, other):
        if not isinstance(other, CandidateMetrics):
            return NotImplemented
        if self.score != other.score:
            return self.score > other.score
        elif self.degree != other.degree:
            return self.degree > other.degree
        else:
            return False

    def __str__(self):
        return 'score: %s; degree: %s' % (self.score, self.degree)

    def __repr__(self):
        return self.__str__()
