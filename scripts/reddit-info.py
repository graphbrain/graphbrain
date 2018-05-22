import sys
import json


class RedditReader(object):
    def __init__(self):
        self.threads = 0
        self.comments = 0
        self.authors = set()
        self.max_comments = 0

    def process_comments(self, comments):
        if comments:
            for comment in comments:
                if comment:
                    self.comments += 1
                    if 'author' in comment:
                        self.authors.add(comment['author'])
                    if 'comments' in comment:
                        self.process_comments(comment['comments'])

    def process_thread(self, thread):
        self.threads += 1
        if 'author' in thread:
            self.authors.add(thread['author'])

        comments_before = self.comments
        if 'comments' in thread:
            self.process_comments(thread['comments'])
        comments = self.comments - comments_before
        if comments > self.max_comments:
            self.max_comments = comments

    def read_file(self, filename):
        with open(filename, 'r') as f:
            for line in f:
                thread = json.loads(line)
                self.process_thread(thread)

        print('threads: %s' % self.threads)
        print('comments: %s' % self.comments)
        print('mean comments per thread: %.2f' % (float(self.comments) / float(self.threads)))
        print('largest thread: %s comments' % self.max_comments)
        print('authors: %s' % len(self.authors))


if __name__ == '__main__':
    RedditReader().read_file(sys.argv[1])
