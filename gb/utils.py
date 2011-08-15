import random
import string


def _random_string(length):
        rg = random.SystemRandom()
        alphabet = string.letters[0:52] + string.digits
        return str().join(rg.choice(alphabet) for _ in range(length))


if __name__ == '__main__':
    print _random_string(10)
