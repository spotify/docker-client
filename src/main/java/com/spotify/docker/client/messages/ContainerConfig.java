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

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class ContainerConfig {

  @JsonProperty("Hostname") private String hostname;
  @JsonProperty("Domainname") private String domainname;
  @JsonProperty("User") private String user;
  @JsonProperty("AttachStdin") private Boolean attachStdin;
  @JsonProperty("AttachStdout") private Boolean attachStdout;
  @JsonProperty("AttachStderr") private Boolean attachStderr;
  @JsonProperty("PortSpecs") private ImmutableList<String> portSpecs;
  @JsonProperty("ExposedPorts") private ImmutableSet<String> exposedPorts;
  @JsonProperty("Tty") private Boolean tty;
  @JsonProperty("OpenStdin") private Boolean openStdin;
  @JsonProperty("StdinOnce") private Boolean stdinOnce;
  @JsonProperty("Env") private ImmutableList<String> env;
  @JsonProperty("Cmd") private ImmutableList<String> cmd;
  @JsonProperty("Image") private String image;
  @JsonProperty("Volumes") private ImmutableSet<String> volumes;
  @JsonProperty("WorkingDir") private String workingDir;
  @JsonProperty("Entrypoint") private ImmutableList<String> entrypoint;
  @JsonProperty("NetworkDisabled") private Boolean networkDisabled;
  @JsonProperty("OnBuild") private ImmutableList<String> onBuild;
  @JsonProperty("Labels") private ImmutableMap<String, String> labels;
  @JsonProperty("MacAddress") private String macAddress;
  @JsonProperty("HostConfig") private HostConfig hostConfig;


  private ContainerConfig() {
  }

  private ContainerConfig(final Builder builder) {
    this.hostname = builder.hostname;
    this.domainname = builder.domainname;
    this.user = builder.user;
    this.attachStdin = builder.attachStdin;
    this.attachStdout = builder.attachStdout;
    this.attachStderr = builder.attachStderr;
    this.portSpecs = builder.portSpecs;
    this.exposedPorts = builder.exposedPorts;
    this.tty = builder.tty;
    this.openStdin = builder.openStdin;
    this.stdinOnce = builder.stdinOnce;
    this.env = builder.env;
    this.cmd = builder.cmd;
    this.image = builder.image;
    this.volumes = builder.volumes;
    this.workingDir = builder.workingDir;
    this.entrypoint = builder.entrypoint;
    this.networkDisabled = builder.networkDisabled;
    this.onBuild = builder.onBuild;
    this.labels = builder.labels;
    this.macAddress = builder.macAddress;
    this.hostConfig = builder.hostConfig;
  }

  public String hostname() {
    return hostname;
  }

  public String domainname() {
    return domainname;
  }

  public String user() {
    return user;
  }

  public Boolean attachStdin() {
    return attachStdin;
  }

  public Boolean attachStdout() {
    return attachStdout;
  }

  public Boolean attachStderr() {
    return attachStderr;
  }

  public List<String> portSpecs() {
    return portSpecs;
  }

  public Set<String> exposedPorts() {
    return exposedPorts;
  }

  public Boolean tty() {
    return tty;
  }

  public Boolean openStdin() {
    return openStdin;
  }

  public Boolean stdinOnce() {
    return stdinOnce;
  }

  public List<String> env() {
    return env;
  }

  public List<String> cmd() {
    return cmd;
  }

  public String image() {
    return image;
  }

  public Set<String> volumes() {
    return volumes;
  }

  public String workingDir() {
    return workingDir;
  }

  public List<String> entrypoint() {
    return entrypoint;
  }

  public Boolean networkDisabled() {
    return networkDisabled;
  }

  public List<String> onBuild() {
    return onBuild;
  }

  public Map<String, String> labels() {
    return labels;
  }

  public String macAddress() {
    return macAddress;
  }

  public HostConfig hostConfig() {
    return hostConfig;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final ContainerConfig config = (ContainerConfig) o;

    if (attachStderr != null ? !attachStderr.equals(config.attachStderr)
                             : config.attachStderr != null) {
      return false;
    }
    if (attachStdin != null ? !attachStdin.equals(config.attachStdin)
                            : config.attachStdin != null) {
      return false;
    }
    if (attachStdout != null ? !attachStdout.equals(config.attachStdout)
                             : config.attachStdout != null) {
      return false;
    }
    if (cmd != null ? !cmd.equals(config.cmd) : config.cmd != null) {
      return false;
    }
    if (domainname != null ? !domainname.equals(config.domainname) : config.domainname != null) {
      return false;
    }
    if (entrypoint != null ? !entrypoint.equals(config.entrypoint) : config.entrypoint != null) {
      return false;
    }
    if (env != null ? !env.equals(config.env) : config.env != null) {
      return false;
    }
    if (exposedPorts != null ? !exposedPorts.equals(config.exposedPorts)
                             : config.exposedPorts != null) {
      return false;
    }
    if (hostname != null ? !hostname.equals(config.hostname) : config.hostname != null) {
      return false;
    }
    if (image != null ? !image.equals(config.image) : config.image != null) {
      return false;
    }
    if (networkDisabled != null ? !networkDisabled.equals(config.networkDisabled)
                                : config.networkDisabled != null) {
      return false;
    }
    if (onBuild != null ? !onBuild.equals(config.onBuild) : config.onBuild != null) {
      return false;
    }
    if (openStdin != null ? !openStdin.equals(config.openStdin) : config.openStdin != null) {
      return false;
    }
    if (portSpecs != null ? !portSpecs.equals(config.portSpecs) : config.portSpecs != null) {
      return false;
    }
    if (stdinOnce != null ? !stdinOnce.equals(config.stdinOnce) : config.stdinOnce != null) {
      return false;
    }
    if (tty != null ? !tty.equals(config.tty) : config.tty != null) {
      return false;
    }
    if (user != null ? !user.equals(config.user) : config.user != null) {
      return false;
    }
    if (volumes != null ? !volumes.equals(config.volumes) : config.volumes != null) {
      return false;
    }
    if (workingDir != null ? !workingDir.equals(config.workingDir) : config.workingDir != null) {
      return false;
    }

    if (labels != null ? !labels.equals(config.labels) : config.labels != null) {
      return false;
    }

    if (macAddress != null ? !macAddress.equals(config.macAddress) : config.macAddress != null) {
      return false;
    }

    if (hostConfig != null ? !hostConfig.equals(config.hostConfig) : config.hostConfig != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = hostname != null ? hostname.hashCode() : 0;
    result = 31 * result + (domainname != null ? domainname.hashCode() : 0);
    result = 31 * result + (user != null ? user.hashCode() : 0);
    result = 31 * result + (attachStdin != null ? attachStdin.hashCode() : 0);
    result = 31 * result + (attachStdout != null ? attachStdout.hashCode() : 0);
    result = 31 * result + (attachStderr != null ? attachStderr.hashCode() : 0);
    result = 31 * result + (portSpecs != null ? portSpecs.hashCode() : 0);
    result = 31 * result + (exposedPorts != null ? exposedPorts.hashCode() : 0);
    result = 31 * result + (tty != null ? tty.hashCode() : 0);
    result = 31 * result + (openStdin != null ? openStdin.hashCode() : 0);
    result = 31 * result + (stdinOnce != null ? stdinOnce.hashCode() : 0);
    result = 31 * result + (env != null ? env.hashCode() : 0);
    result = 31 * result + (cmd != null ? cmd.hashCode() : 0);
    result = 31 * result + (image != null ? image.hashCode() : 0);
    result = 31 * result + (volumes != null ? volumes.hashCode() : 0);
    result = 31 * result + (workingDir != null ? workingDir.hashCode() : 0);
    result = 31 * result + (entrypoint != null ? entrypoint.hashCode() : 0);
    result = 31 * result + (networkDisabled != null ? networkDisabled.hashCode() : 0);
    result = 31 * result + (onBuild != null ? onBuild.hashCode() : 0);
    result = 31 * result + (labels != null ? labels.hashCode() : 0);
    result = 31 * result + (macAddress != null ? macAddress.hashCode() : 0);
    result = 31 * result + (hostConfig != null ? hostConfig.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("hostname", hostname)
        .add("domainname", domainname)
        .add("username", user)
        .add("attachStdin", attachStdin)
        .add("attachStdout", attachStdout)
        .add("attachStderr", attachStderr)
        .add("portSpecs", portSpecs)
        .add("exposedPorts", exposedPorts)
        .add("tty", tty)
        .add("openStdin", openStdin)
        .add("stdinOnce", stdinOnce)
        .add("env", env)
        .add("cmd", cmd)
        .add("image", image)
        .add("volumes", volumes)
        .add("workingDir", workingDir)
        .add("entrypoint", entrypoint)
        .add("networkDisabled", networkDisabled)
        .add("onBuild", onBuild)
        .add("labels", labels)
        .add("macAddress", macAddress)
        .add("hostConfig", hostConfig)
        .toString();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String hostname;
    private String domainname;
    private String user;
    private Long memory;
    private Long memorySwap;
    private Long cpuShares;
    private String cpuset;
    private Boolean attachStdin;
    private Boolean attachStdout;
    private Boolean attachStderr;
    private ImmutableList<String> portSpecs;
    private ImmutableSet<String> exposedPorts;
    private Boolean tty;
    private Boolean openStdin;
    private Boolean stdinOnce;
    private ImmutableList<String> env;
    private ImmutableList<String> cmd;
    private String image;
    private ImmutableSet<String> volumes;
    private String workingDir;
    private ImmutableList<String> entrypoint;
    private Boolean networkDisabled;
    private ImmutableList<String> onBuild;
    private ImmutableMap<String, String> labels;
    private String macAddress;
    private HostConfig hostConfig;

    private Builder() {
    }

    private Builder(final ContainerConfig config) {
      this.hostname = config.hostname;
      this.domainname = config.domainname;
      this.user = config.user;
      this.attachStdin = config.attachStdin;
      this.attachStdout = config.attachStdout;
      this.attachStderr = config.attachStderr;
      this.portSpecs = config.portSpecs;
      this.exposedPorts = config.exposedPorts;
      this.tty = config.tty;
      this.openStdin = config.openStdin;
      this.stdinOnce = config.stdinOnce;
      this.env = config.env;
      this.cmd = config.cmd;
      this.image = config.image;
      this.volumes = config.volumes;
      this.workingDir = config.workingDir;
      this.entrypoint = config.entrypoint;
      this.networkDisabled = config.networkDisabled;
      this.onBuild = config.onBuild;
      this.labels = config.labels;
      this.macAddress = config.macAddress;
      this.hostConfig = config.hostConfig;
    }

    public Builder hostname(final String hostname) {
      this.hostname = hostname;
      return this;
    }

    public String hostname() {
      return hostname;
    }

    public Builder domainname(final String domainname) {
      this.domainname = domainname;
      return this;
    }

    public String domainname() {
      return domainname;
    }

    public Builder user(final String user) {
      this.user = user;
      return this;
    }

    public String user() {
      return user;
    }

    public Builder memory(final Long memory) {
      this.memory = memory;
      return this;
    }

    public Long memory() {
      return memory;
    }

    public Builder memorySwap(final Long memorySwap) {
      this.memorySwap = memorySwap;
      return this;
    }

    public Long memorySwap() {
      return memorySwap;
    }

    public Builder cpuShares(final Long cpuShares) {
      this.cpuShares = cpuShares;
      return this;
    }

    public Long cpuShares() {
      return cpuShares;
    }

    public Builder cpuset(final String cpuset) {
      this.cpuset = cpuset;
      return this;
    }

    public String cpuset() {
      return cpuset;
    }

    public Builder attachStdin(final Boolean attachStdin) {
      this.attachStdin = attachStdin;
      return this;
    }

    public Boolean attachStdin() {
      return attachStdin;
    }

    public Builder attachStdout(final Boolean attachStdout) {
      this.attachStdout = attachStdout;
      return this;
    }

    public Boolean attachStdout() {
      return attachStdout;
    }

    public Builder attachStderr(final Boolean attachStderr) {
      this.attachStderr = attachStderr;
      return this;
    }

    public Boolean attachStderr() {
      return attachStderr;
    }

    public Builder portSpecs(final List<String> portSpecs) {
      if (portSpecs != null && !portSpecs.isEmpty()) {
        this.portSpecs = ImmutableList.copyOf(portSpecs);
      }

      return this;
    }

    public Builder portSpecs(final String... portSpecs) {
      if (portSpecs != null && portSpecs.length > 0) {
        this.portSpecs = ImmutableList.copyOf(portSpecs);
      }

      return this;
    }

    public List<String> portSpecs() {
      return portSpecs;
    }

    public Builder exposedPorts(final Set<String> exposedPorts) {
      if (exposedPorts != null && !exposedPorts.isEmpty()) {
        this.exposedPorts = ImmutableSet.copyOf(exposedPorts);
      }

      return this;
    }

    public Builder exposedPorts(final String... exposedPorts) {
      if (exposedPorts != null && exposedPorts.length > 0) {
        this.exposedPorts = ImmutableSet.copyOf(exposedPorts);
      }

      return this;
    }

    public Set<String> exposedPorts() {
      return exposedPorts;
    }

    public Builder tty(final Boolean tty) {
      this.tty = tty;
      return this;
    }

    public Boolean tty() {
      return tty;
    }

    public Builder openStdin(final Boolean openStdin) {
      this.openStdin = openStdin;
      return this;
    }

    public Boolean openStdin() {
      return openStdin;
    }

    public Builder stdinOnce(final Boolean stdinOnce) {
      this.stdinOnce = stdinOnce;
      return this;
    }

    public Boolean stdinOnce() {
      return stdinOnce;
    }

    public Builder env(final List<String> env) {
      if (env != null && !env.isEmpty()) {
        this.env = ImmutableList.copyOf(env);
      }

      return this;
    }

    public Builder env(final String... env) {
      if (env != null && env.length > 0) {
        this.env = ImmutableList.copyOf(env);
      }

      return this;
    }

    public List<String> env() {
      return env;
    }

    public Builder cmd(final List<String> cmd) {
      if (cmd != null && !cmd.isEmpty()) {
        this.cmd = ImmutableList.copyOf(cmd);
      }

      return this;
    }

    public Builder cmd(final String... cmd) {
      if (cmd != null && cmd.length > 0) {
        this.cmd = ImmutableList.copyOf(cmd);
      }

      return this;
    }

    public List<String> cmd() {
      return cmd;
    }

    public Builder image(final String image) {
      this.image = image;
      return this;
    }

    public String image() {
      return image;
    }

    public Builder volumes(final Set<String> volumes) {
      if (volumes != null && !volumes.isEmpty()) {
        this.volumes = ImmutableSet.copyOf(volumes);
      }

      return this;
    }

    public Builder volumes(final String... volumes) {
      if (volumes != null && volumes.length > 0) {
        this.volumes = ImmutableSet.copyOf(volumes);
      }

      return this;
    }

    public Set<String> volumes() {
      return volumes;
    }

    public Builder workingDir(final String workingDir) {
      this.workingDir = workingDir;
      return this;
    }

    public String workingDir() {
      return workingDir;
    }

    public Builder entrypoint(final List<String> entrypoint) {
      if (entrypoint != null && !entrypoint.isEmpty()) {
        this.entrypoint = ImmutableList.copyOf(entrypoint);
      }

      return this;
    }

    public Builder entrypoint(final String... entrypoint) {
      if (entrypoint != null && entrypoint.length > 0) {
        this.entrypoint = ImmutableList.copyOf(entrypoint);
      }

      return this;
    }

    public List<String> entrypoint() {
      return entrypoint;
    }

    public Builder networkDisabled(final Boolean networkDisabled) {
      this.networkDisabled = networkDisabled;
      return this;
    }

    public Boolean networkDisabled() {
      return networkDisabled;
    }

    public Builder onBuild(final List<String> onBuild) {
      if (onBuild != null && !onBuild.isEmpty()) {
        this.onBuild = ImmutableList.copyOf(onBuild);
      }

      return this;
    }

    public Builder onBuild(final String... onBuild) {
      if (onBuild != null && onBuild.length > 0) {
        this.onBuild = ImmutableList.copyOf(onBuild);
      }

      return this;
    }

    public List<String> onBuild() {
      return onBuild;
    }

    public Builder labels(final Map<String, String> labels) {
      if (labels != null && !labels.isEmpty()) {
        this.labels = ImmutableMap.copyOf(labels);
      }

      return this;
    }

    public Map<String, String> labels() {
      return labels;
    }

    public Builder macAddress(final String macAddress) {
      this.macAddress = macAddress;
      return this;
    }

    public String macAddress() {
      return macAddress;
    }

    public Builder hostConfig(final HostConfig hostConfig) {
      this.hostConfig = hostConfig;
      return this;
    }

    public HostConfig hostConfig() {
      return hostConfig;
    }

    public ContainerConfig build() {
      return new ContainerConfig(this);
    }
  }
}
