package com.graphbrain.eco;

import com.graphbrain.db.Vertex;
import com.graphbrain.eco.nodes.*;
import com.graphbrain.eco.nodes.NlpFun.NlpFunType;
import com.graphbrain.eco.nodes.FilterFun.FilterFunType;
import com.graphbrain.eco.nodes.VertexFun.VertexFunType;
import com.graphbrain.eco.nodes.WordsFun.WordsFunType;

import java.util.LinkedList;
import java.util.List;

public class Parser {

    private Token[] tokens;
    private ProgNode expr;

    public Parser(String input) {
        List<Token> lstTokens = new Lexer(input).tokens();
        tokens = lstTokens.toArray(new Token[lstTokens.size()]);
        expr = parse(0);
    }

    private ProgNode parse(int pos) {
        switch(tokens[pos].getTtype()) {
            case LPar: return parseList(pos);
            case String: return new StringNode(tokens[pos].getText(), pos);
            case Symbol: return parseSymbol(pos);
            case Number: return new NumberNode(Double.parseDouble(tokens[pos].getText()), pos);
            case Vertex: return new VertexNode(Vertex.fromId(tokens[pos].getText()), pos);
            default: return null; // error
        }
    }

    private ProgNode parseSymbol(int pos) {
        String s = tokens[pos].getText();

        if (s.equals("true"))
            return new BoolNode(true, pos);
        if (s.equals("false"))
            return new BoolNode(false, pos);

        return parseVar(pos);
    }

    private ProgNode parseVar(int pos) {
        return new VarNode(tokens[pos].getText(), pos);
    }

    private boolean matchOpeningPar(int pos) {
        return tokens[pos].getTtype() == TokenType.LPar;
    }

    private boolean matchClosingPar(int pos) {
        return tokens[pos].getTtype() == TokenType.RPar;
    }

    private ProgNode parseList(int pos) {
        if (tokens[pos + 1].getTtype() == TokenType.Symbol) {
            return parseFun(pos + 1);
        }

        return null; //error
    }

    private ProgNode parseFun(int pos) {
        String text = tokens[pos].getText();

        if (text.equals("wv")) return parseWV(pos + 1);
        if (text.equals("ww")) return parseWW(pos + 1);
        if (text.equals("let")) return parseLet(pos + 1);
        if (text.equals("!")) return parseNotFun(pos + 1);
        if (text.equals("build")) return parseBuildVert(pos + 1);
        if (text.equals(":wv")) return parseWVRecursion(pos + 1);
        if (text.equals(":ww")) return parseWWRecursion(pos + 1);
        if (text.equals("rel-vert")) return parseRelVert(pos + 1);
        if (text.equals("txt-vert")) return parseTxtVert(pos + 1);
        if (text.equals("is-pos")) return parseNlpFun(NlpFunType.IS_POS, pos + 1);
        if (text.equals("is-pos-pre")) return parseNlpFun(NlpFunType.IS_POSPRE, pos + 1);
        if (text.equals("are-pos")) return parseNlpFun(NlpFunType.ARE_POS, pos + 1);
        if (text.equals("are-pos-pre")) return parseNlpFun(NlpFunType.ARE_POSPRE, pos + 1);
        if (text.equals("contains-pos")) return parseNlpFun(NlpFunType.CONTAINS_POS, pos + 1);
        if (text.equals("contains-pos-pre")) return parseNlpFun(NlpFunType.CONTAINS_POSPRE, pos + 1);
        if (text.equals("is-lemma")) return parseNlpFun(NlpFunType.IS_LEMMA, pos + 1);
        if (text.equals("max-depth")) return parseMaxDepth(pos + 1);
        if (text.equals("filter-min")) return parseFilter(FilterFunType.FilterMin, pos + 1);
        if (text.equals("filter-max")) return parseFilter(FilterFunType.FilterMax, pos + 1);
        if (text.equals("len")) return parseLenFun(pos + 1);
        if (text.equals("pos")) return parsePosFun(pos + 1);
        if (text.equals("+")) return parseSum(pos + 1);
        if (text.equals("ends-with")) return parseEndsWith(pos + 1);
        if (text.equals("flatten")) return parseFlatten(pos + 1);
        if (text.equals("conj")) return parseVerbFun(VerbFun.VerbFunType.Conj, pos + 1);
        if (text.equals("tense")) return parseVerbFun(VerbFun.VerbFunType.Tense, pos + 1);
        return parseDummy(text, pos + 1);

    }

    private ProgNode parseWV(int pos) {
        ProgNode p1 = parsePattern(pos);
        ProgNode p2 = parseConds(p1.getLastTokenPos() + 1);
        ProgNode p3 = parse(p2.getLastTokenPos() + 1);

        if (matchClosingPar(p3.getLastTokenPos() + 1))
            return new WVRule(new ProgNode[]{p1, p2, p3}, p3.getLastTokenPos() + 1);
        else
            return null; // error
    }

