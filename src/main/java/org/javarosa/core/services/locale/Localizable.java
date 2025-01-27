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

package org.javarosa.core.services.locale;

/**
 * Localizable objects are able to update their text
 * based on the current locale.
 * 
 * @author Drew Roos
 *
 */
public interface Localizable {
    /**
     * Updates the current object with the locate given.
     * @param locale
     * @param localizer
     */
    void localeChanged(String locale, Localizer localizer);
}
