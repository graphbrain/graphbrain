import org.scalatest.FunSuite


class POSTaggerTest extends FunSuite {

	val testSentence="I am a person"
	
	

	test("tag string") {
		val tagged = POSTagger.tagText(testSentence)
		for (wordTag <- tagged){
			assert(
				wordTag match{
					case ("I", tag) => tag=="PRP"
					case ("am", tag) => tag=="VBP"
					case ("a", tag) => tag=="DT"
					case ("person", tag) => tag=="NN"
				}
			)
			
		}

	}

	test("token sequence"){

		assert(POSTagger.getTokenSequence(testSentence)=="PRP VBP DT NN")
	}


}