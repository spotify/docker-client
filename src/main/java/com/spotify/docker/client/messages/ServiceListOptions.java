/*
 * Copyright (c) 2015 Spotify AB.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.spotify.docker.client.messages;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class ServiceListOptions {

    @JsonProperty("Filter")
    private Map<String, Map<String, Boolean>> filter = new HashMap<String, Map<String, Boolean>>();

    public Map<String, Map<String, Boolean>> filter() {
        return filter;
    }

    /**
     * Add a new item to the filter.
     * 
     * @param name
     * @param value
     * @return
     */
    public Map<String, Map<String, Boolean>> addFilter(String name, String value) {
        Map<String, Boolean> filtersForName = filter.get(name);
        if (filtersForName == null) {
            filtersForName = new HashMap<String, Boolean>();
            filter.put(name, filtersForName);
        }
        filtersForName.put(value, true);
        return filter;
    }
}
