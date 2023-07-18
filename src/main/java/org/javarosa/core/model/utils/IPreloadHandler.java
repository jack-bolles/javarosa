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

import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.DateTimeData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.services.PropertyManager;
import org.javarosa.core.util.Map;
import org.javarosa.core.util.PropertyUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.javarosa.core.model.utils.DateUtils.localDateFrom;
import static org.javarosa.core.model.utils.StringUtils.split;

/**
 * An IPreloadHandler is capable of taking in a set of parameters
 * for a question's preloaded value, and returning an IAnswerData
 * object that should be preloaded for a question. 
 * 
 * @author Clayton Sims
 *
 */
public interface IPreloadHandler {

    /**
     * @return A String representing the preload handled by this handler
     */
    String preloadHandled();

    /**
     * Takes in a set of preload parameters, and determines the
     * proper IAnswerData to be preloaded for a question.
     *
     * @param preloadParams the parameters determining the preload value
     * @return An IAnswerData to be used as the default, preloaded value
     * for a Question.
     */
    IAnswerData handlePreload(String preloadParams);

    /**
     * Handles any post processing tasks that should be completed after the form entry
     * interaction is completed.
     *
     * @param node The node to be processed
     * @param params Processing parameters.
     * @return true if any post-processing occurs, false otherwise.
     */
    boolean handlePostProcess(TreeElement node, String params);

    static Map<String, IPreloadHandler> builtIns(){
        Map<String, IPreloadHandler> map = new Map<>();
        addPreloadHandler(map, new IPreloadHandler.DatePreloadHandler());
        addPreloadHandler(map, new IPreloadHandler.PropertyPreloadHandler());
        addPreloadHandler(map, new IPreloadHandler.TimeStampPreloadHandler());
        addPreloadHandler(map, new IPreloadHandler.UIDPreloadHandler());
        return map;
    }

    static void addPreloadHandler(Map<String, IPreloadHandler> map, IPreloadHandler handler) {
        map.put(handler.preloadHandled(), handler);
    }


    class DatePreloadHandler implements IPreloadHandler {
        public static final long DAY_IN_MS = 86400000L;

        /**
             * Creates a Date object representing the amount of time between the
             * reference date, and the given parameters.
             *
             * @param ref          The starting reference date
             * @param type         "week", or "month", representing the time period which is to be returned.
             * @param start        "sun", "mon", ... etc. representing the start of the time period.
             * @param beginning    true=return first day of period, false=return last day of period
             * @param includeToday Whether today's date can count as the last day of the period
             * @param nAgo         How many periods ago. 1=most recent period, 0=period in progress
             * @return a Date object representing the amount of time between the
             * reference date, and the given parameters.
             */
        public static Date pastPeriodDateFrom(Date ref, SupportedPeriod type, String start, boolean beginning, boolean includeToday, int nAgo) {
            if (!type.equals(SupportedPeriod.week)) {
                throw new IllegalArgumentException();
            }

            Calendar cd = Calendar.getInstance();
            cd.setTime(ref);
            int current_dow = cd.get(Calendar.DAY_OF_WEEK) - 1;
            int target_dow = DOW.valueOf(start).order;
            int offset = (includeToday ? 1 : 0);
            int diff = ((current_dow - target_dow + 7 + offset) % 7 - offset) + (7 * nAgo) - (beginning ? 0 : 6);
            return new Date(ref.getTime() - diff * DAY_IN_MS);
        }

        public String preloadHandled() {
            return "date";
        }

