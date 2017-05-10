package com.spotify.docker.client.messages.swarm;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;


@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class SwarmInit {
  @JsonProperty("ListenAddr")
  public abstract String listenAddr();

  @JsonProperty("AdvertiseAddr")
  public abstract String advertiseAddr();

  @JsonProperty("ForceNewCluster")
  public abstract boolean forceNewCluster();

  @Nullable
  @JsonProperty("Spec")
  public abstract SwarmSpec swarmSpec();

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder listenAddr(String listenAddr);

    public abstract Builder advertiseAddr(String advertiseAddr);

    public abstract Builder forceNewCluster(boolean forceNewCluster);

    public abstract Builder swarmSpec(SwarmSpec swarmSpec);

    public abstract SwarmInit build();
  }

  public static SwarmInit.Builder builder() {
    return new AutoValue_SwarmInit.Builder();
  }

  @JsonCreator
  static SwarmInit create(
      @JsonProperty("ListenAddr") final String listenAddr,
      @JsonProperty("AdvertiseAddr") final String advertiseAddr,
      @JsonProperty("ForceNewCluster") final boolean forceNewCluster,
      @JsonProperty("Spec") final SwarmSpec swarmSpec) {
    final Builder builder = builder()
        .listenAddr(listenAddr)
        .advertiseAddr(advertiseAddr)
        .forceNewCluster(forceNewCluster);

    if (swarmSpec != null) {
      builder.swarmSpec(swarmSpec);
    }

    return builder.build();

  }
}