    private ProgNode parseWW(int pos) {
        ProgNode p1 = parsePattern(pos);
        ProgNode p2 = parseConds(p1.getLastTokenPos() + 1);
        ProgNode p3 = parse(p2.getLastTokenPos() + 1);

        if (matchClosingPar(p3.getLastTokenPos() + 1))
            return new WWRule(new ProgNode[]{p1, p2, p3}, p3.getLastTokenPos() + 1);
        else
            return null; // error
    }

    private Object[] parseElems(int pos) {
        //(Array[ProgNode], Int)
        if (!matchOpeningPar(pos))
            return null; // error

        List<ProgNode> lstParams = parseElemList(pos + 1);
        ProgNode[] params = lstParams.toArray(new ProgNode[lstParams.size()]);

        int lastParamsTokenPos;

        if (params.length == 0)
            lastParamsTokenPos = pos;
        else
            lastParamsTokenPos = params[params.length - 1].getLastTokenPos();

        if (!matchClosingPar(lastParamsTokenPos + 1))
            return null; // error

        return new Object[]{params, lastParamsTokenPos + 1};
    }

    private void parseElemList_r(List<ProgNode> l, int pos) {
        if (!matchClosingPar(pos)) {
            ProgNode elem = parse(pos);
            l.add(elem);
            parseElemList_r(l, elem.getLastTokenPos() + 1);
        }
    }

    private List<ProgNode> parseElemList(int pos) {
        List<ProgNode> l = new LinkedList<>();
        parseElemList_r(l, pos);
        return l;
    }

    private ProgNode parseConds(int pos) {
        Object[] e = parseElems(pos);
        return new CondsFun((ProgNode[])e[0], (Integer)e[1]);
    }

    private void parseParamsList_r(List<ProgNode> l, int pos) {
        if (!matchClosingPar(pos)) {

            ProgNode param;

            switch(tokens[pos].getTtype()) {
                case String:
                    param = new StringNode(tokens[pos].getText(), pos);
                    break;
                case Symbol:
                    param = parseVar(pos);
                    break;
                case LPar:
                    param = parseFun(pos + 1);
                    break;
                case Number:
                    param = new NumberNode(Double.parseDouble(tokens[pos].getText()), pos);
                    break;
                default:
                    param = null; // error
            }

            if (param != null) {
                l.add(param);
                parseParamsList_r(l, param.getLastTokenPos() + 1);
            }
        }
    }

    private List<ProgNode> parseParamsList(int pos) {
        List<ProgNode> l = new LinkedList<>();
        parseParamsList_r(l, pos);
        return l;
    }

    private ProgNode parsePattern(int pos) {
        if (!matchOpeningPar(pos))
            return null; // error

        List<ProgNode> lstParams = parseParamsList(pos + 1);
        ProgNode[] params = lstParams.toArray(new ProgNode[parseParamsList(pos + 1).size()]);

        int lastParamsTokenPos;
        if (params.length == 0)
            lastParamsTokenPos = pos + 1;
        else
            lastParamsTokenPos = params[params.length - 1].getLastTokenPos();

        if (!matchClosingPar(lastParamsTokenPos + 1))
            return null; // error

        return new PatFun(params, lastParamsTokenPos + 1);
    }

    private ProgNode parseLet(int pos) {
        ProgNode p1 = parse(pos);
        ProgNode p2 = parse(p1.getLastTokenPos() + 1);

        if (matchClosingPar(p2.getLastTokenPos() + 1))
            return new LetFun(new ProgNode[]{p1, p2}, p2.getLastTokenPos() + 1);
        else
            return null; // error
    }

    private ProgNode parseBuildVert(int pos) {
        int lastPos = pos;
        List<ProgNode> paramList = new LinkedList<>();

        while (!matchClosingPar(lastPos)) {
            ProgNode p = parse(lastPos);
            lastPos = p.getLastTokenPos() + 1;
            paramList.add(p);
        }

        ProgNode[] params = paramList.toArray(new ProgNode[paramList.size()]);
        return new VertexFun(VertexFunType.BuildVert, params, lastPos);
    }

    private ProgNode parseRelVert(int pos) {
        ProgNode p1 = parse(pos);

        if (matchClosingPar(p1.getLastTokenPos() + 1))
            return new VertexFun(VertexFunType.RelVert, new ProgNode[]{p1}, p1.getLastTokenPos() + 1);
        else
            return null; // error
    }

    private ProgNode parseTxtVert(int pos) {
        ProgNode p1 = parse(pos);

        if (matchClosingPar(p1.getLastTokenPos() + 1))
            return new VertexFun(VertexFunType.TxtVert, new ProgNode[]{p1}, p1.getLastTokenPos() + 1);
        else
            return null; // error
    }

    private ProgNode parseDummy(String name, int pos) {
        int lastPos = pos;
        List<ProgNode> paramList = new LinkedList<>();

        while (!matchClosingPar(lastPos)) {
            ProgNode p = parse(lastPos);
            lastPos = p.getLastTokenPos() + 1;
            paramList.add(p);
        }

        ProgNode[] params = paramList.toArray(new ProgNode[paramList.size()]);
        return new DummyFun(name, params, lastPos);
    }

