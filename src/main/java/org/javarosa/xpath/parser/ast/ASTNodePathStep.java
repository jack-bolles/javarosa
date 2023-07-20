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
import org.javarosa.xpath.parser.XPathSyntaxException;

import java.util.Vector;

import static org.javarosa.xpath.expr.XPathStep.TEST_TYPE_COMMENT;
import static org.javarosa.xpath.expr.XPathStep.TEST_TYPE_NODE;
import static org.javarosa.xpath.expr.XPathStep.TEST_TYPE_PROCESSING_INSTRUCTION;
import static org.javarosa.xpath.expr.XPathStep.TEST_TYPE_TEXT;

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
        if (NODE_TEST_TYPE_ABBR_DOT == nodeTestType) {
            return XPathStep.ABBR_SELF();
        } else if (NODE_TEST_TYPE_ABBR_DBL_DOT == nodeTestType) {
            return XPathStep.ABBR_PARENT();
        } else {
            XPathStep step;

            if (AXIS_TYPE_NULL == axisType)
                axisVal = XPathStep.AXIS_CHILD;
            else if (AXIS_TYPE_ABBR == axisType)
                axisVal = XPathStep.AXIS_ATTRIBUTE;

            if (NODE_TEST_TYPE_QNAME == nodeTestType)
                step = new XPathStep(axisVal, nodeTestQName);
            else if (NODE_TEST_TYPE_WILDCARD == nodeTestType)
                step = new XPathStep(axisVal, XPathStep.TEST_NAME_WILDCARD);
            else if (NODE_TEST_TYPE_NSWILDCARD == nodeTestType)
                step = new XPathStep(axisVal, nodeTestNamespace);
            else {
                String funcName = nodeTestFunc.name.toString();
                int type;
                switch (funcName) {
                    case "node":
                        type = TEST_TYPE_NODE;
                        break;
                    case "text":
                        type = TEST_TYPE_TEXT;
                        break;
                    case "comment":
                        type = TEST_TYPE_COMMENT;
                        break;
                    case "processing-instruction":
                        type = TEST_TYPE_PROCESSING_INSTRUCTION;
                        break;
                    default:
                        throw new RuntimeException();
                }

                step = new XPathStep(axisVal, type);
                if (!nodeTestFunc.args.isEmpty()) {
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
}
