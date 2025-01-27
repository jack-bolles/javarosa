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

package org.javarosa.core.api;

/**
 * The Module Interface represents an integration point
 * for an extensible set of JavaRosa code. A Module is
 * used to configure a set of components with any application
 * which might use them.
 *  
 * @author Clayton Sims
 *
 */
public interface IModule {
    /**
     * Register Module should identify all configuration that
     * needs to occur for the elements that are contained within
     * a module, and perform that configuration and registration
     * with the current application.
     */
    void registerModule();
}
