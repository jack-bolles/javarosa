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

package org.javarosa.core.services;

import org.javarosa.core.services.properties.IPropertyRules;
import org.javarosa.core.services.properties.Property;
import org.javarosa.core.services.storage.IStorageUtilityIndexed;
import org.javarosa.core.services.storage.StorageFullException;
import org.javarosa.core.services.storage.StorageManager;
import org.javarosa.core.util.externalizable.Externalizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * PropertyManager is a class that is used to set and retrieve name/value pairs
 * from persistent storage.
 * <p>
 * Which properties are allowed, and what they can be set to, can be specified by an implementation of
 * the IPropertyRules interface, any number of which can be registered with a property manager. All
 * property rules are inclusive, and can only increase the number of potential properties or property
 * values.
 *
 * @author Clayton Sims
 */
public class PropertyManager implements IPropertyManager {
    private static final Logger logger = LoggerFactory.getLogger(PropertyManager.class);

    private static IPropertyManager instance; //a global instance of the property manager

    public static void setPropertyManager(IPropertyManager pm) {
        instance = pm;
    }

    public static void initDefaultPropertyManager() {
        StorageManager.registerStorage(PropertyManager.STORAGE_KEY, Property.class);
        setPropertyManager(new PropertyManager());
    }

    public static IPropertyManager __() {
        if (instance == null) {
            initDefaultPropertyManager();
        }
        return instance;
    }

    public static final String STORAGE_KEY = "PROPERTY";

    private final List<IPropertyRules> rulesList;

    private final IStorageUtilityIndexed<? extends Externalizable> properties;

    public PropertyManager() {
        this.properties = (IStorageUtilityIndexed<? extends Externalizable>) StorageManager.getStorage(STORAGE_KEY);
        rulesList = new ArrayList<>(0);
    }

    /**
     * Retrieves the singular property specified, as long as it exists in one of the current rules
     *
     * @param propertyName the name of the property being retrieved
     * @return The String value of the property specified if it exists, is singular, and is in one the current
     * rules. null if the property is denied by the current ruleset, or is a vector.
     */
    public String getSingularProperty(String propertyName) {
        String retVal = null;
        if ((rulesList.size() == 0 || checkPropertyAllowed(propertyName))) {
            List<String> value = getValue(propertyName);
            if (value != null && value.size() == 1) {
                retVal = value.get(0);
            }
        }
        if (retVal == null) {
            logger.warn("Singular property request failed for property {}", propertyName);
        }
        return retVal;
    }


    /**
     * Retrieves the property specified, as long as it exists in one of the current rules
     *
     * @param propertyName the name of the property being retrieved
     * @return The String value of the property specified if it exists, and is the current ruleset, if one exists.
     * null if the property is denied by the current ruleset.
     */
    public List<String> getProperty(String propertyName) {
        if (rulesList.size() == 0) {
            return getValue(propertyName);
        } else {
            if (checkPropertyAllowed(propertyName)) {
                return getValue(propertyName);
            } else {
                return null;
            }
        }
    }

    /**
     * Sets the given property to the given string value, if both are allowed by any existing ruleset
     *
     * @param propertyName  The property to be set
     * @param propertyValue The value that the property will be set to
     */
    public void setProperty(String propertyName, String propertyValue) {
        List<String> wrapper = new ArrayList<>(1);
        wrapper.add(propertyValue);
        setProperty(propertyName, wrapper);
    }

    /**
     * Sets the given property to the given vector value, if both are allowed by any existing ruleset
     *
     * @param propertyName  The property to be set
     * @param propertyValue The value that the property will be set to
     */
    public void setProperty(String propertyName, List<String> propertyValue) {
        List<String> oldValue = getProperty(propertyName);
        if (oldValue != null && listEquals(oldValue, propertyValue)) {
            //No point in redundantly setting values!
            return;
        }
        if (rulesList.size() == 0) {
            writeValue(propertyName, propertyValue);
        } else {
            boolean valid = true;
            for (String pv : propertyValue) {
                // RL - checkPropertyAllowed is implicit in checkValueAllowed
                if (!checkValueAllowed(propertyName, pv)) {
                    valid = false;
                }
            }
            if (valid) {
                writeValue(propertyName, propertyValue);
                notifyChanges(propertyName);
            } else {
                logger.info("Property Manager: Unable to write value ({}) to {}", propertyValue, propertyName);
            }
        }

    }

    private boolean listEquals(List<String> v1, List<String> v2) {
        if (v1.size() != v2.size()) {
            return false;
        } else {
            for (int i = 0; i < v1.size(); ++i) {
                if (!v1.get(i).equals(v2.get(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Retrieves the set of rules being used by this property manager if any exist.
     *
     * @return The rules being used by this property manager
     */
    public List<IPropertyRules> getRules() {
        return rulesList;
    }

    /**
     * Sets the rules that should be used by this PropertyManager, removing any other
     * existing rules sets.
     *
     * @param rules The rules to be used.
     */
    public void setRules(IPropertyRules rules) {
        this.rulesList.clear();
        this.rulesList.add(rules);
    }

    /**
     * Checks that a property is permitted to exist by any of the existing rules sets
     *
     * @param propertyName The name of the property to be set
     * @return true if the property is permitted to store values. false otherwise
     */
    public boolean checkPropertyAllowed(String propertyName) {
        if (rulesList.size() == 0) {
            return true;
        } else {
            boolean allowed = false;
            //We're fine if we return true, inclusive rules sets
            for (IPropertyRules rules : rulesList) {
                if (rules.checkPropertyAllowed(propertyName)) {
                    allowed = true;
                    break;
                }
            }
            return allowed;
        }
    }

    /**
     * Checks that a property is allowed to store a certain value.
     *
     * @param propertyName  The name of the property to be set
     * @param propertyValue The value to be stored in the given property
     * @return true if the property given is allowed to be stored. false otherwise.
     */
    public boolean checkValueAllowed(String propertyName,
                                     String propertyValue) {
        if (rulesList.size() == 0) {
            return true;
        } else {
            boolean allowed = false;
            for (IPropertyRules rules : rulesList) {
                if (rules.checkPropertyAllowed(propertyName)) {
                    if (rules.checkValueAllowed(propertyName, propertyValue)) {
                        allowed = true;
                        break;
                    }
                }
            }
            return allowed;
        }
    }

    /**
     * Identifies the property rules set that the property belongs to, and notifies
     * it about the property change.
     *
     * @param property The property that has been changed
     */
    private void notifyChanges(String property) {
        if (rulesList.size() == 0) {
            return;
        }

        for (IPropertyRules therules : rulesList) {
            if (therules.checkPropertyAllowed(property)) {
                therules.handlePropertyChanges(property);
            }
        }
    }

    public List<String> getValue(String name) {
        try {
            Property p = (Property) properties.getRecordForValue("NAME", name);
            return p.value;
        } catch (NoSuchElementException nsee) {
            return null;
        }
    }

    public void writeValue(String propertyName, List<String> value) {
        Property theProp = new Property();
        theProp.name = propertyName;
        theProp.value = value;

        List<Integer> IDs = properties.getIDsForValue("NAME", propertyName);
        if (IDs.size() == 1) {
            theProp.setID(IDs.get(0));
        }

        try {
            properties.write(theProp);
        } catch (StorageFullException e) {
            throw new RuntimeException("uh-oh, storage full [properties]"); //TODO: handle this
        }
    }

}
