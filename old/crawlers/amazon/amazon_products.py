import bottlenose
from BeautifulSoup import BeautifulSoup

#Keys for my account (not graphbrain's)
AWS_KEY='AKIAIW4QSHKCZIMI5MTQ'
SECRET_KEY='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'
AFF_TAG='chihchun-20'

SEARCH_CATS=["Actor", "Artist", "AudienceRating", "Author", "Brand", "BrowseNode", "City", "Composer", "Conductor", "Director", "Keywords", "Manufacturer", "MusicLabel", "Neighbourhood", "Orchestra", "Power", "Publisher", "TextStream", "Title"];

amazon=bottlenose.Amazon(AWS_KEY, SECRET_KEY, AFF_TAG)

def initAPI(country='us'):
	return API(AWS_KEY, SECRET_KEY, AFF_TAG, country);

def findFilm(title, director, country='us'):
	api=initAPI(country);

	if(director==''):
		rs=amazon.ItemSearch(SearchIndex="DVD",Title=filmTitle,Sort="relevancerank")
		results=BeautifulSoup(''.join(rs));
		urls=results.findAll('url')
		if(len(urls)>=1):
			#Just get the top result (as returned by relevance rank)
			return urls[0];
	else:
		rs=amazon.ItemSearch(SearchIndex="DVD",Title=title,Director=director,Sort="relevancerank")
		results=BeautifulSoup(''.join(rs));
		urls=results.findAll('url')
		if(len(urls)>=1):
			return urls[0];
	return None;

def processDVDNodes(mfilms, graph):
	q = mfilms.find(timeout=False)
	
	for film in film_nodes:
		title=film['title'];
		director=''
		if 'directors' in film.keys():
			directors = film['directors']
			director=directors[0];	

		dvdURL=findFilm(title, director)
		if(dvdUrl!=None):
			# update db
    		dvd={'title': title, 'url': dvdURL}
    		dvdNode=Node().create_or_get_by_eid(label=dvdURL, graph=graph, eid=dvdURL, crawler='amazon', node_type='sale');
    		graph.add_link(dvdNode, film, 'DVD of', 'DVD of');
    
		

if __name__=='__main__':
	graph_owner = 'gb@graphbrain.com'
    graph_name = 'Main'
    movie_graph_name = 'Movies'
    u = User().get_by_email(graph_owner)
    movie_graph = Graph().get_by_owner_and_name(u, movie_graph_name)

    db = Connection().cinema
    # synch films
    mfilms = db.films
    total = mfilms.count()
    count = 1
    
    getDVDNodes(mfilms, movie_graph);



    
    albums_graph_name = 'Albums'
    
