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

package com.spotify.docker.client.messages;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class ContainerConfig {

  @JsonProperty("Hostname")
  @Nullable
  public abstract String hostname();

  @JsonProperty("Domainname")
  @Nullable
  public abstract String domainname();

  @JsonProperty("User")
  @Nullable
  public abstract String user();

  @JsonProperty("AttachStdin")
  @Nullable
  public abstract Boolean attachStdin();

  @JsonProperty("AttachStdout")
  @Nullable
  public abstract Boolean attachStdout();

  @JsonProperty("AttachStderr")
  @Nullable
  public abstract Boolean attachStderr();

  @JsonProperty("PortSpecs")
  @Nullable
  public abstract ImmutableList<String> portSpecs();

  @JsonProperty("ExposedPorts")
  @Nullable
  public abstract ImmutableSet<String> exposedPorts();

  @JsonProperty("Tty")
  @Nullable
  public abstract Boolean tty();

  @JsonProperty("OpenStdin")
  @Nullable
  public abstract Boolean openStdin();

  @JsonProperty("StdinOnce")
  @Nullable
  public abstract Boolean stdinOnce();

  @JsonProperty("Env")
  @Nullable
  public abstract ImmutableList<String> env();

  @JsonProperty("Cmd")
  @Nullable
  public abstract ImmutableList<String> cmd();

  @JsonProperty("Image")
  @Nullable
  public abstract String image();

  @JsonProperty("Volumes")
  @Nullable
  public abstract Set<String> volumes();

  @JsonProperty("WorkingDir")
  @Nullable
  public abstract String workingDir();

  @JsonProperty("Entrypoint")
  @Nullable
  public abstract ImmutableList<String> entrypoint();

  @JsonProperty("NetworkDisabled")
  @Nullable
  public abstract Boolean networkDisabled();

  @JsonProperty("OnBuild")
  @Nullable
  public abstract ImmutableList<String> onBuild();

  @JsonProperty("Labels")
  @Nullable
  public abstract ImmutableMap<String, String> labels();

  @JsonProperty("MacAddress")
  @Nullable
  public abstract String macAddress();

  @JsonProperty("HostConfig")
  @Nullable
  public abstract HostConfig hostConfig();

  @JsonProperty("StopSignal")
  @Nullable
  public abstract String stopSignal();

  public abstract Builder toBuilder();

  public static Builder builder() {
    return new AutoValue_ContainerConfig.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder hostname(final String hostname);

    public abstract Builder domainname(final String domainname);

    public abstract Builder user(final String user);

    public abstract Builder attachStdin(final Boolean attachStdin);

    public abstract Builder attachStdout(final Boolean attachStdout);

    public abstract Builder attachStderr(final Boolean attachStderr);

    public abstract Builder portSpecs(final List<String> portSpecs);

    public abstract Builder portSpecs(final String... portSpecs);

    public abstract Builder exposedPorts(final Set<String> exposedPorts);

    public abstract Builder exposedPorts(final String... exposedPorts);

    public abstract Builder tty(final Boolean tty);

    public abstract Builder openStdin(final Boolean openStdin);

    public abstract Builder stdinOnce(final Boolean stdinOnce);

    public abstract Builder env(final List<String> env);

    public abstract Builder env(final String... env);

    public abstract Builder cmd(final List<String> cmd);

    public abstract Builder cmd(final String... cmd);

    public abstract Builder image(final String image);

    public abstract Builder volumes(Set<String> volumes);

    public Builder volumes(String... volumes) {
      return this.volumes(ImmutableSet.copyOf(volumes));
    }

    public abstract Builder workingDir(final String workingDir);

    public abstract Builder entrypoint(final List<String> entrypoint);

    public abstract Builder entrypoint(final String... entrypoint);

    public abstract Builder networkDisabled(final Boolean networkDisabled);

    public abstract Builder onBuild(final List<String> onBuild);

    public abstract Builder onBuild(final String... onBuild);

    public abstract Builder labels(final Map<String, String> labels);

    public abstract Builder macAddress(final String macAddress);

    public abstract Builder hostConfig(final HostConfig hostConfig);

    public abstract Builder stopSignal(final String stopSignal);

    public abstract ContainerConfig build();
  }
}
