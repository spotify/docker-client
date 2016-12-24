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

package com.spotify.docker.client.messages.swarm;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.spotify.docker.client.messages.mount.Mount;

import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class ContainerSpec {

  @NotNull
  @JsonProperty("Image")
  public abstract String image();

  @Nullable
  @JsonProperty("Labels")
  public abstract ImmutableMap<String, String> labels();

  @NotNull
  @JsonProperty("Command")
  public abstract ImmutableList<String> command();

  @Nullable
  @JsonProperty("Args")
  public abstract ImmutableList<String> args();

  @Nullable
  @JsonProperty("Env")
  public abstract ImmutableList<String> env();

  @Nullable
  @JsonProperty("Dir")
  public abstract String dir();

  @Nullable
  @JsonProperty("User")
  public abstract String user();

  @Nullable
  @JsonProperty("Groups")
  public abstract ImmutableList<String> groups();

  @Nullable
  @JsonProperty("TTY")
  public abstract Boolean tty();

  @Nullable
  @JsonProperty("Mounts")
  public abstract ImmutableList<Mount> mounts();

  @Nullable
  @JsonProperty("StopGracePeriod")
  public abstract Long stopGracePeriod();

  @AutoValue.Builder
  public abstract static class Builder {

    @JsonProperty("Image")
    public abstract Builder image(String image);

    abstract ImmutableMap.Builder<String, String> labelsBuilder();

    public Builder addLabel(final String label, final String value) {
      labelsBuilder().put(label, value);
      return this;
    }

    @JsonProperty("Labels")
    public abstract Builder labels(Map<String, String> labels);

    @JsonProperty("Command")
    public abstract Builder command(String... commands);

    @JsonProperty("Command")
    public abstract Builder command(List<String> commands);

    @JsonProperty("Args")
    public abstract Builder args(String... args);

    @JsonProperty("Args")
    public abstract Builder args(List<String> args);

    @JsonProperty("Env")
    public abstract Builder env(String... env);

    @JsonProperty("Env")
    public abstract Builder env(List<String> env);

    @JsonProperty("Dir")
    public abstract Builder dir(String dir);

    @JsonProperty("User")
    public abstract Builder user(String user);

    @JsonProperty("Groups")
    public abstract Builder groups(String... groups);

    @JsonProperty("Groups")
    public abstract Builder groups(List<String> groups);

    @JsonProperty("TTY")
    public abstract Builder tty(Boolean tty);

    @JsonProperty("Mounts")
    public abstract Builder mounts(Mount... mounts);

    @JsonProperty("Mounts")
    public abstract Builder mounts(List<Mount> mounts);

    @JsonProperty("StopGracePeriod")
    public abstract Builder stopGracePeriod(Long stopGracePeriod);

    public abstract ContainerSpec build();
  }

  @NotNull
  public static ContainerSpec.Builder builder() {
    return new AutoValue_ContainerSpec.Builder();
  }

  @JsonCreator
  static ContainerSpec create(
      @JsonProperty("Image") final String image,
      @JsonProperty("Labels") final Map<String, String> labels,
      @JsonProperty("Command") final List<String> command,
      @JsonProperty("Args") final List<String> args,
      @JsonProperty("Env") final List<String> env,
      @JsonProperty("Dir") final String dir,
      @JsonProperty("User") final String user,
      @JsonProperty("Groups") final List<String> groups,
      @JsonProperty("TTY") final Boolean tty,
      @JsonProperty("Mounts") final List<Mount> mounts,
      @JsonProperty("StopGracePeriod") final Long stopGracePeriod) {
    final Builder builder = builder()
        .image(image)
        .command(command)
        .args(args)
        .env(env)
        .dir(dir)
        .user(user)
        .groups(groups)
        .tty(tty)
        .mounts(mounts)
        .stopGracePeriod(stopGracePeriod);

    if (labels != null) {
      builder.labels(labels);
    }

    return builder.build();
  }
}
