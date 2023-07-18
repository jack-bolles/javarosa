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

package org.javarosa.core.model.utils;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Question Preloader is responsible for maintaining a set of handlers which are capable
 * of parsing 'preload' elements, and their parameters, and returning IAnswerData objects.
 *
 * @author Clayton Sims
 */
public class QuestionPreloader {
    private static final Logger logger = LoggerFactory.getLogger(QuestionPreloader.class);

    // NOTE: this is not java.util.Map!!!
    private final Map<String, IPreloadHandler> preloadHandlers = new Map<>();

    public QuestionPreloader() {
        initPreloadHandlers();
    }

    private void initPreloadHandlers() {
        addPreloadHandler(new IPreloadHandler.DatePreloadHandler());
        addPreloadHandler(new IPreloadHandler.PropertyPreloadHandler());
        addPreloadHandler(new IPreloadHandler.TimeStampPreloadHandler());
        addPreloadHandler(new IPreloadHandler.UIDPreloadHandler());
    }

    /**
     * Adds a new preload handler to this preloader.
     *
     * @param handler an IPreloadHandler that can handle a preload type
     */
    public void addPreloadHandler(IPreloadHandler handler) {
        preloadHandlers.put(handler.preloadHandled(), handler);
    }

    /**
     * Returns the IAnswerData preload value for the given preload type and parameters
     *
     * @param preloadType   The type of the preload to be returned
     * @param preloadParams Parameters for the preload handler
     * @return An IAnswerData corresponding to a pre-loaded value for the given
     * Arguments. null if no preload could successfully be derived due to either
     * the lack of a handler, or to invalid parameters
     */
    public IAnswerData getQuestionPreload(String preloadType, String preloadParams) {
        IPreloadHandler handler = preloadHandlers.get(preloadType);
        if (handler != null) {
            return handler.handlePreload(preloadParams);
        } else {
            logger.error("Do not know how to handle preloader [{}]", preloadType);
            return null;
        }
    }

    public boolean questionPostProcess(TreeElement node, String preloadType, String params) {
        IPreloadHandler handler = preloadHandlers.get(preloadType);
        if (handler != null) {
            return handler.handlePostProcess(node, params);
        } else {
            logger.error("Do not know how to handle preloader [{}]", preloadType);
            return false;
        }
    }
}
