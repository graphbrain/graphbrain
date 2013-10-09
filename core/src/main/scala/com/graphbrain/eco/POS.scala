package com.graphbrain.eco

// http://www.computing.dcu.ie/~acahill/tagset.html

object POS extends Enumeration {
  type POS = Value
	val CC,       // Coordinating conjunction e.g. and,but,or...
      CD,       // Cardinal Number
      DT,       // Determiner
      EX,       // Existential there
      FW,       // Foreign Word
      IN,       // Preposision or subordinating conjunction
      JJ,       // Adjective
      JJR,      // Adjective, comparative
      JJS,      // Adjective, superlative
      LS,       // List Item Marker
      MD,       // Modal e.g. can, could, might, may...
      NN,       // Noun, singular or mass
      NNP,      // Proper Noun, singular
      NNPS,     // Proper Noun, plural
      NNS,      // Noun, plural
      PDT,      // Predeterminer e.g. all, both ... when they precede an article
      POS,      // Possessive Ending e.g. Nouns ending in 's
      PRP,      // Personal Pronoun e.g. I, me, you, he...
      PRP$,     // Possessive Pronoun e.g. my, your, mine, yours...
      RB,       // Adverb Most words that end in -ly as well as degree words like quite, too and very
      RBR,      // Adverb, comparative Adverbs with the comparative ending -er, with a strictly comparative meaning.
      RBS,      // Adverb, superlative
      RP,       // Particle
      SYM,      // Symbol Should be used for mathematical, scientific or technical symbols
      TO,       // to
      UH,       // Interjection e.g. uh, well, yes, my...
      VB,       // Verb, base form subsumes imperatives, infinitives and subjunctives
      VBD,      // Verb, past tense includes the conditional form of the verb to be
      VBG,      // Verb, gerund or persent participle
      VBN,      // Verb, past participle
      VBP,      // Verb, non-3rd person singular present
      VBZ,      // Verb, 3rd person singular present
      WDT,      // Wh-determiner e.g. which, and that when it is used as a relative pronoun
      WP,       // Wh-pronoun e.g. what, who, whom...
      WP$,      // Possessive wh-pronoun
      WRB       // Wh-adverb e.g. how, where why
        = Value

  def fromString(s: String) = s match {
    case "CC" => CC
    case "CD" => CD
    case "DT" => DT
    case "EX" => EX
    case "FW" => FW
    case "IN" => IN
    case "JJ" => JJ
    case "JJR" => JJR
    case "JJS" => JJS
    case "LS" => LS
    case "MD" => MD
    case "NN" => NN
    case "NNP" => NNP
    case "NNPS" => NNPS
    case "NNS" => NNS
    case "PDT" => PDT
    case "POS" => POS
    case "PRP" => PRP
    case "PRP$" => PRP$
    case "RB" => RB
    case "RBR" => RBR
    case "RBS" => RBS
    case "RP" => RP
    case "SYM" => SYM
    case "TO" => TO
    case "UH" => UH
    case "VB" => VB
    case "VBD" => VBD
    case "VBG" => VBG
    case "VBN" => VBN
    case "VBP" => VBP
    case "VBZ" => VBZ
    case "WDT" => WDT
    case "WP" => WP
    case "WP$" => WP$
    case "WRB" => WRB
  }
}
