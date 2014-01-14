import os
from crawlsite import CrawlSite

 
class CrawlList:
    def __init__(self, list_file):
        self.list_file = list_file

    def crawl(self):
        with open(self.list_file) as f:
            url_list = f.readlines()

        urls = set(url_list)

        print("{} sites in list".format(len(urls)))

        for u in urls:
            url = u.strip()
            if (url != ''):
                print(">>> Parsing site: {}".format(url))
                c = CrawlSite(url)
                c.crawl()


if __name__ == "__main__":
	c = CrawlList("climate_blogs.txt")
	c.crawl()