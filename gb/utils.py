import random
import string
import time


def random_string(length):
    rg = random.SystemRandom()
    alphabet = string.letters[0:52] + string.digits
    return str().join(rg.choice(alphabet) for _ in range(length))


def timestamp():
    return int(time.time())
