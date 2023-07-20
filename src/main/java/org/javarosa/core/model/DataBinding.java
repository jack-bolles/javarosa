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

package org.javarosa.core.model;

import org.javarosa.core.model.condition.IConditionExpr;
import org.javarosa.core.model.condition.Triggerable;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapNullable;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A data binding is an object that represents how a
 * data element is to be used in a form entry interaction.
 * <p>
 * It contains a reference to where the data should be retreived
 * and stored, as well as the preload parameters, and the
 * conditional logic for the question.
 * <p>
 * The class relies on any Data References that are used
 * in a form to be registered with the FormDefRMSUtility's
 * prototype factory in order to properly deserialize.
 *
 * @author Drew Roos
 */
public class DataBinding implements Externalizable {
    private String id;
    private IDataReference ref;
    private int dataType;

    public Triggerable relevancyCondition;
    public boolean relevantAbsolute;
    public Triggerable requiredCondition;
    public boolean requiredAbsolute;
    public Triggerable readonlyCondition;
    public boolean readonlyAbsolute;
    public IConditionExpr constraint;
    public Triggerable calculate;

    private String preload;
    private String preloadParams;
    public String constraintMessage;

    private final List<TreeElement> additionalAttrs = new ArrayList<>(0);

    public DataBinding() {
        relevantAbsolute = true;
        requiredAbsolute = false;
        readonlyAbsolute = false;
    }

    public IDataReference getReference() {
        return ref;
    }

    public void setReference(IDataReference ref) {
        this.ref = ref;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public String getPreload() {
        return preload;
    }

    public void setPreload(String preload) {
        this.preload = preload;
    }

    public String getPreloadParams() {
        return preloadParams;
    }

    public void setPreloadParams(String preloadParams) {
        this.preloadParams = preloadParams;
    }

    public void setAdditionalAttribute(String namespace, String name, String value) {
        TreeElement.setAttribute(null, additionalAttrs, namespace, name, value);
    }

    public List<TreeElement> getAdditionalAttributes() {
        return additionalAttrs;
    }

    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
        setId((String) ExtUtil.read(in, new ExtWrapNullable(String.class), pf));
        setDataType(ExtUtil.readInt(in));
        setPreload((String) ExtUtil.read(in, new ExtWrapNullable(String.class), pf));
        setPreloadParams((String) ExtUtil.read(in, new ExtWrapNullable(String.class), pf));
        ref = (IDataReference) ExtUtil.read(in, new ExtWrapTagged());

        //don't bother reading relevancy/required/readonly/constraint/calculate/additionalAttrs right now; they're only used during parse anyway
    }

    public void writeExternal(DataOutputStream out) throws IOException {
        ExtUtil.write(out, new ExtWrapNullable(getId()));
        ExtUtil.writeNumeric(out, getDataType());
        ExtUtil.write(out, new ExtWrapNullable(getPreload()));
        ExtUtil.write(out, new ExtWrapNullable(getPreloadParams()));
        ExtUtil.write(out, new ExtWrapTagged(ref));

        //don't bother writing relevancy/required/readonly/constraint/calculate/additionalAttrs right now; they're only used during parse anyway
    }


}
