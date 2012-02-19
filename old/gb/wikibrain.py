from BeautifulSoup import BeautifulSoup
import re
from re import findall, DOTALL
import urllib2
import sys
#import parser
#import graph
import nltk


def graphdata(term):
    graph_data=[]
    results=wikicontent(term)
    splitsentences=split_into_sentences(results)
    opening_paragraph=splitsentences[0]
    for sentence in opening_paragraph:
        separated=sentencise(sentence)
        for sep in separated:
            try:
                graph_data.append(parse(sep))
            except:
                print_exc(file=strderr)
    
    return graph_data

 


def wikicontent(term):
    opener=urllib2.build_opener()
    opener.addheaders=[('User-agent', 'Mozilla/5.0')]
    url='http://en.wikipedia.org/w/index.php?title='+term+'&printable=yes';
    infile = opener.open(url)
    s=infile.read()

    text=BeautifulSoup(''.join(s))
    text.prettify()
    
    #return paragraphs
    return text.findAll('p')

    
def sentences(text, strip_to_raw):
    sentenceEnds=re.compile('[.!?:][\s]{1,2}(?=[A-Z])')
    split_sentences=sentenceEnds.split(text)
    if strip_to_raw==1:
        return_sentences=[]
        for sentence in split_sentences:
            #Take out html tags
            sentence=nltk.clean_html(sentence)
            #Take out references
            sentence=re.sub('\[\s*[0-9]+\s*\]','', sentence)
            #Take out things in parentheses
            sentence=re.sub('\(.*\)', '', sentence)
            return_sentences.append(sentence)
        return return_sentences
    else:
        #Returns in raw form with links and references.
        return split_sentences


def split_into_sentences(paragraphs, strip_to_raw=1):
    split_sentences=[]
    
    for paragraph in paragraphs:
        results=sentences(str(paragraph), strip_to_raw)
        split_sentences.append(results)
            
    return split_sentences


#Reassembles sentences in text to the required pattern or rejects the text if no possible sentences can be assembled.
def sentencise(text):

#    DATE = re.compile(r'^[1-9][0-9]?(th|st|rd)? (January|...) ([12][0-9][0-9][0-9])')
    
    return_sentences=[]
    
    try:
        tokens=nltk.word_tokenize(text)
        tagged_tokens = nltk.pos_tag(tokens)
    except:
        return ['']
    
    words=[t[0] for t in tagged_tokens]
    tags=[t[1] for t in tagged_tokens]
    
    #Check for the presence of verbs and nouns
    verb_tag=re.compile('V[A-Z]*')
    noun_tag=re.compile('N[A-Z]*')
    
    #print(tagged_tokens)
    if (re.search(verb_tag, str(tags))==None or re.search(noun_tag, str(tags))==None or re.match(verb_tag, tags[0])):
        #Get rid of unwanted sentences
        return ['']
        
    else:
        conj=re.compile('(\s*,\s*)|,?(and)')
        from_stub=''
        from_phrases=['']
        rel=['']
        to_stub=''
        to_phrases=['']
        from_counter=0
        to_counter=0
        rel_counter=0
        index=0
        rel_index=0
        for token_tag in tagged_tokens:
            
            if(re.match(verb_tag, token_tag[1])):
                if(re.match(conj, words[index-1])):
                    rel.append(token_tag[0])
                    rel_counter+=1
                else:
                    rel[rel_counter]=token_tag[0]
                    
                #save the index where last verb found
                rel_index=index
                #TODO: If list before verb, singularise
            
            elif(index<=rel_index):
                
                if(re.match(conj, token_tag[0])):
                    if((re.match(noun_tag, tags[index+1]) or (tags[index+1]=='JJ')) and re.match(noun_tag, tags[index-1])):
                        from_phrases.append(from_stub)                    
                        from_counter+=1
                    else:
                        return[]
                elif(re.match(noun_tag, token_tag[1])):
                    if(from_counter==0):
                        from_stub=from_phrases[0]
                    from_phrases[from_counter]=from_phrases[from_counter]+token_tag[0]+' '
                else:
                    from_stub += token_tag[0]+' ' 
                    from_phrases[from_counter]=from_stub

                rel_index+=1
            elif(index>rel_index):
                if(re.match(conj, token_tag[0])):
                    if(re.match(noun_tag, tags[index+1]) or (tags[index+1]=='JJ')):
                        to_phrases.append(to_stub)
                        to_counter+=1
                    else:
                        return[]
                
                elif(re.match(noun_tag, token_tag[1])):
                    if(to_counter==0):
                        to_stub=to_phrases[0]
                    to_phrases[to_counter]=to_phrases[to_counter]+token_tag[0]+' '
                    
                else:
                    to_phrases[to_counter]=to_phrases[to_counter]+token_tag[0]+' '
                    
            index+=1
            
            
        if(from_phrases[0]=='' or to_phrases[0]=='' or rel[0]==''):
            return []
                
        from_list=range(from_counter+1)
        rel_list=range(rel_counter+1)
        to_list=range(to_counter+1)
        for i in from_list:
            for j in rel_list:
                for k in to_list:
                    return_sentences.append(from_phrases[i]+' '+rel[j]+' ' +to_phrases[k])
                    
        return return_sentences
    


def main():    
    term=sys.argv[1];
    results=(wikicontent(term))
    sentence_results=split_into_sentences(results)

    opening_paragraph=sentence_results[0]
    for sentence in opening_paragraph:
        print(sentencise(sentence))
    

if __name__=='__main__':
    main()



