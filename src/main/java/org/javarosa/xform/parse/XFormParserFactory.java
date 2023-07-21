package org.javarosa.xform.parse;

import org.kxml2.kdom.Document;

import java.io.Reader;

public class XFormParserFactory implements IXFormParserFactory {

    public XFormParser getXFormParser(Reader reader) {
        return new XFormParser(reader);
    }

    public XFormParser getXFormParser(Document doc) {
        return new XFormParser(doc);
    }

    public XFormParser getXFormParser(Reader form, Reader instance) {
        return new XFormParser(form, instance);
    }

    public XFormParser getXFormParser(Document form, Document instance) {
        return new XFormParser(form, instance);
    }

}