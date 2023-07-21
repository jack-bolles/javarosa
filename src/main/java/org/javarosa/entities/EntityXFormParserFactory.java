package org.javarosa.entities;

import org.javarosa.entities.internal.EntityFormParseProcessor;
import org.javarosa.xform.parse.IXFormParserFactory;
import org.javarosa.xform.parse.XFormParser;
import org.kxml2.kdom.Document;

import java.io.Reader;

public class EntityXFormParserFactory implements IXFormParserFactory {

    @Override
    public XFormParser getXFormParser(Reader reader) {
        return configureEntityParsing(new XFormParser(reader));
    }

    @Override
    public XFormParser getXFormParser(Document doc) {
        return configureEntityParsing(new XFormParser(doc));
    }

    @Override
    public XFormParser getXFormParser(Reader form, Reader instance) {
        return configureEntityParsing(new XFormParser(form, instance));
    }

    @Override
    public XFormParser getXFormParser(Document form, Document instance) {
        return configureEntityParsing(new XFormParser(form, instance));
    }

    private XFormParser configureEntityParsing(XFormParser xFormParser) {
        EntityFormParseProcessor processor = new EntityFormParseProcessor();
        xFormParser.addProcessor(processor);

        return xFormParser;
    }

}
