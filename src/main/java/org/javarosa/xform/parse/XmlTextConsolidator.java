package org.javarosa.xform.parse;

import org.kxml2.kdom.Element;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import static org.kxml2.kdom.Node.ELEMENT;
import static org.kxml2.kdom.Node.TEXT;

class XmlTextConsolidator {

    /**
     * According to Clayton Sims, in Feb 2012:
     * For escaped unicode strings we end up with a lot of cruft, so we really
     * want to go through and convert the kxml parsed text (which have lots of
     * characters each as their own string) into one single string
     */
    static void consolidateText(Element rootElement) {
        Stack<Element> q = new Stack<>();
        q.push(rootElement);
        while (!q.isEmpty()) {
            Element e = q.pop();
            Set<Integer> removeIndexes = new HashSet<>();
            String accumulator = "";
            for (int i = 0; i < e.getChildCount(); ++i) {
                int type = e.getType(i);
                if (type == TEXT) {
                    accumulator += e.getText(i);
                    removeIndexes.add(i);
                } else {
                    if (type == ELEMENT) {
                        q.add(e.getElement(i));
                    }
                    if (nonBlank(accumulator)) {
                        e.addChild(i++, TEXT, accumulator);
                    }
                    accumulator = "";
                }
            }
            if (nonBlank(accumulator)) {
                e.addChild(TEXT, accumulator);
            }
            ElementChildDeleter.delete(e, removeIndexes);
        }
    }

    private static boolean nonBlank(String s) {
        return !s.trim().isEmpty();
    }

}
