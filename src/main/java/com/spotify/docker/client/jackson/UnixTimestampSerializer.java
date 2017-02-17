/*-
 * -\-\-
 * docker-client
 * --
 * Copyright (C) 2016 Spotify AB
 * --
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -/-/-
 */

package com.spotify.docker.client.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Date;

/**
 * A {@link Date} serializer that outputs seconds since epoch.
 * The inverse of {@link UnixTimestampDeserializer}.
 */
public class UnixTimestampSerializer extends JsonSerializer<Date> {

  private UnixTimestampSerializer() {
  }

  @Override
  public void serialize(final Date date,
                        final JsonGenerator gen,
                        final SerializerProvider serializers)
      throws IOException, JsonProcessingException {
    gen.writeNumber(date.getTime() / 1000);
  }
}
