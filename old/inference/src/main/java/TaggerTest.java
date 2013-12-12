
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;


import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class TaggerTest {

  public static void main(String[] args) throws Exception {
    
    MaxentTagger tagger = new MaxentTagger("pos_models/english-bidirectional-distsim.tagger");
    System.out.println(tagger.tagString("I am a person"));

  }

}
