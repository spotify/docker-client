/*
 * Copyright (c) 2014 Spotify AB.
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


package com.spotify.docker.client;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.guava.GuavaModule;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
@Produces(MediaType.APPLICATION_JSON)
@SuppressWarnings({"rawtypes", "unchecked"})
public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {

  private static final Logger log = LoggerFactory.getLogger(ObjectMapperProvider.class);

  private static final Function<? super Object, ?> VOID_VALUE = new Function<Object, Object>() {
    @Override
    public Object apply(final Object input) {
      return null;
    }
  };

  private static final SimpleModule MODULE = new SimpleModule();
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  static {
    try {
      MODULE.addSerializer(Set.class, new SetSerializer());
      MODULE.addDeserializer(Set.class, new SetDeserializer());
      MODULE.addSerializer(ImmutableSet.class, new ImmutableSetSerializer());
      MODULE.addDeserializer(ImmutableSet.class, new ImmutableSetDeserializer());
      OBJECT_MAPPER.registerModule(new GuavaModule());
      OBJECT_MAPPER.registerModule(MODULE);
      OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
      OBJECT_MAPPER.setDateFormat(new DockerDateFormat());
    } catch (Throwable t) {
      log.error("Failure during static initialization", t);
      throw t;
    }
  }

  @Override
  public ObjectMapper getContext(Class<?> type) {
    return OBJECT_MAPPER;
  }

  static ObjectMapper objectMapper() {
    return OBJECT_MAPPER;
  }

  private static class SetSerializer extends JsonSerializer<Set> {

    @Override
    public void serialize(final Set value, final JsonGenerator jgen,
                          final SerializerProvider provider) throws IOException {
      final Map map = (value == null) ? null : Maps.asMap(value, VOID_VALUE);
      OBJECT_MAPPER.writeValue(jgen, map);
    }
  }

  private static class SetDeserializer extends JsonDeserializer<Set> {

    @Override
    public Set<?> deserialize(final JsonParser jp, final DeserializationContext ctxt)
        throws IOException {
      final Map map = OBJECT_MAPPER.readValue(jp, Map.class);
      return (map == null) ? null : map.keySet();
    }
  }

  private static class ImmutableSetSerializer extends JsonSerializer<ImmutableSet> {

    @Override
    public void serialize(final ImmutableSet value, final JsonGenerator jgen,
                          final SerializerProvider provider) throws IOException {
      final Map map = (value == null) ? null : Maps.asMap(value, VOID_VALUE);
      OBJECT_MAPPER.writeValue(jgen, map);
    }
  }

  private static class ImmutableSetDeserializer extends JsonDeserializer<ImmutableSet> {

    @Override
    public ImmutableSet<?> deserialize(final JsonParser jp, final DeserializationContext ctxt)
        throws IOException {
      final Map map = OBJECT_MAPPER.readValue(jp, Map.class);
      return (map == null) ? null : ImmutableSet.copyOf(map.keySet());
    }
  }
}
