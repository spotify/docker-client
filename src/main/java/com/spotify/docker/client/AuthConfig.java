package com.spotify.docker.client;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Objects;
import com.sun.jersey.core.util.Base64;

/**
 * Configuration object holding auth information for
 * pushing to Docker
 *
 */
public class AuthConfig {

    private String username;
    private String password;
    private String email;
    private String auth;

    public AuthConfig() {
    }

    public AuthConfig(String user, String password, String email,String auth) {
		this.username = user;
		this.password = password;
		this.email = email;
		this.auth = auth;
    }

    @JsonIgnore
    public String toHeaderValue() {
    	ObjectNode ret = new ObjectNode(JsonNodeFactory.instance);
        
    	ret.put("username", username);
    	ret.put("password", password);
    	ret.put("email", email);
    	ret.put("auth", auth);
        
        return new String(Base64.encode(ret.toString().getBytes()));
    }
    
    @Override
    public int hashCode() {
      int result = username != null ? username.hashCode() : 0;
      result = 31 * result + (password != null ? password.hashCode() : 0);
      result = 31 * result + (email != null ? email.hashCode() : 0);
      result = 31 * result + (auth != null ? auth.hashCode() : 0);
      return result;
    }
    @Override
    public String toString() {
      return Objects.toStringHelper(this)
              .add("username", username)
              .add("password", password)
              .add("email", email)
              .add("auth", auth)
              .toString();
    }
}