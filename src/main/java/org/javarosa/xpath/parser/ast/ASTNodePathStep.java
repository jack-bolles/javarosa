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
import org.javarosa.xpath.expr.XPathQName;
import org.javarosa.xpath.expr.XPathStep;
import org.javarosa.xpath.parser.Token;
import org.javarosa.xpath.parser.XPathSyntaxException;

import java.util.Vector;

public class ASTNodePathStep extends ASTNode {
    public static final int AXIS_TYPE_ABBR = 1;
    public static final int AXIS_TYPE_EXPLICIT = 2;
    public static final int AXIS_TYPE_NULL = 3;

    public static final int NODE_TEST_TYPE_QNAME = 1;
    public static final int NODE_TEST_TYPE_WILDCARD = 2;
    public static final int NODE_TEST_TYPE_NSWILDCARD = 3;
    public static final int NODE_TEST_TYPE_ABBR_DOT = 4;
    public static final int NODE_TEST_TYPE_ABBR_DBL_DOT = 5;
    public static final int NODE_TEST_TYPE_FUNC = 6;

    public int axisType;
    public int axisVal;
    public int nodeTestType;
    public ASTNodeFunctionCall nodeTestFunc;
    public XPathQName nodeTestQName;
    public String nodeTestNamespace;
    public Vector<ASTNode> predicates;

    public ASTNodePathStep () {
        predicates = new Vector<>();
    }

    public Vector<ASTNode> getChildren() {
        return predicates;
    }

    public XPathExpression build() {
        return null;
    }

    public XPathStep getStep () throws XPathSyntaxException {
        if (nodeTestType == NODE_TEST_TYPE_ABBR_DOT) {
            return XPathStep.ABBR_SELF();
        } else if (nodeTestType == NODE_TEST_TYPE_ABBR_DBL_DOT) {
            return XPathStep.ABBR_PARENT();
        } else {
            XPathStep step;

            if (axisType == AXIS_TYPE_NULL)
                axisVal = XPathStep.AXIS_CHILD;
            else if (axisType == AXIS_TYPE_ABBR)
                axisVal = XPathStep.AXIS_ATTRIBUTE;

            if (nodeTestType == NODE_TEST_TYPE_QNAME)
                step = new XPathStep(axisVal, nodeTestQName);
            else if (nodeTestType == NODE_TEST_TYPE_WILDCARD)
                step = new XPathStep(axisVal, XPathStep.TEST_NAME_WILDCARD);
            else if (nodeTestType == NODE_TEST_TYPE_NSWILDCARD)
                step = new XPathStep(axisVal, nodeTestNamespace);
            else {
                String funcName = nodeTestFunc.name.toString();
                int type;
                switch (funcName) {
                    case "node":
                        type = XPathStep.TEST_TYPE_NODE;
                        break;
                    case "text":
                        type = XPathStep.TEST_TYPE_TEXT;
                        break;
                    case "comment":
                        type = XPathStep.TEST_TYPE_COMMENT;
                        break;
                    case "processing-instruction":
                        type = XPathStep.TEST_TYPE_PROCESSING_INSTRUCTION;
                        break;
                    default:
                        throw new RuntimeException();
                }

                step = new XPathStep(axisVal, type);
                if (nodeTestFunc.args.size() > 0) {
                    step.literal = (String)((ASTNodeAbstractExpr)nodeTestFunc.args.elementAt(0)).getToken(0).val;
                }
            }

            XPathExpression[] preds = new XPathExpression[predicates.size()];
            for (int i = 0; i < preds.length; i++)
                preds[i] = predicates.elementAt(i).build();
            step.predicates = preds;

            return step;
        }
    }

    public static int validateAxisName (String axisName) {
        int axis = -1;

        switch (axisName) {
            case "child":
                axis = XPathStep.AXIS_CHILD;
                break;
            case "descendant":
                axis = XPathStep.AXIS_DESCENDANT;
                break;
            case "parent":
                axis = XPathStep.AXIS_PARENT;
                break;
            case "ancestor":
                axis = XPathStep.AXIS_ANCESTOR;
                break;
            case "following-sibling":
                axis = XPathStep.AXIS_FOLLOWING_SIBLING;
                break;
            case "preceding-sibling":
                axis = XPathStep.AXIS_PRECEDING_SIBLING;
                break;
            case "following":
                axis = XPathStep.AXIS_FOLLOWING;
                break;
            case "preceding":
                axis = XPathStep.AXIS_PRECEDING;
                break;
            case "attribute":
                axis = XPathStep.AXIS_ATTRIBUTE;
                break;
            case "namespace":
                axis = XPathStep.AXIS_NAMESPACE;
                break;
            case "self":
                axis = XPathStep.AXIS_SELF;
                break;
            case "descendant-or-self":
                axis = XPathStep.AXIS_DESCENDANT_OR_SELF;
                break;
            case "ancestor-or-self":
                axis = XPathStep.AXIS_ANCESTOR_OR_SELF;
                break;
        }

        return axis;
    }

    public static boolean validateNodeTypeTest (ASTNodeFunctionCall f) {
        String name = f.name.toString();
        if (name.equals("node") || name.equals("text") || name.equals("comment") || name.equals("processing-instruction")) {
            if (f.args.size() == 0) {
                return true;
            } else if (name.equals("processing-instruction") && f.args.size() == 1) {
                ASTNodeAbstractExpr x = (ASTNodeAbstractExpr)f.args.elementAt(0);
                return x.content.size() == 1 && x.getTokenType(0) == Token.STR;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

}
