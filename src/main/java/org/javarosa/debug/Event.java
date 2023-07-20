/*
 * Copyright (C) 2014 University of Washington
 *
 * Originally developed by Dobility, Inc. (as part of SurveyCTO)
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

package org.javarosa.debug;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Author: Meletis Margaritis
 * Date: 12/2/14
 * Time: 7:35 PM
 */
public class Event {

  private final String message;
  private final List<EvaluationResult> evaluationResults;

  public Event(String message) {
    this(message, Collections.<EvaluationResult>emptyList());
  }

  public Event(String message, EvaluationResult ref) {
    this(message, Collections.singletonList(ref));
  }

  public Event(String message, List<EvaluationResult> evaluationResults) {
    this.message = message;
    this.evaluationResults = evaluationResults;
  }

  public String getMessage() {
    return message;
  }

  protected List<EvaluationResult> getEvaluationResults() {
    return evaluationResults;
  }

  public String getDisplayMessage() {
    if (getEvaluationResults() == null) {
      return getMessage();
    } else {
      return "Processing '" + getMessage() + "' for " + getShortFieldNames(this.getEvaluationResults());
    }
  }

  private static String join(String separator, String... args) {
    StringBuilder sb = new StringBuilder();
    for (String arg : args) {
      if (!sb.isEmpty()) {
        sb.append(separator);
      }
      sb.append(arg);
    }
    return sb.toString();
  }

  private static String getShortFieldNames(Collection<EvaluationResult> evaluationResults) {
    if (null == evaluationResults) {
      return "";
    }

    StringBuilder sb = new StringBuilder();
    for (EvaluationResult evaluationResult : evaluationResults) {
      if (!sb.isEmpty()) {
        sb.append(", ");
      }
      sb.append(getShortFieldName(evaluationResult));
    }
    return sb.toString();
  }

  private static String getShortFieldName(EvaluationResult evaluationResult) {
     if (null == evaluationResult) {
        return "";
     } else {
       return evaluationResult.toString();
     }
  }
}
