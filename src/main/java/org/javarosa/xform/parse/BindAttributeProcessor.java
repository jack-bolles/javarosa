package org.javarosa.xform.parse;

import kotlin.Pair;
import org.javarosa.core.model.DataBinding;

import java.util.Set;

public interface BindAttributeProcessor extends Processor {

    Set<Pair<String, String>> getBindAttributes();

    void processBindAttribute(String name, String value, DataBinding binding);
}
