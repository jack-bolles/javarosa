package org.javarosa.xform.parse;

import kotlin.Pair;

import java.util.Set;

public interface ModelAttributeProcessor extends Processor {

    Set<Pair<String, String>> getModelAttributes();

    void processModelAttribute(String name, String value) throws ParseException;
}