        /**
         * @param preloadParams the parameters determining the preload value
         * When:
         * "today" => creates sets it as today
         * "prevperiod-" => as the front of the string, it tokenises the rest of the string
         *              (using '-' as the delimiter) for up to 5 values:
         * "period" - Currently supports only "week"
         * "start" - The day of the week to use as the start of the period. IOW, sets the period starting point,
         *          eg "sun" => Sunday to Saturday; "mon" => Monday to Sunday
         *          Accepts 'sun', 'mon', 'tue', 'wed', 'thu', 'fri', 'sat'
         * "beginning or end" - use the first ('head') or last ('tail') day of the period
         * "includeToday" - 'x' for yes, '' for no. Anything else throws a IllegalArgumentException
         * "nAgo" - periods to go <em>back</em>. Must be an integer. Negative numbers move forward.
         *
         * This method only handles the unpacking of the parameters.
         * @see org.javarosa.core.model.utils.DateUtils.getPastPeriodDate() for the algorithm
         *
         * Examples:
         * "prevperiod-week-mon-head--11"
         * Gives you a date that is the Monday 11 weeks ago, 12 weeks if today is a Monday
         *
         * "prevperiod-week-fri-tail-x-11"
         * Gives you a date that is the Thursday 10 weeks ago
         *
         * @return An DateData to be used as the default, preloaded value for a Question.
         * Or null if the type of date can't be determined.
         * Or throws an IllegalArgumentException if the tokenised params can't be interpreted by the  parser
         */
        @SuppressWarnings("JavadocReference")
        public IAnswerData handlePreload(String preloadParams) {
            //TODO - use LocalDate
            Date d;
            if (preloadParams.equals("today")) {
                d = new Date();
            } else if (preloadParams.startsWith("prevperiod-")) {
                List<String> v = split(preloadParams.substring(11), "-", false);
                String[] params = new String[v.size()];
                for (int i = 0; i < params.length; i++)
                    params[i] = v.get(i);

                try {
                    SupportedPeriod supportedPeriod = SupportedPeriod.valueOf(params[0]);
                    String start = params[1];

                    boolean beginning;
                    if (params[2].equals("head")) beginning = true;
                    else if (params[2].equals("tail")) beginning = false;
                    else throw new RuntimeException();

                    boolean includeToday;
                    if (params.length >= 4) {
                        if (params[3].equals("x")) includeToday = true;
                        else if (params[3].equals("")) includeToday = false;
                        else throw new RuntimeException();
                    } else {
                        includeToday = false;
                    }

                    int nAgo;
                    if (params.length >= 5) {
                        nAgo = Integer.parseInt(params[4]);
                    } else {
                        nAgo = 1;
                    }

                    d = pastPeriodDateFrom(new Date(), supportedPeriod, start, beginning, includeToday, nAgo);
                } catch (Exception e) {
                    throw new IllegalArgumentException("invalid preload params for preload mode 'date'", e);
                }
            } else return null;

            return new DateData(localDateFrom(d));
        }

        public boolean handlePostProcess(TreeElement node, String params) {
            return false;
        }

        //convenience, should go away soon
        private enum DOW {
            sun(0), mon(1), tue(2), wed(3), thu(4), fri(5), sat(6);
            final int order;

            DOW(int ordinal) {
                this.order = ordinal;
            }
        }
    }

    class PropertyPreloadHandler implements IPreloadHandler {
        public String preloadHandled() {
            return "property";
        }

        public IAnswerData handlePreload(String preloadParams) {
            String propval = PropertyManager.__().getSingularProperty(preloadParams);
            StringData data = null;
            if (propval != null && propval.length() > 0) {
                data = new StringData(propval);
            }
            return data;
        }

        public boolean handlePostProcess(TreeElement node, String params) {
            IAnswerData answer = node.getValue();
            String value = (answer == null ? null : answer.getDisplayText());
            if (params != null && params.length() > 0 && value != null && value.length() > 0)
                PropertyManager.__().setProperty(params, value);
            return false;
        }
    }

    class TimeStampPreloadHandler implements IPreloadHandler {
        public String preloadHandled() {
            return "timestamp";
        }

        /** if preloadParams == 'start', @return a DateTimeData, wrapping this moment in time; otherwise return null */
        public IAnswerData handlePreload(String preloadParams) {
            return ("start".equals(preloadParams) ? getDateTimeDataOfNow() : null);
        }

        /** if preloadParams == 'end', set the node's answer to this moment in time and @return true; otherwise @return false */
        public boolean handlePostProcess(TreeElement node, String params) {
            if ("end".equals(params)) {
                node.setAnswer(getDateTimeDataOfNow());
                return true;
            } else {
                return false;
            }
        }
        private DateTimeData getDateTimeDataOfNow() {
            return new DateTimeData(new Date());
        }
    }

    class UIDPreloadHandler implements IPreloadHandler {
        public String preloadHandled() {
            return "uid";
        }

        /** @return a StringData, wrapping a generated RFC 4122 UUID with a "uuid:" prefix */
        public IAnswerData handlePreload(String preloadParams) {
            return new StringData("uuid:" + PropertyUtils.genUUID());
        }

        public boolean handlePostProcess(TreeElement node, String params) {
            return false;
        }
    }
}

