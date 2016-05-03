/*
 * Copyright (c) 2014 Spotify AB.
 * Copyright (c) 2014 Oleg Poleshuk.
 * Copyright (c) 2014 CyDesign Ltd.
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
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.jboss.resteasy.client.jaxrs.i18n.Messages;
import org.jboss.resteasy.client.jaxrs.internal.ClientInvocation;
import org.jboss.resteasy.client.jaxrs.internal.ClientResponse;
import org.jboss.resteasy.util.HttpResponseCodes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

public class ResteasyClientFactory implements ClientFactory {
    @Override
    public Client getClient(HttpClientConnectionManager cm, RequestConfig requestConfig) {
        final CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(cm)
                .build();
        
        return new ResteasyClientBuilder()
            .httpEngine(new NonAutoClosingApacheHttpClient4Engine(httpClient))
            .register(ObjectMapperProvider.class)
            .register(ProgressResponseReader.class)
            .register(LogsResponseReader.class)
            .register(new CustomObjectMapperJacksonJaxbJsonProvider())
            .build();
    }
    
    @Provider
    @Consumes({"application/*+json", "text/json"})
    @Produces({"application/*+json", "text/json"})
    private static class CustomObjectMapperJacksonJaxbJsonProvider extends JacksonJaxbJsonProvider {
        @Override
        public ObjectMapper locateMapper(Class<?> type, MediaType mediaType) {
            return ObjectMapperProvider.objectMapper();
        }
    }
    
    // This entire thing is required because RESTEasy automatically closes the stream if the entity
    // isn't a stream type of response, which of course the list of these types is hard-coded
    // other than the specifically called out spot before this is entirely copied from the 
    // super-classes and enough tweaks to pass checkstyle/findbugs.
    private static class NonAutoClosingApacheHttpClient4Engine extends ApacheHttpClient4Engine {
        public NonAutoClosingApacheHttpClient4Engine(CloseableHttpClient httpClient) {
            super(httpClient);
        }

        @Override
        public ClientResponse invoke(ClientInvocation request) {
            final String uri = request.getUri().toString();
            final HttpRequestBase httpMethod = createHttpMethod(uri, request.getMethod());
            final HttpResponse res;
            try {
                loadHttpMethod(request, httpMethod);

                res = httpClient.execute(httpMethod, httpContext);
            } catch (Exception e) {
                throw new ProcessingException(Messages.MESSAGES.unableToInvokeRequest(), e);
            } finally {
                cleanUpAfterExecute(httpMethod);
            }

            ClientResponse response = new ClientResponse(request.getClientConfiguration()) {
                InputStream stream;
                InputStream hc4Stream;

                @Override
                protected void setInputStream(InputStream is) {
                    stream = is;
                }

                @Override
                public InputStream getInputStream() {
                    if (stream == null) {
                        final HttpEntity entity = res.getEntity();
                        if (entity == null) {
                            return null;
                        }
                        try {
                            hc4Stream = entity.getContent();
                            stream = createBufferedStream(hc4Stream);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return stream;
                }

                @Override
                public void releaseConnection() throws IOException {
                    try {
                        if (stream != null) {
                            stream.close();
                        } else {
                            final InputStream is = getInputStream();
                            if (is != null) {
                                is.close();
                            }
                        }
                    } finally {
                        if (hc4Stream != null) {
                            try {
                                hc4Stream.close();
                            } catch (IOException ignored) {

                            }
                        } else {
                            try {
                                final HttpEntity entity = res.getEntity();
                                if (entity != null) {
                                    entity.getContent().close();
                                }
                            } catch (IOException ignored) {
                            }

                        }

                    }
                }
                
                @Override
                @SuppressWarnings("unchecked")
                // this is copied entirely from the superclass other than the called out HACK
                public synchronized <T> T readEntity(Class<T> type, Type genericType, 
                        Annotation[] anns) {
                    
                    abortIfClosed();
                    if (entity != null) {
                        if (type.isInstance((this.entity))) {
                            return (T) entity;
                        } else if (entity instanceof InputStream) {
                            setInputStream((InputStream) entity);
                            entity = null;
                        } else if (bufferedEntity == null) {
                            throw new RuntimeException(
                                    Messages.MESSAGES.entityAlreadyRead(entity.getClass()));
                            
                        } else {
                            entity = null;
                        }
                    }

                    if (status == HttpResponseCodes.SC_NO_CONTENT) {
                        return null;
                    }

                    try {
                        entity = readFrom(type, genericType, getMediaType(), anns);
                        if (entity == null || 
                                (!InputStream.class.isInstance(entity) && 
                                 !Reader.class.isInstance(entity) &&
                                 // HACK: RESTEasy has a hardcoded list of classes it won't 
                                 // close a stream on, custom streaming types require this
                                 // hackery
                                 !ProgressStream.class.isInstance(entity) && 
                                 !LogStream.class.isInstance(entity) && 
                                 // END-HACK
                                 bufferedEntity == null)) {
                            try {
                                close();
                            } catch (Exception ignored) {
                            }
                        }
                    } catch (RuntimeException e) {
                        try {
                            close();
                        } catch (Exception ignored) {

                        }
                        throw e;
                    }
                    return (T) entity;
                }
            };
            
            response.setProperties(request.getMutableProperties());
            response.setStatus(res.getStatusLine().getStatusCode());
            response.setHeaders(extractHeaders(res));
            response.setClientConfiguration(request.getClientConfiguration());
            return response;
        }
    }
}
