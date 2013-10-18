package com.graphbrain.eco

import scala.collection.mutable
import edu.stanford.nlp.parser.lexparser.LexicalizedParser
import edu.stanford.nlp.process.{CoreLabelTokenFactory, PTBTokenizer}
import edu.stanford.nlp.trees.Tree
import java.io.StringReader

class Contexts(s: String) {
  val ctxts = mutable.ListBuffer[Context]()
  val sentence = parseSentence(s)

  private val addCtxts = mutable.ListBuffer[Context]()
  private val remCtxts = mutable.ListBuffer[Context]()

  def addContext(c: Context) = addCtxts += c
  def remContext(c: Context) = remCtxts += c

  def applyChanges() = {
    for (c <- addCtxts) ctxts += c
    for (c <- remCtxts) ctxts -= c
    addCtxts.clear()
    remCtxts.clear()
  }

  private def parseSentence(s: String) = {
    val rawWords = Contexts.tokenizerFactory.getTokenizer(new StringReader(s)).tokenize()
    val parse = Contexts.lp.apply(rawWords)

    tree2Phrase(parse.children()(0))
  }

  private def tree2Phrase(parse: Tree): Phrase = {
    val children = if ((parse.children().length == 1) && (parse.children()(0).isLeaf))
      Array[Phrase]()
    else
      parse.children().map(tree2Phrase(_))

    new Phrase(parse.value(),
      parse.yieldWords().toArray().map(_.toString).reduceLeft(_ + " " + _), children)
  }

  def print() = {
    for (c <- ctxts) c.print()
  }
}

object Contexts {
  val lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz")
  //val lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/frenchFactored.ser.gz")

  val tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "")

  def main(args: Array[String]) = {
    val c = new Contexts("For one thing, Lu felt jQuery constructed the rendering tree the wrong way around and opted to emulate the way games handled rendering instead.")
    //val c = new Contexts("Les mouvements sont accusés de racisme.")
    println(c.sentence)
    println(c.sentence.text)
  }
}