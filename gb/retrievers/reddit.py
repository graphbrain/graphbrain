import time
import datetime
import praw


class RedditRetriever(object):
    def __init__(self, subreddit, step=3600):
        self.r = praw.Reddit(site_name='graphbrain', user_agent='GraphBrain (http://graphbrain.org)')
        self.subreddit = subreddit
        self.step = step

    def retrieve_posts(self, start_date, end_date):
        start_ts = int(time.mktime(datetime.datetime.strptime(start_date, "%d/%m/%Y").timetuple()))
        end_ts = int(time.mktime(datetime.datetime.strptime(end_date, "%d/%m/%Y").timetuple()))
        for ts in range(start_ts, end_ts, self.step):
            query = 'timestamp:%s..%s' % (str(ts), str(ts + self.step))
            print(query)
            search_results = self.r.subreddit(self.subreddit).search(query, syntax='cloudsearch')

            for res in search_results:
                top_level_comments = list(res.comments)
                for comment in top_level_comments:
                    print(vars(comment))
                post = {'id': res.id,
                        'title': res.title,
                        'author': res.author.name,
                        'permalink': res.permalink.replace('?ref=search_posts', ''),
                        'url': res.url,
                        'selftext': res.selftext,
                        'score': res.score,
                        'ups': res.ups,
                        'downs': res.downs,
                        'created': res.created_utc}
                #print(post)


if __name__ == '__main__':
    retriever = RedditRetriever('worldnews')
    retriever.retrieve_posts('25/01/2016', '26/01/2016')
