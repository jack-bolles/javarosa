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

package org.javarosa.xform.util;

import org.javarosa.core.model.FormDef;
import org.javarosa.xform.parse.IXFormParserFactory;
import org.javarosa.xform.parse.ParseException;
import org.javarosa.xform.parse.XFormParser;
import org.javarosa.xform.parse.XFormParserFactory;
import org.kxml2.kdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Static Utility methods pertaining to XForms.
 *
 * @author Clayton Sims
 */
public class XFormUtils {
    private static final Logger logger = LoggerFactory.getLogger(XFormUtils.class);

    private static IXFormParserFactory _factory = new XFormParserFactory();

    public static void setXFormParserFactory(IXFormParserFactory factory) {
        _factory = factory;
    }


    /**
     * Parses a form with an external secondary instance, and returns a FormDef.
     *
     * @param is the InputStream containing the form
     * @return a FormDef for the parsed form
     * @throws ParseException if the form can’t be parsed
     */
    public static FormDef getFormFromInputStream(InputStream is) throws ParseException {
        return getFormFromInputStream(is, null);
    }

    /**
     * @param lastSavedSrc The src of the last-saved instance of this form (for auto-filling). If null,
     *                     no data will be loaded and the instance will be blank.
     * @see #getFormFromInputStream(InputStream)
     */
    public static FormDef getFormFromInputStream(InputStream is, String lastSavedSrc) throws ParseException {
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(is, StandardCharsets.UTF_8);

            XFormParser xFormParser = _factory.getXFormParser(isr);
            return xFormParser.parse(null, lastSavedSrc);
        } finally {
            try {
                if (isr != null) {
                    isr.close();
                }
            } catch (IOException e) {
                logger.error("IO Exception while closing stream.", e);
            }
        }
    }


    /////Parser Attribute warning stuff

    private static List<String> getAttributeList(Element e) {
        List<String> atts = new ArrayList<>(e.getAttributeCount());
        for (int i = 0; i < e.getAttributeCount(); i++) {
            atts.add(e.getAttributeName(i));
        }

        return atts;
    }

    private static List<String> getUnusedAttributes(Element e, List<String> usedAtts) {
        List<String> unusedAtts = getAttributeList(e);
        for (String usedAtt : usedAtts) {
            unusedAtts.remove(usedAtt);
        }

        return unusedAtts;
    }

    public static String unusedAttWarning(Element e, List<String> usedAtts) {
        String warning = "Warning: ";
        List<String> unusedAttributes = getUnusedAttributes(e, usedAtts);
        warning += unusedAttributes.size() + " Unrecognized attributes found in Element [" + e.getName() +
                "] and will be ignored: ";
        warning += "[";
        for (int i = 0; i < unusedAttributes.size(); i++) {
            warning += unusedAttributes.get(i);
            if (i != unusedAttributes.size() - 1) warning += ",";
        }
        warning += "] ";

        return warning;
    }

    public static boolean showUnusedAttributeWarning(Element e, List<String> usedAtts) {
        return !getUnusedAttributes(e, usedAtts).isEmpty();
    }

}
