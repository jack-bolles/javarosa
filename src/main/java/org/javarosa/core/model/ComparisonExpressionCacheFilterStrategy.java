package org.javarosa.core.model;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.FilterStrategy;
import org.javarosa.core.model.instance.DataInstance;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.xpath.expr.XPathCmpExpr;
import org.javarosa.xpath.expr.XPathEqExpr;
import org.javarosa.xpath.expr.XPathExpression;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Caches down stream evaluations (in the {@link FilterStrategy} chain) for supported expressions - currently just
 * {@link XPathCmpExpr} and {@link XPathEqExpr}. Repeated evaluations are fetched in O(1) time.
 */
public class ComparisonExpressionCacheFilterStrategy implements FilterStrategy {

    private final Map<String, List<TreeReference>> cachedEvaluations = new HashMap<>();

    @NotNull
    @Override
    public List<TreeReference> filter(@NotNull DataInstance sourceInstance, @NotNull TreeReference nodeSet, @NotNull XPathExpression predicate, @NotNull List<TreeReference> children, @NotNull EvaluationContext evaluationContext, @NotNull Supplier<List<TreeReference>> next) {
        if (sourceInstance.getInstanceId() == null) {
            return next.get();
        }

        CompareToNodeExpression candidate = CompareToNodeExpression.parse(predicate);
        if (candidate != null) {
            Object absoluteValue = candidate.evalContextSide(sourceInstance, evaluationContext);
            String key = nodeSet.toString() + predicate + candidate.getNodeSide() + absoluteValue.toString();

            if (cachedEvaluations.containsKey(key)) {
                return cachedEvaluations.get(key);
            } else {
                List<TreeReference> filtered = next.get();
                cachedEvaluations.put(key, filtered);
                return filtered;
            }
        } else {
            return next.get();
        }
    }

}
