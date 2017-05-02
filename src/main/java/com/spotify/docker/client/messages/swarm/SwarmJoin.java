package com.spotify.docker.client.messages.swarm;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.util.List;


@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class SwarmJoin {
  @JsonProperty("ListenAddr")
  public abstract String listenAddr();

  @JsonProperty("AdvertiseAddr")
  public abstract String advertiseAddr();

  @JsonProperty("RemoteAddrs")
  public abstract List<String> remoteAddrs();

  @JsonProperty("JoinToken")
  public abstract String joinToken();

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder listenAddr(String listenAddr);

    public abstract Builder advertiseAddr(String advertiseAddr);

    public abstract Builder remoteAddrs(List<String> remoteAddrs);

    public abstract Builder joinToken(String swarmSpec);

    public abstract SwarmJoin build();
  }

  public static SwarmJoin.Builder builder() {
    return new AutoValue_SwarmJoin.Builder();
  }

  @JsonCreator
  static SwarmJoin create(
      @JsonProperty("ListenAddr") final String listenAddr,
      @JsonProperty("AdvertiseAddr") final String advertiseAddr,
      @JsonProperty("RemoteAddrs") final List<String> remoteAddrs,
      @JsonProperty("JoinToken") final String joinToken) {
    final Builder builder = builder()
        .listenAddr(listenAddr)
        .advertiseAddr(advertiseAddr)
        .remoteAddrs(remoteAddrs)
        .joinToken(joinToken);

    return builder.build();

  }
}