    private ProgNode parseNlpFun(NlpFunType ftype, int pos) {
        ProgNode[] params = (ProgNode[])parseParamsList(pos).toArray();

        int lastParamsTokenPos;
        if (params.length == 0)
            lastParamsTokenPos = pos;
        else
            lastParamsTokenPos = params[params.length - 1].getLastTokenPos();

        if (matchClosingPar(lastParamsTokenPos + 1))
            return new NlpFun(ftype, params, lastParamsTokenPos + 1);
        else
            return null; // error
    }

    private ProgNode parseWVRecursion(int pos) {
        ProgNode p1 = parse(pos);

        if (matchClosingPar(p1.getLastTokenPos() + 1))
            return new WVRecursion(new ProgNode[]{p1}, p1.getLastTokenPos() + 1);
        else
            return null; // error
    }

    private ProgNode parseWWRecursion(int pos) {
        ProgNode p1 = parse(pos);

        if (matchClosingPar(p1.getLastTokenPos() + 1))
            return new WWRecursion(new ProgNode[]{p1}, p1.getLastTokenPos() + 1);
        else
            return null; // error
    }

    private ProgNode parseNotFun(int pos) {
        ProgNode p1 = parse(pos);

        if (matchClosingPar(p1.getLastTokenPos() + 1))
            return new NotFun(new ProgNode[]{p1}, p1.getLastTokenPos() + 1);
        else
            return null; // error
    }

    private ProgNode parseMaxDepth(int pos) {
        ProgNode p1 = parse(pos);

        if (matchClosingPar(p1.getLastTokenPos() + 1))
            return new MaxDepthFun(new ProgNode[]{p1}, p1.getLastTokenPos() + 1);
        else
            return null; // error
    }

    private ProgNode parseFilter(FilterFunType fun, int pos) {
        ProgNode p1 = parse(pos);

        if (matchClosingPar(p1.getLastTokenPos() + 1))
            return new FilterFun(fun, new ProgNode[]{p1}, p1.getLastTokenPos() + 1);
        else
            return null; // error
    }

    private ProgNode parseLenFun(int pos) {
        ProgNode p1 = parse(pos);

        if (matchClosingPar(p1.getLastTokenPos() + 1))
            return new LenFun(new ProgNode[]{p1}, p1.getLastTokenPos() + 1);
        else
            return null; // error
    }

    private ProgNode parsePosFun(int pos) {
        ProgNode p1 = parse(pos);

        if (matchClosingPar(p1.getLastTokenPos() + 1))
            return new PosFun(new ProgNode[]{p1}, p1.getLastTokenPos() + 1);
        else
            return null; // error
    }

    private ProgNode parseSum(int pos) {
        List<ProgNode> progNodes = parseParamsList(pos);
        ProgNode[] params = progNodes.toArray(new ProgNode[progNodes.size()]);
        int lastParamsTokenPos;
        if (params.length == 0)
            lastParamsTokenPos = pos;
        else
            lastParamsTokenPos = params[params.length - 1].getLastTokenPos();

        if (!matchClosingPar(lastParamsTokenPos + 1))
            return null; // error

        return new SumFun(params, lastParamsTokenPos + 1);
    }

    private ProgNode parseEndsWith(int pos) {
        ProgNode p1 = parse(pos);
        ProgNode p2 = parse(p1.getLastTokenPos() + 1);

        if (matchClosingPar(p2.getLastTokenPos() + 1))
            return new WordsFun(WordsFunType.EndsWith, new ProgNode[]{p1, p2}, p2.getLastTokenPos() + 1);
        else
            return null; // error
    }

    private ProgNode parseFlatten(int pos) {
        ProgNode p = parse(pos);

        if (matchClosingPar(p.getLastTokenPos() + 1))
            return new VertexFun(VertexFunType.Flatten, new ProgNode[]{p}, p.getLastTokenPos() + 1);
        else
            return null; // error
    }

    private ProgNode parseVerbFun(VerbFun.VerbFunType ftype, int pos) {
        switch (ftype) {
            case Conj:
                ProgNode p1 = parse(pos);
                ProgNode p2 = parse(p1.getLastTokenPos() + 1);
                ProgNode p3 = parse(p2.getLastTokenPos() + 1);

                if (matchClosingPar(p3.getLastTokenPos() + 1))
                    return new VerbFun(VerbFun.VerbFunType.Conj, new ProgNode[]{p1, p2, p3}, p3.getLastTokenPos() + 1);
                else
                    return null; // error
            case Tense:
                ProgNode p = parse(pos);
                if (matchClosingPar(p.getLastTokenPos() + 1))
                    return new VerbFun(VerbFun.VerbFunType.Tense, new ProgNode[]{p}, p.getLastTokenPos() + 1);
                else
                    return null; // error
            default:
                return null; // error
        }
    }

    public ProgNode getExpr() {
        return expr;
    }
}