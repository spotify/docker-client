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

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;

public class ProgressResponseReader implements MessageBodyReader<ProgressStream> {

  @Override
  public boolean isReadable(final Class<?> type, final Type genericType,
                            final Annotation[] annotations,
                            final MediaType mediaType) {
    return type == ProgressStream.class;
  }

  @Override
  public ProgressStream readFrom(final Class<ProgressStream> type, final Type genericType,
                                 final Annotation[] annotations,
                                 final MediaType mediaType,
                                 final MultivaluedMap<String, String> httpHeaders,
                                 final InputStream entityStream)
      throws IOException, WebApplicationException {
    return new ProgressStream(entityStream);
  }
}
