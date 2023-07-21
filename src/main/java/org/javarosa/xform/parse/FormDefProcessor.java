package org.javarosa.xform.parse;

import org.javarosa.core.model.FormDef;

public interface FormDefProcessor extends Processor {
    void processFormDef(FormDef formDef) throws ParseException;
}
