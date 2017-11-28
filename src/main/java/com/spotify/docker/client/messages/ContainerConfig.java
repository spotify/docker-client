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
import javax.annotation.Nullable;

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

  /**
   * @deprecated As of 8.10.0, use {@link #volumes()}.
   */
  @Deprecated
  public Set<String> volumeNames() {
    return volumes();
  }

  @Nullable
  @JsonProperty("Volumes")
  public abstract ImmutableSet<String> volumes();

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

  @Nullable
  @JsonProperty("Healthcheck")
  public abstract Healthcheck healthcheck();

  /**
   * @deprecated  As of release 7.0.0, replaced by {@link #stopSignal()}.
   */
  @Deprecated
  public String getStopSignal() {
    return stopSignal();
  }

  @Nullable
  @JsonProperty("NetworkingConfig")
  public abstract NetworkingConfig networkingConfig();

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
      @JsonProperty("Volumes") final Set<String> volumes,
      @JsonProperty("WorkingDir") final String workingDir,
      @JsonProperty("Entrypoint") final List<String> entrypoint,
      @JsonProperty("NetworkDisabled") final Boolean networkDisabled,
      @JsonProperty("OnBuild") final List<String> onBuild,
      @JsonProperty("Labels") final Map<String, String> labels,
      @JsonProperty("MacAddress") final String macAddress,
      @JsonProperty("HostConfig") final HostConfig hostConfig,
      @JsonProperty("StopSignal") final String stopSignal,
      @JsonProperty("Healthcheck") final Healthcheck healthcheck,
      @JsonProperty("NetworkingConfig") final NetworkingConfig networkingConfig) {
    return builder()
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
        .stopSignal(stopSignal)
        .networkingConfig(networkingConfig)
        .volumes(volumes)
        .portSpecs(portSpecs)
        .exposedPorts(exposedPorts)
        .env(env)
        .cmd(cmd)
        .entrypoint(entrypoint)
        .onBuild(onBuild)
        .labels(labels)
        .healthcheck(healthcheck)
        .build();
  }

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

    abstract ImmutableSet.Builder<String> volumesBuilder();

    public Builder addVolume(final String volume) {
      volumesBuilder().add(volume);
      return this;
    }

    public Builder addVolumes(final String... volumes) {
      for (final String volume : volumes) {
        volumesBuilder().add(volume);
      }
      return this;
    }

    /**
     * @deprecated As of 8.10.0, use {@link #volumes(Set)} or
     *             {@link #volumes(String...)}.
     */
    @Deprecated
    public Builder volumes(final Map<String, Map> volumes) {
      this.volumes(volumes.keySet());
      return this;
    }

    public abstract Builder volumes(final Set<String> volumes);

    public abstract Builder volumes(final String... volumes);

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

    public abstract Builder healthcheck(final Healthcheck healthcheck);

    public abstract Builder networkingConfig(final NetworkingConfig networkingConfig);

    public abstract ContainerConfig build();
  }

  @AutoValue
  public abstract static class Healthcheck {
    @Nullable
    @JsonProperty("Test")
    public abstract ImmutableList<String> test();

    /**
     * In nanoseconds.
     */
    @Nullable
    @JsonProperty("Interval")
    public abstract Long interval();

    /**
     * In nanoseconds.
     */
    @Nullable
    @JsonProperty("Timeout")
    public abstract Long timeout();

    @Nullable
    @JsonProperty("Retries")
    public abstract Integer retries();

    /**
     * In nanoseconds.
     * @since API 1.29
     */
    @Nullable
    @JsonProperty("StartPeriod")
    public abstract Long startPeriod();

    public static Healthcheck create(
            @JsonProperty("Test") final List<String> test,
            @JsonProperty("Interval") final Long interval,
            @JsonProperty("Timeout") final Long timeout,
            @JsonProperty("Retries") final Integer retries) {
      return create(test, interval, timeout, retries, null);
    }

    @JsonCreator
    public static Healthcheck create(
            @JsonProperty("Test") final List<String> test,
            @JsonProperty("Interval") final Long interval,
            @JsonProperty("Timeout") final Long timeout,
            @JsonProperty("Retries") final Integer retries,
            @JsonProperty("StartPeriod") final Long startPeriod) {
      return builder()
          .test(test)
          .interval(interval)
          .timeout(timeout)
          .retries(retries)
          .startPeriod(startPeriod)
          .build();
    }

    public static Builder builder() {
      return new AutoValue_ContainerConfig_Healthcheck.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
      public abstract Builder test(final List<String> test);

      public abstract Builder interval(final Long interval);

      public abstract Builder timeout(final Long timeout);

      public abstract Builder retries(final Integer retries);

      public abstract Builder startPeriod(final Long startPeriod);

      public abstract Healthcheck build();
    }
  }

  @AutoValue
  public abstract static class NetworkingConfig {
    @JsonProperty("EndpointsConfig")
    public abstract ImmutableMap<String, EndpointConfig> endpointsConfig();

    @JsonCreator
    public static NetworkingConfig create(
            @JsonProperty("EndpointsConfig") final Map<String, EndpointConfig> endpointsConfig) {
      final ImmutableMap<String, EndpointConfig> endpointsConfigCopy =
              endpointsConfig == null
                      ? ImmutableMap.<String, EndpointConfig>of()
                      : ImmutableMap.copyOf(endpointsConfig);
      return new AutoValue_ContainerConfig_NetworkingConfig(endpointsConfigCopy);
    }
  }
}
