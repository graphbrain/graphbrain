# TODO: this is no longer woring due to recent Reddit API changes


import time
import datetime
import json
import argparse
import praw


class RedditRetriever(object):
    def __init__(self, _subreddit, _outfile, _start_date, _end_date, step=3600):
        self.r = praw.Reddit(site_name='graphbrain', user_agent='GraphBrain (http://graphbrain.org)')
        self.subreddit = _subreddit
        self.output_file = _outfile
        self.step = step
        self.start_ts = int(time.mktime(datetime.datetime.strptime(_start_date, "%d/%m/%Y").timetuple()))
        self.end_ts = int(time.mktime(datetime.datetime.strptime(_end_date, "%d/%m/%Y").timetuple()))
        self.cur_ts = self.start_ts
        self.posts = 0
        self.comments = 0
        self.retry_wait = 30

    def print_status(self):
        delta_t = self.end_ts - self.start_ts
        done_t = self.cur_ts - self.start_ts
        per = (float(done_t) / float(delta_t)) * 100.
        print('retrieving subreddit: %s [%.2f%% done] --- %s posts; %s comments'
              % (self.subreddit, per, self.posts, self.comments))

    def build_comment(self, comment):
        if hasattr(comment, 'replies'):
            replies = [self.build_comment(reply) for reply in comment.replies if reply is not None]
        else:
            replies = []
        if not hasattr(comment, 'body'):
            return None
        if hasattr(comment, 'author') and comment.author is not None:
            author = comment.author.name
        else:
            author = ''
        self.comments += 1
        return {'id': comment.id,
                'author': author,
                'body': comment.body,
                'score': comment.score,
                'ups': comment.ups,
                'downs': comment.downs,
                'created': comment.created,
                'created_utc': comment.created_utc,
                'comments': replies}

    def comments_tree(self, post):
        top_level_comments = list(post.comments)
        return [self.build_comment(comment) for comment in top_level_comments]

    def retrieve_posts(self):
        for ts in range(self.cur_ts, self.end_ts, self.step):
            self.cur_ts = ts
            query = 'timestamp:%s..%s' % (str(ts), str(ts + self.step))
            self.print_status()
            search_results = self.r.subreddit(self.subreddit).search(query, syntax='cloudsearch')

            for res in search_results:
                comments = self.comments_tree(res)
                post = {'id': res.id,
                        'title': res.title,
                        'author': res.author.name,
                        'permalink': res.permalink.replace('?ref=search_posts', ''),
                        'url': res.url,
                        'selftext': res.selftext,
                        'score': res.score,
                        'ups': res.ups,
                        'downs': res.downs,
                        'created': res.created,
                        'created_utc': res.created_utc,
                        'comments': comments}
                self.posts += 1

                # write to file
                with open(self.output_file, 'a') as file:
                    file.write('%s\n' % json.dumps(post, separators=(',', ':')))

    def run(self):
        print('writing to file: %s' % self.output_file)
        while True:
            try:
                self.retrieve_posts()
                print('done.')
                exit()
            except KeyboardInterrupt:
                exit()
            except SystemExit:
                exit()
            except Exception as e:
                print('exception: %s' % str(e))
                print('retrying in %s seconds...' % self.retry_wait)
                time.sleep(self.retry_wait)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()

    parser.add_argument('--outfile', type=str, help='output file', default=None)
    parser.add_argument('--startdate', type=str, help='start date', default=None)
    parser.add_argument('--enddate', type=str, help='end date', default=None)
    parser.add_argument('--subreddit', type=str, help='subreddit to retrieve.', default=None)

    args = parser.parse_args()

    subreddit = args.subreddit
    outfile = args.outfile
    startdate = args.startdate
    enddate = args.enddate
    rr = RedditRetriever(subreddit, outfile, startdate, enddate)
    rr.run()
