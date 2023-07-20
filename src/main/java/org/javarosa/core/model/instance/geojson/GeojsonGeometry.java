/*
 * Copyright 2022 ODK
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.javarosa.core.model.instance.geojson;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class GeojsonGeometry {

    private String type;
    private ArrayList<Object> coordinates;

    public String getOdkCoordinates() throws IOException {
        StringJoiner stringJoiner = new StringJoiner("; ");
        switch (type) {
            case "Point":
                return coordinates.get(1) + " " + coordinates.get(0) + " 0 0";
            case "LineString":
                for (Object item : coordinates) {
                    List<Object> point = (List<Object>) item;
                    stringJoiner.add(point.get(1) + " " + point.get(0) + " 0 0");
                }
                return stringJoiner.toString();
            case "Polygon":
                if (!coordinates.isEmpty()) {
                    for (Object item : (List<Object>) coordinates.get(0)) {
                        List<Object> point = (List<Object>) item;
                        stringJoiner.add(point.get(1) + " " + point.get(0) + " 0 0");
                    }
                    return stringJoiner.toString();
                } else {
                    return "";
                }

            default:
                throw new IOException("Only Points, LineStrings and Polygons are currently supported");
        }
    }
}