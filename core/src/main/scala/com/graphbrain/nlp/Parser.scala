package com.graphbrain.nlp

import java.io.StringReader
import edu.stanford.nlp.process.CoreLabelTokenFactory
import edu.stanford.nlp.process.PTBTokenizer
import edu.stanford.nlp.trees._
import edu.stanford.nlp.parser.lexparser.LexicalizedParser

object Parser {

  def main(args: Array[String]) = {
    //val lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz")
    val lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/frenchFactored.ser.gz")

    println("xpto...")

    //val sent2 = "It answered my question about how to include both the data and the lib"
    //val sent2 = "This package is a Java implementation of probabilistic natural language parsers, both highly optimized PCFG and lexicalized dependency parsers, and a lexicalized PCFG parser."
    //val sent2 = "Les mouvements actuellement classés à l'extrême droite en Europe sont souvent accusés de racisme et de xénophobie en raison de leur hostilité générale à l'immigration et des positions ouvertement racistes revendiquées par certains d'entre eux."
    val sent2 = "Les mouvements sont accusés de racisme."

    val tokenizerFactory =
      PTBTokenizer.factory(new CoreLabelTokenFactory(), "")
    val rawWords2 =
      tokenizerFactory.getTokenizer(new StringReader(sent2)).tokenize()
    val parse = lp.apply(rawWords2)

    /*
    val tlp = new PennTreebankLanguagePack()
    val gsf = tlp.grammaticalStructureFactory()
    val gs = gsf.newGrammaticalStructure(parse)
    val tdl = gs.typedDependenciesCCprocessed()
    println(tdl)
    println()
    */

    //val tp = new TreePrint("penn,typedDependenciesCollapsed")
    val tp = new TreePrint("penn")
    tp.printTree(parse)

    for (p <- parse.children()(0).children()) println("xxx> " + p)
  }
}