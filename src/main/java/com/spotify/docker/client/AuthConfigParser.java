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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.spotify.docker.client.messages.AuthConfig;
import org.glassfish.jersey.internal.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This helper class is used to build a {@Link AuthConf.Builder} from the docker config json
 * created by running `docker login`
 */
public class AuthConfigParser {
    private Map<String, AuthConfig.Builder> authBuilders;

    private static final Logger log = LoggerFactory.getLogger(AuthConfigParser.class);

    public AuthConfigParser(String configFile) {
        File dockerConfig = new File(configFile);
        parseAuthBuilders(dockerConfig);
    }

    public AuthConfigParser() {
        String home = System.getProperty("user.home");
        File dockerConfig = new File(new File(home, ".docker"), "config.json");
        File dockerCfg = new File(home, ".dockercfg");

        if (dockerConfig.isFile()) {
            log.debug("Using configfile: " + dockerConfig);
            parseAuthBuilders(dockerConfig);
        } else if (dockerCfg.isFile()) {
            log.debug("Using configfile: " + dockerCfg);
            parseAuthBuilders(dockerCfg);
        } else {
            log.error("Could not find a docker config. Please run 'docker login' to create one");
        }
    }

    /**
     * @return a {@Link AuthConf.Builder} based on docker config json
     */
    public AuthConfig.Builder getBuilder() {
        if (authBuilders.isEmpty()) {
            log.error("Could not find any valid auth configurations, returning default builder");
            return AuthConfig.builder();
        } else {
            String serverAddress = new ArrayList<>(authBuilders.keySet()).get(0);
            return authBuilders.get(serverAddress);
        }

    }

    /**
     * @param serverAddress
     * @return a {@Link AuthConf.Builder} based on docker config json for the
     * given serverAdress
     */
    public AuthConfig.Builder getBuilderFor(String serverAddress) {
        if (authBuilders.containsKey(serverAddress)) {
            return authBuilders.get(serverAddress);
        } else {
            log.error("Could not find auth config for ${serverAddress}, returning empty builder");
            return AuthConfig.builder().serverAddress(serverAddress);
        }
    }

    private void parseAuthBuilders(File configFile) {
        authBuilders = new HashMap<>();
        JsonNode authJson = extractAuthJson(configFile);

        Iterator<String> servers = authJson.fieldNames();
        while (servers.hasNext()) {
            String server = servers.next();
            AuthConfig.Builder authBuilder = AuthConfig.builder();
            authBuilder.serverAddress(server);

            JsonNode serverAuth = authJson.get(server);

            if (serverAuth.has("auth")) {
                String authString = serverAuth.get("auth").asText();
                String[] authParams = Base64.decodeAsString(authString).split(":");

                if (authParams.length == 2) {
                    authBuilder.username(authParams[0].trim());
                    authBuilder.password(authParams[1].trim());
                } else {
                    log.error("Failed to parse auth string for ${server} in ${configFile}");
                    continue;
                }
            } else {
                log.error("Could not find auth value for ${server} in ${configFile}");
                continue;
            }

            if (serverAuth.has("email")) {
                authBuilder.email(serverAuth.get("email").asText());
            }

            authBuilders.put(server, authBuilder);
        }

    }

    private JsonNode extractAuthJson(File configFile)  {
        ObjectMapper mapper = new ObjectMapper();

        JsonNode config = new TextNode("");
        try {
            config = mapper.readTree(configFile);
        } catch (IOException e) {
            log.error("Could not read configfile: ${configFile}");
            log.error(e.getMessage());
        }
        if (config.has("auths")) {
            return config.get("auths");
        }
        return config;
    }
}

