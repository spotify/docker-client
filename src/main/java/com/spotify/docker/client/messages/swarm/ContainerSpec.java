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
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.mount.Mount;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class ContainerSpec {

  @JsonProperty("Image")
  public abstract String image();

  /**
   * @since API 1.26
   */
  @Nullable
  @JsonProperty("Hostname")
  public abstract String hostname();

  @Nullable
  @JsonProperty("Labels")
  public abstract ImmutableMap<String, String> labels();

  @Nullable
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

  /**
   * @since API 1.26
   */
  @Nullable
  @JsonProperty("Healthcheck")
  public abstract ContainerConfig.Healthcheck healthcheck();

  /**
   * @since API 1.26
   */
  @Nullable
  @JsonProperty("Hosts")
  public abstract ImmutableList<String> hosts();

  /**
   * @since API 1.26
   */
  @Nullable
  @JsonProperty("Secrets")
  public abstract ImmutableList<SecretBind> secrets();

  /**
   * @since API 1.30
   */
  @Nullable
  @JsonProperty("Configs")
  public abstract ImmutableList<ConfigBind> configs();

  @Nullable
  @JsonProperty("DNSConfig")
  public abstract DnsConfig dnsConfig();


  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder image(String image);

    /**
     * @deprecated  As of release 7.0.0, replaced by {@link #image(String)}.
     */
    @Deprecated
    public Builder withImage(final String image) {
      image(image);
      return this;
    }

    abstract ImmutableMap.Builder<String, String> labelsBuilder();

    public Builder addLabel(final String label, final String value) {
      labelsBuilder().put(label, value);
      return this;
    }

    /**
     * @deprecated  As of release 7.0.0, replaced by {@link #addLabel(String, String)} ()}.
     */
    @Deprecated
    public Builder withLabel(final String label, final String value) {
      addLabel(label, value);
      return this;
    }

    public abstract Builder hostname(String hostname);

    public abstract Builder labels(Map<String, String> labels);

    public abstract Builder command(String... commands);

    public abstract Builder command(List<String> commands);

    /**
     * @deprecated  As of release 7.0.0, replaced by {@link #command(String...)}.
     */
    @Deprecated
    public Builder withCommands(final String... commands) {
      if (commands != null && commands.length > 0) {
        command(commands);
      }
      return this;
    }

    /**
     * @deprecated  As of release 7.0.0, replaced by {@link #command(List)}.
     */
    @Deprecated
    public Builder withCommands(final List<String> commands) {
      if (commands != null && !commands.isEmpty()) {
        command(commands);
      }
      return this;
    }

    public abstract Builder args(String... args);

    public abstract Builder args(List<String> args);

    /**
     * @deprecated  As of release 7.0.0, replaced by {@link #args(String...)}.
     */
    @Deprecated
    public Builder withArgs(final String... args) {
      if (args != null && args.length > 0) {
        args(args);
      }
      return this;
    }

    /**
     * @deprecated  As of release 7.0.0, replaced by {@link #args(List)}.
     */
    @Deprecated
    public Builder withArgs(final List<String> args) {
      if (args != null && !args.isEmpty()) {
        args(args);
      }
      return this;
    }

    public abstract Builder env(String... env);

    public abstract Builder env(List<String> env);

    /**
     * @deprecated  As of release 7.0.0, replaced by {@link #env(String...)}.
     */
    @Deprecated
    public Builder withEnv(final String... env) {
      if (env != null && env.length > 0) {
        env(env);
      }
      return this;
    }

    /**
     * @deprecated  As of release 7.0.0, replaced by {@link #env(List)}.
     */
    @Deprecated
    public Builder withEnv(final List<String> env) {
      env(env);
      return this;
    }

    public abstract Builder dir(String dir);

    /**
     * @deprecated  As of release 7.0.0, replaced by {@link #dir(String)}.
     */
    @Deprecated
    public Builder withDir(final String dir) {
      dir(dir);
      return this;
    }

    public abstract Builder user(String user);

    /**
     * @deprecated  As of release 7.0.0, replaced by {@link #user(String)}.
     */
    @Deprecated
    public Builder withUser(final String user) {
      user(user);
      return this;
    }

    public abstract Builder groups(String... groups);

    public abstract Builder groups(List<String> groups);

    /**
     * @deprecated  As of release 7.0.0, replaced by {@link #groups(String...)}.
     */
    @Deprecated
    public Builder withGroups(final String... groups) {
      if (groups != null && groups.length > 0) {
        groups(groups);
      }
      return this;
    }

    /**
     * @deprecated  As of release 7.0.0, replaced by {@link #groups(List)}.
     */
    @Deprecated
    public Builder withGroups(final List<String> groups) {
      if (groups != null && !groups.isEmpty()) {
        groups(groups);
      }
      return this;
    }

    public abstract Builder tty(Boolean tty);

    /**
     * @deprecated  As of release 7.0.0, replaced by {@link #tty(Boolean)}
     */
    @Deprecated
    public Builder withTty() {
      tty(true);
      return this;
    }

    /**
     * @deprecated  As of release 7.0.0, replaced by {@link #tty(Boolean)}.
     */
    @Deprecated
    public Builder withTty(final boolean tty) {
      tty(tty);
      return this;
    }

    public abstract Builder mounts(Mount... mounts);

    public abstract Builder mounts(List<Mount> mounts);

    /**
     * @deprecated  As of release 7.0.0, replaced by {@link #mounts(Mount...)}.
     */
    @Deprecated
    public Builder withMounts(final Mount... mounts) {
      if (mounts != null && mounts.length > 0) {
        mounts(mounts);
      }
      return this;
    }

    /**
     * @deprecated  As of release 7.0.0, replaced by {@link #mounts(List)}.
     */
    @Deprecated
    public Builder withMounts(final List<Mount> mounts) {
      if (mounts != null && !mounts.isEmpty()) {
        mounts(mounts);
      }
      return this;
    }

    public abstract Builder stopGracePeriod(Long stopGracePeriod);

    /**
     * @deprecated  As of release 7.0.0, replaced by {@link #stopGracePeriod(Long)}.
     */
    @Deprecated
    public Builder withStopGracePeriod(final long stopGracePeriod) {
      stopGracePeriod(stopGracePeriod);
      return this;
    }

    public abstract Builder dnsConfig(DnsConfig dnsConfig);

    public abstract Builder healthcheck(ContainerConfig.Healthcheck healthcheck);

    public abstract Builder hosts(List<String> hosts);

    public abstract Builder secrets(List<SecretBind> secrets);

    public abstract Builder configs(List<ConfigBind> configs);

    public abstract ContainerSpec build();
  }

  public static ContainerSpec.Builder builder() {
    return new AutoValue_ContainerSpec.Builder();
  }

  @JsonCreator
  static ContainerSpec create(
      @JsonProperty("Image") final String image,
      @JsonProperty("Labels") final Map<String, String> labels,
      @JsonProperty("Hostname") final String hostname,
      @JsonProperty("Command") final List<String> command,
      @JsonProperty("Args") final List<String> args,
      @JsonProperty("Env") final List<String> env,
      @JsonProperty("Dir") final String dir,
      @JsonProperty("User") final String user,
      @JsonProperty("Groups") final List<String> groups,
      @JsonProperty("TTY") final Boolean tty,
      @JsonProperty("Mounts") final List<Mount> mounts,
      @JsonProperty("StopGracePeriod") final Long stopGracePeriod,
      @JsonProperty("Healthcheck") final ContainerConfig.Healthcheck healthcheck,
      @JsonProperty("Hosts") final List<String> hosts,
      @JsonProperty("Secrets") final List<SecretBind> secrets,
      @JsonProperty("DNSConfig") final DnsConfig dnsConfig,
      @JsonProperty("Configs") final List<ConfigBind> configs) {
    final Builder builder = builder()
        .image(image)
        .hostname(hostname)
        .args(args)
        .env(env)
        .dir(dir)
        .user(user)
        .groups(groups)
        .tty(tty)
        .mounts(mounts)
        .stopGracePeriod(stopGracePeriod)
        .healthcheck(healthcheck)
        .hosts(hosts)
        .dnsConfig(dnsConfig)
        .command(command)
        .secrets(secrets)
        .configs(configs);

    if (labels != null) {
      builder.labels(labels);
    }

    return builder.build();
  }
}
