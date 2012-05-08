# (c) 2012 GraphBrain Ltd. All rigths reserved.


trim = (str) ->
  str.replace(/^\s+/g,'').replace(/\s+$/g,'')


class SentenceParser
    constructor: ->

    setSentence: (sentence) ->
    	@parts = sentence.split(":")
    	@orig = trim(@parts[0])
    	@rel = trim(@parts[1])
    	@targ = trim(@parts[2])

    print: ->
    	console.log(@orig + " [" + @rel + "]-> " + @targ)