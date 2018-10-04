class Word:
    """Generic NLP word."""

    def __init__(self):
        self.text = None
        self.prob = None
        self.vector = None
        self.sword = None

    def similarity(self, other):
        return self.sword.similarity(other.sword)

    def __eq__(self, other):
        if isinstance(other, Word):
            return self.text == other.text
        return NotImplemented

    def __str__(self):
        return self.text

    def __repr__(self):
        return self.__str__()

    def __hash__(self):
        return hash(self.text)
