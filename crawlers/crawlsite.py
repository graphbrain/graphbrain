
from bs4 import BeautifulSoup
import urllib.request, urllib.error, urllib.parse
from urllib.parse import urlparse
import os
import collections

 
class CrawlSite:
    def __init__(self, baseurl):
        self.baseurl = baseurl

        self.basedir = "crawldata/" + self.url2path(baseurl)

        self.pages = set()
        self.queue = collections.deque()
        self.count = 0

        self.stop_fragments = ("comment-page", "langswitch_lang", "comments_popup", "wpmp_switcher", "submit=search", "share=")
        self.stop_endings = (".png", ".gif", ".jpg", ".jpeg", ".pdf", ".r")

        # create base dir if it does not exist
        if not os.path.exists(self.basedir):
    	    os.makedirs(self.basedir)

    def crawl(self):
        self.queue.append(self.baseurl)

        while len(self.queue) > 0:
            self.process_page(self.queue.pop())

    def url2path(self, urlstr):
        return str.replace(urlstr, "/", "_")

    def save_page(self, page_url_str):
        url = urlparse(page_url_str)

        if url.fragment != '':
            return False

        urlstr = page_url_str.lower()

        for s in self.stop_fragments:
        	if s in urlstr:
        		return False

        for s in self.stop_endings:
        	if urlstr.endswith(s):
        		return False

        return True

    def process_page(self, page):
        self.pages.add(page)

        self.count += 1
        print(("#{} {}".format(self.count, page)))

        try:
            content = urllib.request.urlopen(page).read()
        except urllib.error.URLError as error:
            print("URLError: {}".format(error))
            return
        except UnicodeEncodeError as error:
            print("UnicodeEnocodeError: {}".format(error))
            return

        try:
            soup = BeautifulSoup(content)
        except TypeError as error:
            print("TypeError: {}".format(error))
            return

        path = self.url2path(page)

        with open(self.basedir + "/" + path, "w") as text_file:
    	    text_file.write(str(soup))

        for link in soup.find_all('a'):
            urlstr = link.get('href')

            if (urlstr != '') and (not urlstr is None):
                if urlstr.startswith(self.baseurl):
                    if not urlstr in self.pages:
                        if self.save_page(urlstr):
                            self.queue.append(urlstr)
                            

if __name__ == "__main__":
	c = CrawlSite("http://www.realclimate.org")
	c.crawl()