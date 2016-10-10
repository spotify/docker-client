/*
 * Copyright (c) 2016
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

package com.spotify.docker.client.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.Date;

/**
 * A deserializer for Dates where the source data is in seconds since the epoch rather than
 * milliseconds as {@link Date} expects.
 */
public class UnixTimestampDeserializer extends JsonDeserializer<Date> {

  public UnixTimestampDeserializer() {
  }

  @Override
  public Date deserialize(final JsonParser p, final DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    final JsonToken token = p.getCurrentToken();
    if (token == JsonToken.VALUE_STRING) {
      final String str = p.getText().trim();
      return toDate(Long.parseLong(str));
    } else if (token == JsonToken.VALUE_NUMBER_INT) {
      return toDate(p.getLongValue());
    }
    throw ctxt.wrongTokenException(p, JsonToken.VALUE_STRING, "Expected a string or numeric value");
  }

  private static Date toDate(long secondsSinceEpoch) {
    return new Date(secondsSinceEpoch * 1000);
  }
}
