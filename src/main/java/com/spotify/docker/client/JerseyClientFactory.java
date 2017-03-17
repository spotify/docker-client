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

import javax.ws.rs.client.Client;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.jackson.JacksonFeature;

public class JerseyClientFactory implements ClientFactory {
    private static final ClientConfig DEFAULT_CONFIG = new ClientConfig(
            ObjectMapperProvider.class,
            JacksonFeature.class,
            LogsResponseReader.class,
            ProgressResponseReader.class);
    
    @Override
    public Client getClient(HttpClientConnectionManager cm, RequestConfig requestConfig) {
        final ClientConfig config = DEFAULT_CONFIG
                .connectorProvider(new ApacheConnectorProvider())
                .property(ApacheClientProperties.CONNECTION_MANAGER, cm)
                .property(ApacheClientProperties.REQUEST_CONFIG, requestConfig);

        return new JerseyClientBuilder().withConfig(config).build();
    }

}
