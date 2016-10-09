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

import com.google.common.base.MoreObjects;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class Version {

  @JsonProperty("ApiVersion")
  private String apiVersion;
  @JsonProperty("Arch")
  private String arch;
  @JsonProperty("BuildTime")
  private String buildTime;
  @JsonProperty("GitCommit")
  private String gitCommit;
  @JsonProperty("GoVersion")
  private String goVersion;
  @JsonProperty("KernelVersion")
  private String kernelVersion;
  @JsonProperty("Os")
  private String os;
  @JsonProperty("Version")
  private String version;

  public String apiVersion() {
    return apiVersion;
  }

  public String arch() {
    return arch;
  }

  public String buildTime() {
    return buildTime;
  }

  public String gitCommit() {
    return gitCommit;
  }

  public String goVersion() {
    return goVersion;
  }

  public String kernelVersion() {
    return kernelVersion;
  }

  public String os() {
    return os;
  }

  public String version() {
    return version;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final Version that = (Version) o;

    return Objects.equals(this.apiVersion, that.apiVersion) &&
           Objects.equals(this.arch, that.arch) &&
           Objects.equals(this.buildTime, that.buildTime) &&
           Objects.equals(this.gitCommit, that.gitCommit) &&
           Objects.equals(this.goVersion, that.goVersion) &&
           Objects.equals(this.kernelVersion, that.kernelVersion) &&
           Objects.equals(this.os, that.os) &&
           Objects.equals(this.version, that.version);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apiVersion, arch, buildTime, gitCommit,
                        goVersion, kernelVersion, os, version);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("apiVersion", apiVersion)
        .add("arch", arch)
        .add("buildTime", buildTime)
        .add("gitCommit", gitCommit)
        .add("goVersion", goVersion)
        .add("kernelVersion", kernelVersion)
        .add("os", os)
        .add("version", version)
        .toString();
  }
}
