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

package org.javarosa.core.services.properties;

import org.javarosa.core.services.PropertyManager;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.core.services.locale.Localizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

/**
 * A set of rules governing the allowable properties for JavaRosa's
 * core functionality.
 *
 * @author ctsims
 *
 */
public class JavaRosaPropertyRules implements IPropertyRules {
    HashMap<String,ArrayList<String>> rules;

    ArrayList<String> readOnlyProperties;

    public final static String DEVICE_ID_PROPERTY = "DeviceID";
    public final static String CURRENT_LOCALE = "cur_locale";

    public final static String LOGS_ENABLED = "logenabled";

    public final static String LOGS_ENABLED_YES = "Enabled";
    public final static String LOGS_ENABLED_NO = "Disabled";

    /** The expected compliance version for the OpenRosa API set **/
    public final static String OPENROSA_API_LEVEL = "jr_openrosa_api";

    /**
     * Creates the JavaRosa set of property rules
     */
    public JavaRosaPropertyRules() {
        rules = new HashMap<>();
        readOnlyProperties = new ArrayList<>(2);

        //DeviceID Property
        rules.put(DEVICE_ID_PROPERTY, new ArrayList<>(1));
        ArrayList<String> logs = new ArrayList<>(2);
        logs.add(LOGS_ENABLED_NO);
        logs.add(LOGS_ENABLED_YES);
        rules.put(LOGS_ENABLED, logs);

        rules.put(CURRENT_LOCALE, new ArrayList<>(1));

        rules.put(OPENROSA_API_LEVEL, new ArrayList<>(1));

        readOnlyProperties.add(DEVICE_ID_PROPERTY);
        readOnlyProperties.add(OPENROSA_API_LEVEL);

    }

    /** (non-Javadoc)
     *  @see org.javarosa.core.services.properties.IPropertyRules#allowableValues(String)
     */
    public ArrayList<String> allowableValues(String propertyName) {
        if(CURRENT_LOCALE.equals(propertyName)) {
            Localizer l = Localization.getGlobalLocalizerAdvanced();
            String[] locales = l.getAvailableLocales();
            ArrayList<String> v = new ArrayList<>(locales.length);
            Collections.addAll(v, locales);
            return v;
        }
      return rules.get(propertyName);
    }

    /** (non-Javadoc)
     *  @see org.javarosa.core.services.properties.IPropertyRules#checkValueAllowed(String, String)
     */
    public boolean checkValueAllowed(String propertyName, String potentialValue) {
        if(CURRENT_LOCALE.equals(propertyName)) {
            return Localization.getGlobalLocalizerAdvanced().hasLocale(potentialValue);
        }
        ArrayList<String> prop = rules.get(propertyName);
        if(prop.size() != 0) {
            //Check whether this is a dynamic property
            if(prop.size() == 1 && checkPropertyAllowed(prop.get(0))) {
                // If so, get its list of available values, and see whether the potential value is acceptable.
                return PropertyManager.__().getProperty(prop.get(0)).contains(potentialValue);
            }
            else {
                return rules.get(propertyName).contains(potentialValue);
            }
        }
        else
            return true;
    }

    /** (non-Javadoc)
     *  @see org.javarosa.core.services.properties.IPropertyRules#allowableProperties()
     */
    public ArrayList<String> allowableProperties() {
        Set<String> keys = rules.keySet();
        return new ArrayList<>(keys);
    }

    /** (non-Javadoc)
     *  @see org.javarosa.core.services.properties.IPropertyRules#checkPropertyAllowed)
     */
    public boolean checkPropertyAllowed(String propertyName) {
       return rules.containsKey(propertyName);
    }

    /** (non-Javadoc)
     *  @see org.javarosa.core.services.properties.IPropertyRules#checkPropertyUserReadOnly)
     */
    public boolean checkPropertyUserReadOnly(String propertyName){
        return readOnlyProperties.contains(propertyName);
    }

    /*
     * (non-Javadoc)
     * @see org.javarosa.core.services.properties.IPropertyRules#handlePropertyChanges(java.lang.String)
     */
    public void handlePropertyChanges(String propertyName) {
        if(CURRENT_LOCALE.equals(propertyName)) {
            String locale = PropertyManager.__().getSingularProperty(propertyName);
            Localization.setLocale(locale);
        }
    }
}
