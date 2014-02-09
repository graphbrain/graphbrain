package com.graphbrain.eco.nodes;

import com.graphbrain.eco.*;
import com.graphbrain.eco.conjugator.EnglishConjugator;
import com.graphbrain.eco.conjugator.Person;
import com.graphbrain.eco.conjugator.VerbTense;

public class VerbFun extends FunNode {
    private static EnglishConjugator conj = new EnglishConjugator();

    public enum VerbFunType {
        Conj, Tense
    }

    private VerbFunType fun;

    public VerbFun(VerbFunType fun, ProgNode[] params, int lastTokenPos) {
        super(params, lastTokenPos);
        this.fun = fun;
    }

    @Override
    public String label() {
        switch(fun) {
            case Conj: return "conj";
            case Tense: return "tense";
            default: return "?";
        }
    }

    @Override
    public NodeType ntype(Context ctxt) {
        switch(fun) {
            case Conj:
                return NodeType.Words;
            case Tense:
                return NodeType.Number;
            default:
                return NodeType.Unknown;
        }
    }

    private VerbTense int2tense(int tense) {
        switch (tense) {
            case 0:
                return VerbTense.PAST;
            case 1:
                return VerbTense.PAST_PARTICIPLE;
            case 2:
                return VerbTense.PAST_PERFECT;
            case 3:
                return VerbTense.PAST_PERFECT_PARTICIPLE;
            case 4:
                return VerbTense.PERFECT;
            case 5:
                return VerbTense.PRESENT;
            case 6:
                return VerbTense.PRESENT_PARTICIPLE;
            default:
                return VerbTense.PRESENT;
        }
    }

    private Person int2person(int person) {
        switch (person) {
            case 1:
                return Person.FIRST_PERSON_SINGULAR;
            case 2:
                return Person.SECOND_PERSON_SINGULAR;
            case 3:
                return Person.THIRD_PERSON_SINGULAR;
            case 4:
                return Person.FIRST_PERSON_PLURAL;
            case 5:
                return Person.SECOND_PERSON_PLURAL;
            case 6:
                return Person.THIRD_PERSON_PLURAL;
            default:
                return Person.THIRD_PERSON_SINGULAR;
        }
    }

    @Override
    public void eval(Contexts ctxts) {
        switch(fun) {
            case Conj:
                for (ProgNode p : params) {
                    p.eval(ctxts);
                }

                for (Context c : ctxts.getCtxts()) {
                    VerbTense tense = int2tense((int)c.getRetNumber(params[1]));
                    Person person = int2person((int)c.getRetNumber(params[2]));

                    ProgNode p = params[0];
                    switch(p.ntype(c)) {
                        case Words:
                            Words verb = c.getRetWords(params[0]);
                            Words res = new Words(verb);

                            for (Word w : res.getWords()) {
                                w.setWord(conj.conjugate(w.getLemma(), tense, person));
                            }
                            c.setRetWords(this, res);
                            break;
                        default:
                            // error
                            break;
                    }
                }
                break;
            case Tense:
                ProgNode p = params[0];

                p.eval(ctxts);

                for (Context c : ctxts.getCtxts()) {
                    switch(p.ntype(c)) {
                        case Words:
                            // TODO: deal with more verbs
                            Word verb = c.getRetWords(params[0]).getWords()[0];

                            int tense = -1;
                            if (verb.getPos().equals("VBP")) {
                                tense = 5;
                            }
                            else if (verb.getPos().equals("VBZ")) {
                                tense = 5;
                            }
                            else if (verb.getPos().equals("VBD")) {
                                tense = 0;
                            }
                            c.setRetNumber(this, tense);
                            break;
                        default: // error!
                    }
                }
                break;
        }
    }
}