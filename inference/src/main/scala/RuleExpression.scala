abstract class RuleExpression
case class StringExpression(exp:String) extends RuleExpression
case class POS(exp:String) extends RuleExpression //A POS expression is a string containing a sequence of POS tags.
case class REGEX(exp:String) extends RuleExpression
case class GRAPH2(source:String, relation:String, target:String) extends RuleExpression
case class GRAPH2Pair(g1:GRAPH2, g2:GRAPH2)//This data structure is used to pass two graphs in to merge.
case class StringPair(s1:StringExpression, s2:StringExpression)
case class RULE(condition:RuleExpression, input:RuleExpression, output:RuleExpression) extends RuleExpression
case class COMPOSITE(exp1:RuleExpression, operator:String, exp2:RuleExpression) extends RuleExpression