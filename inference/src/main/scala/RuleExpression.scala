abstract class RuleExpression
case class StringExpression(exp:String) extends RuleExpression
case class POS(exp:String) extends RuleExpression //A POS expression is a string containing a sequence of POS tags.
case class REGEX(exp:String) extends RuleExpression
case class GRAPH2(source:String, relation:String, target:String) extends RuleExpression
case class RULE(condition:RuleExpression, input:RuleExpression, output:RuleExpression) extends RuleExpression
case class COMPOSITE(exp1:RuleExpression, operator:String, exp2:RuleExpression) extends RuleExpression