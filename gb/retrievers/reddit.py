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


import time
import datetime
import praw
import json


class RedditRetriever(object):
    def __init__(self, subreddit, outfile, start_date, end_date, step=3600):
        self.r = praw.Reddit(site_name='graphbrain', user_agent='GraphBrain (http://graphbrain.org)')
        self.subreddit = subreddit
        self.output_file = outfile
        self.step = step
        self.start_ts = int(time.mktime(datetime.datetime.strptime(start_date, "%d/%m/%Y").timetuple()))
        self.end_ts = int(time.mktime(datetime.datetime.strptime(end_date, "%d/%m/%Y").timetuple()))
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
    RedditRetriever('worldnews', 'test.json', '25/01/2016', '26/01/2016').run()
