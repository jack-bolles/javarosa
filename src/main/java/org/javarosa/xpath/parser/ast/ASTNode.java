/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.xpath.parser.ast;

import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.parser.Token;
import org.javarosa.xpath.parser.XPathSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Enumeration;
import java.util.Vector;

import static org.javarosa.xpath.parser.Token.UMINUS;
import static org.javarosa.xpath.parser.ast.ASTNodeAbstractExpr.CHILD;
import static org.javarosa.xpath.parser.ast.ASTNodePathStep.AXIS_TYPE_EXPLICIT;
import static org.javarosa.xpath.parser.ast.ASTNodePathStep.NODE_TEST_TYPE_FUNC;
import static org.javarosa.xpath.parser.ast.ASTNodePathStep.NODE_TEST_TYPE_QNAME;

public abstract class ASTNode {
    private static final Logger logger = LoggerFactory.getLogger(ASTNode.class);

    public abstract Vector<ASTNode> getChildren();

    public abstract XPathExpression build() throws XPathSyntaxException;

    //horrible debugging code
    int indent;

    private void printStr(String s) {
        StringBuilder padding = new StringBuilder();
        for (int i = 0; i < 2 * indent; i++)
            padding.append(" ");
        logger.info("{}{}", padding, s);
    }

    private void print(Object o) {
        indent += 1;

        if (o instanceof ASTNodeAbstractExpr) {
            ASTNodeAbstractExpr x = (ASTNodeAbstractExpr) o;
            printStr("abstractexpr {");
            for (int i = 0; i < x.content.size(); i++) {
                if (CHILD == x.getType(i))
                    print(x.content.elementAt(i));
                else
                    printStr(x.getToken(i).toString());
            }
            printStr("}");
        } else if (o instanceof ASTNodePredicate) {
            ASTNodePredicate x = (ASTNodePredicate) o;
            printStr("predicate {");
            print(x.expr);
            printStr("}");
        } else if (o instanceof ASTNodeFunctionCall) {
            ASTNodeFunctionCall x = (ASTNodeFunctionCall) o;
            if (x.args.isEmpty()) {
                printStr("func {" + x.name.toString() + ", args {none}}");
            } else {
                printStr("func {" + x.name.toString() + ", args {{");
                for (int i = 0; i < x.args.size(); i++) {
                    print(x.args.elementAt(i));
                    if (i < x.args.size() - 1)
                        printStr(" } {");
                }
                printStr("}}}");
            }
        } else if (o instanceof ASTNodeBinaryOp) {
            ASTNodeBinaryOp x = (ASTNodeBinaryOp) o;
            printStr("opexpr {");
            for (int i = 0; i < x.exprs.size(); i++) {
                print(x.exprs.elementAt(i));
                if (i < x.exprs.size() - 1) {
                    switch (x.ops.elementAt(i)) {
                        case Token.AND: printStr("and:");
                            break;
                        case Token.OR: printStr("or:");
                            break;
                        case Token.EQ: printStr("eq:");
                            break;
                        case Token.NEQ: printStr("neq:");
                            break;
                        case Token.LT: printStr("lt:");
                            break;
                        case Token.LTE: printStr("lte:");
                            break;
                        case Token.GT: printStr("gt:");
                            break;
                        case Token.GTE: printStr("gte:");
                            break;
                        case Token.PLUS: printStr("plus:");
                            break;
                        case Token.MINUS: printStr("minus:");
                            break;
                        case Token.DIV: printStr("div:");
                            break;
                        case Token.MOD: printStr("mod:");
                            break;
                        case Token.MULT: printStr("mult:");
                            break;
                        case Token.UNION: printStr("union:");
                            break;
                        //TODO - default?
                    }
                }
            }
            printStr("}");
        } else if (o instanceof ASTNodeUnaryOp) {
            ASTNodeUnaryOp x = (ASTNodeUnaryOp) o;
            printStr("opexpr {");
            switch (x.op) {
                case UMINUS: printStr("num-neg:");
                    break;
            }
            print(x.expr);
            printStr("}");
        } else if (o instanceof ASTNodeLocPath) {
            ASTNodeLocPath x = (ASTNodeLocPath) o;
            printStr("pathexpr {");
            int offset = x.isAbsolute() ? 1 : 0;
            for (int i = 0; i < x.clauses.size() + offset; i++) {
                if (offset == 0 || i > 0)
                    print(x.clauses.elementAt(i - offset));
                if (i < x.separators.size()) {
                    switch (x.separators.elementAt(i)) {
                        case Token.DBL_SLASH:
                            printStr("dbl-slash:");
                            break;
                        case Token.SLASH:
                            printStr("slash:");
                            break;
                    }
                }
            }
            printStr("}");

        } else if (o instanceof ASTNodePathStep) {
            ASTNodePathStep x = (ASTNodePathStep) o;
            printStr("step {axis: " + x.axisType + " node test type: " + x.nodeTestType);
            if (AXIS_TYPE_EXPLICIT == x.axisType) printStr("  axis type: " + x.axisVal);
            if (NODE_TEST_TYPE_QNAME == x.nodeTestType) printStr("  node test name: " + x.nodeTestQName.toString());
            if (NODE_TEST_TYPE_FUNC == x.nodeTestType) print(x.nodeTestFunc);
            printStr("predicates...");
            for (Enumeration<ASTNode> e = x.predicates.elements(); e.hasMoreElements(); )
                print(e.nextElement());
            printStr("}");
        } else if (o instanceof ASTNodeFilterExpr) {
            ASTNodeFilterExpr x = (ASTNodeFilterExpr) o;
            printStr("filter expr {");
            print(x.expr);
            printStr("predicates...");
            for (Enumeration<ASTNode> e = x.predicates.elements(); e.hasMoreElements(); )
                print(e.nextElement());
            printStr("}");
        }

        indent -= 1;
    }
}