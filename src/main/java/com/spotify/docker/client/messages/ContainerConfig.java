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

package com.spotify.docker.client.messages;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class ContainerConfig {

  @Nullable
  @JsonProperty("Hostname")
  public abstract String hostname();

  @Nullable
  @JsonProperty("Domainname")
  public abstract String domainname();

  @Nullable
  @JsonProperty("User")
  public abstract String user();

  @Nullable
  @JsonProperty("AttachStdin")
  public abstract Boolean attachStdin();

  @Nullable
  @JsonProperty("AttachStdout")
  public abstract Boolean attachStdout();

  @Nullable
  @JsonProperty("AttachStderr")
  public abstract Boolean attachStderr();

  @Nullable
  @JsonProperty("PortSpecs")
  public abstract ImmutableList<String> portSpecs();

  @Nullable
  @JsonProperty("ExposedPorts")
  public abstract ImmutableSet<String> exposedPorts();

  @Nullable
  @JsonProperty("Tty")
  public abstract Boolean tty();

  @Nullable
  @JsonProperty("OpenStdin")
  public abstract Boolean openStdin();

  @Nullable
  @JsonProperty("StdinOnce")
  public abstract Boolean stdinOnce();

  @Nullable
  @JsonProperty("Env")
  public abstract ImmutableList<String> env();

  @Nullable
  @JsonProperty("Cmd")
  public abstract ImmutableList<String> cmd();

  @Nullable
  @JsonProperty("Image")
  public abstract String image();

  @NotNull
  @SuppressFBWarnings("NP_NULL_ON_SOME_PATH")
  public Set<String> volumeNames() {
    //noinspection ConstantConditions
    return volumes() == null ? Collections.<String>emptySet() : volumes().keySet();
  }

  @Nullable
  @JsonProperty("Volumes")
  public abstract ImmutableMap<String, Map> volumes();

  @Nullable
  @JsonProperty("WorkingDir")
  public abstract String workingDir();

  @Nullable
  @JsonProperty("Entrypoint")
  public abstract ImmutableList<String> entrypoint();

  @Nullable
  @JsonProperty("NetworkDisabled")
  public abstract Boolean networkDisabled();

  @Nullable
  @JsonProperty("OnBuild")
  public abstract ImmutableList<String> onBuild();

  @Nullable
  @JsonProperty("Labels")
  public abstract ImmutableMap<String, String> labels();

  @Nullable
  @JsonProperty("MacAddress")
  public abstract String macAddress();

  @Nullable
  @JsonProperty("HostConfig")
  public abstract HostConfig hostConfig();

  @Nullable
  @JsonProperty("StopSignal")
  public abstract String stopSignal();

  @JsonCreator
  static ContainerConfig create(
      @JsonProperty("Hostname") final String hostname,
      @JsonProperty("Domainname") final String domainname,
      @JsonProperty("User") final String user,
      @JsonProperty("AttachStdin") final Boolean attachStdin,
      @JsonProperty("AttachStdout") final Boolean attachStdout,
      @JsonProperty("AttachStderr") final Boolean attachStderr,
      @JsonProperty("PortSpecs") final List<String> portSpecs,
      @JsonProperty("ExposedPorts") final Set<String> exposedPorts,
      @JsonProperty("Tty") final Boolean tty,
      @JsonProperty("OpenStdin") final Boolean openStdin,
      @JsonProperty("StdinOnce") final Boolean stdinOnce,
      @JsonProperty("Env") final List<String> env,
      @JsonProperty("Cmd") final List<String> cmd,
      @JsonProperty("Image") final String image,
      @JsonProperty("Volumes") final Map<String, Map> volumes,
      @JsonProperty("WorkingDir") final String workingDir,
      @JsonProperty("Entrypoint") final List<String> entrypoint,
      @JsonProperty("NetworkDisabled") final Boolean networkDisabled,
      @JsonProperty("OnBuild") final List<String> onBuild,
      @JsonProperty("Labels") final Map<String, String> labels,
      @JsonProperty("MacAddress") final String macAddress,
      @JsonProperty("HostConfig") final HostConfig hostConfig,
      @JsonProperty("StopSignal") final String stopSignal ) {
    final Builder builder = builder()
        .hostname(hostname)
        .domainname(domainname)
        .user(user)
        .attachStdin(attachStdin)
        .attachStdout(attachStdout)
        .attachStderr(attachStderr)
        .tty(tty)
        .openStdin(openStdin)
        .stdinOnce(stdinOnce)
        .image(image)
        .workingDir(workingDir)
        .networkDisabled(networkDisabled)
        .macAddress(macAddress)
        .hostConfig(hostConfig)
        .stopSignal(stopSignal);

    if (portSpecs != null) {
      builder.portSpecs(portSpecs);
    }
    if (exposedPorts != null) {
      builder.exposedPorts(exposedPorts);
    }
    if (env != null) {
      builder.env(env);
    }
    if (cmd != null) {
      builder.cmd(cmd);
    }
    if (volumes != null)  {
      builder.volumes(volumes);
    }
    if (entrypoint != null) {
      builder.entrypoint(entrypoint);
    }
    if (onBuild != null) {
      builder.onBuild(onBuild);
    }
    if (labels != null) {
      builder.labels(labels);
    }

    return builder.build();
  }

  public abstract Builder toBuilder();

  @NotNull
  public static Builder builder() {
    return new AutoValue_ContainerConfig.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    @JsonProperty("Hostname")
    public abstract Builder hostname(@NotNull final String hostname);

    @JsonProperty("Domainname")
    public abstract Builder domainname(@NotNull final String domainname);

    @JsonProperty("User")
    public abstract Builder user(@NotNull final String user);

    @JsonProperty("AttachStdin")
    public abstract Builder attachStdin(@NotNull final Boolean attachStdin);

    @JsonProperty("AttachStdout")
    public abstract Builder attachStdout(@NotNull final Boolean attachStdout);

    @JsonProperty("AttachStderr")
    public abstract Builder attachStderr(@NotNull final Boolean attachStderr);

    @JsonProperty("PortSpecs")
    public abstract Builder portSpecs(@NotNull final List<String> portSpecs);

    @JsonProperty("PortSpecs")
    public abstract Builder portSpecs(@NotNull final String... portSpecs);

    @JsonProperty("ExposedPorts")
    public abstract Builder exposedPorts(@NotNull final Set<String> exposedPorts);

    @JsonProperty("ExposedPorts")
    public abstract Builder exposedPorts(@NotNull final String... exposedPorts);

    @JsonProperty("TTY")
    public abstract Builder tty(@NotNull final Boolean tty);

    @JsonProperty("OpenStdin")
    public abstract Builder openStdin(@NotNull final Boolean openStdin);

    @JsonProperty("StdinOnce")
    public abstract Builder stdinOnce(@NotNull final Boolean stdinOnce);

    @JsonProperty("Env")
    public abstract Builder env(@NotNull final List<String> env);

    @JsonProperty("Env")
    public abstract Builder env(@NotNull final String... env);

    @JsonProperty("Cmd")
    public abstract Builder cmd(@NotNull final List<String> cmd);

    @JsonProperty("Cmd")
    public abstract Builder cmd(@NotNull final String... cmd);

    @JsonProperty("Image")
    public abstract Builder image(final String image);

    abstract ImmutableMap.Builder<String, Map> volumesBuilder();

    public Builder addVolume(final String volume) {
      volumesBuilder().put(volume, new HashMap());
      return this;
    }

    public Builder addVolumes(final String... volumes) {
      for (final String volume : volumes) {
        volumesBuilder().put(volume, new HashMap());
      }
      return this;
    }

    @JsonProperty("Volumes")
    public abstract Builder volumes(@NotNull final Map<String, Map> volumes);

    @JsonProperty("WorkingDir")
    public abstract Builder workingDir(@NotNull final String workingDir);

    @JsonProperty("Entrypoint")
    public abstract Builder entrypoint(@NotNull final List<String> entrypoint);

    @JsonProperty("Entrypoint")
    public abstract Builder entrypoint(@NotNull final String... entrypoint);

    @JsonProperty("NetworkDisabled")
    public abstract Builder networkDisabled(@NotNull final Boolean networkDisabled);

    @JsonProperty("OnBuild")
    public abstract Builder onBuild(@NotNull final List<String> onBuild);

    @JsonProperty("OnBuild")
    public abstract Builder onBuild(@NotNull final String... onBuild);

    @JsonProperty("Labels")
    public abstract Builder labels(@NotNull final Map<String, String> labels);

    @JsonProperty("MacAddress")
    public abstract Builder macAddress(@NotNull final String macAddress);

    @JsonProperty("HostConfig")
    public abstract Builder hostConfig(@NotNull final HostConfig hostConfig);

    @JsonProperty("StopSignal")
    public abstract Builder stopSignal(@NotNull final String stopSignal);

    public abstract ContainerConfig build();
  }
}
