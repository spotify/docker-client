package com.spotify.docker.client.messages;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.*;

@JsonAutoDetect(fieldVisibility = ANY, setterVisibility = NONE, getterVisibility = NONE)
public class Event {

  @JsonProperty("status") private String status;
  @JsonProperty("id") private String id;
  @JsonProperty("from") private String from;
  @JsonProperty("time") private Date time;

  public String status() {
    return status;
  }

  public String id() {
    return id;
  }

  public String from() {
    return from;
  }

  public Date time() {
    return time;
  }

}
