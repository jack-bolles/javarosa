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

package org.javarosa.xpath;

import org.javarosa.core.log.FatalException;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.IConditionExpr;
import org.javarosa.core.model.condition.pivot.UnpivotableExpressionException;
import org.javarosa.core.model.instance.DataInstance;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.xpath.expr.XPathBinaryOpExpr;
import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.javarosa.xpath.expr.XPathPathExpr;
import org.javarosa.xpath.expr.XPathUnaryOpExpr;
import org.javarosa.xpath.parser.XPathSyntaxException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class XPathConditional implements IConditionExpr {
    private XPathExpression expr;
    public String xpath; //not serialized!
    public boolean hasNow; //indicates whether this XpathConditional contains the now() function (used for timestamping)

    public XPathConditional (String xpath) throws XPathSyntaxException {
        hasNow = xpath.contains("now()");
        this.expr = XPathParseTool.parseXPath(xpath);
        this.xpath = xpath;
    }

    public XPathConditional (XPathExpression expr) {
        this.expr = expr;
    }

    public XPathConditional () {

    }

    public XPathExpression getExpr () {
        return expr;
    }

    public Object evalRaw (DataInstance model, EvaluationContext evalContext) {
        try{
            return XPathFuncExpr.unpack(expr.eval(model, evalContext));
        } catch(XPathUnsupportedException e){
            if(xpath != null){
                throw new XPathUnsupportedException(xpath);
            }else{
                throw e;
            }


        }
    }

    public boolean eval (DataInstance model, EvaluationContext evalContext) {
        return XPathFuncExpr.toBoolean(evalRaw(model, evalContext));
    }

    public String evalReadable (DataInstance model, EvaluationContext evalContext) {
        return XPathFuncExpr.toString(evalRaw(model, evalContext));
    }

    public List<TreeReference> evalNodeset (DataInstance model, EvaluationContext evalContext) {
        if (expr instanceof XPathPathExpr) {
            return ((XPathPathExpr)expr).eval(model, evalContext).getReferences();
        } else {
            throw new FatalException("evalNodeset: must be path expression");
        }
    }

    public Set<TreeReference> getTriggers (TreeReference contextRef) {
        Set<TreeReference> triggers = new HashSet<>();
        getTriggers(expr, contextRef, contextRef, triggers);
        return triggers;
    }

    private static void getTriggers(XPathExpression x, TreeReference contextRef, TreeReference originalContext, Set<TreeReference> triggersSoFar) {
        if (x instanceof XPathPathExpr) {
            TreeReference ref = ((XPathPathExpr)x).getReference();
            TreeReference contextualized = ref;
            if (contextRef != null
                || (ref.getContextType() == TreeReference.CONTEXT_ORIGINAL && originalContext != null)) {
                contextualized = ref.contextualize(ref.getContextType() == TreeReference.CONTEXT_ORIGINAL ? originalContext : contextRef);
            }

            // TODO: It's possible we should just handle this the same way as "genericize". Not entirely clear.
            triggersSoFar.add(contextualized.removePredicates());

            for (int i = 0; i < contextualized.size() ; i++) {
                List<XPathExpression> predicates = contextualized.getPredicate(i);
                if (predicates == null) {
                    continue;
                }

                if (!contextualized.isAbsolute()) {
                    throw new IllegalArgumentException("can't get triggers for relative references");
                }
                TreeReference predicateContext = contextualized.getSubReference(i).removePredicates();

                for (XPathExpression predicate : predicates) {
                    getTriggers(predicate, predicateContext, originalContext, triggersSoFar);
                }
            }
        } else if (x instanceof XPathBinaryOpExpr) {
            getTriggers(((XPathBinaryOpExpr)x).a, contextRef, originalContext, triggersSoFar);
            getTriggers(((XPathBinaryOpExpr)x).b, contextRef, originalContext, triggersSoFar);
        } else if (x instanceof XPathUnaryOpExpr) {
            getTriggers(((XPathUnaryOpExpr)x).a, contextRef, originalContext, triggersSoFar);
        } else if (x instanceof XPathFuncExpr) {
            XPathFuncExpr fx = (XPathFuncExpr)x;
            for (int i = 0; i < fx.args.length; i++)
                getTriggers(fx.args[i], contextRef, originalContext, triggersSoFar);
        }
    }

    public boolean equals (Object o) {
        if (o instanceof XPathConditional) {
            XPathConditional cond = (XPathConditional)o;
            return expr.equals(cond.expr);
        } else {
            return false;
        }
    }

    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
        expr = (XPathExpression)ExtUtil.read(in, new ExtWrapTagged(), pf);
        hasNow = ExtUtil.readBool(in);
    }

    public void writeExternal(DataOutputStream out) throws IOException {
        ExtUtil.write(out, new ExtWrapTagged(expr));
        ExtUtil.writeBool(out, hasNow);
    }

    public String toString () {
        return "xpath[" + expr.toString() + "]";
    }

    public List<Object> pivot(DataInstance model, EvaluationContext evalContext) throws UnpivotableExpressionException {
        return expr.pivot(model, evalContext);
    }
}
