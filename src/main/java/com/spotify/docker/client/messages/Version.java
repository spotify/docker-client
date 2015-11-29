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

public class Version {

  @JsonProperty("ApiVersion") private String apiVersion;
  @JsonProperty("Arch") private String arch;
  @JsonProperty("GitCommit") private String gitCommit;
  @JsonProperty("GoVersion") private String goVersion;
  @JsonProperty("KernelVersion") private String kernelVersion;
  @JsonProperty("Os") private String os;
  @JsonProperty("Version") private String version;

  public String apiVersion() {
    return apiVersion;
  }

  public String arch() {
    return arch;
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

    final Version version1 = (Version) o;

    if (apiVersion != null ? !apiVersion.equals(version1.apiVersion)
                           : version1.apiVersion != null) {
      return false;
    }
    if (arch != null ? !arch.equals(version1.arch) : version1.arch != null) {
      return false;
    }
    if (gitCommit != null ? !gitCommit.equals(version1.gitCommit) : version1.gitCommit != null) {
      return false;
    }
    if (goVersion != null ? !goVersion.equals(version1.goVersion) : version1.goVersion != null) {
      return false;
    }
    if (kernelVersion != null ? !kernelVersion.equals(version1.kernelVersion)
                              : version1.kernelVersion != null) {
      return false;
    }
    if (os != null ? !os.equals(version1.os) : version1.os != null) {
      return false;
    }
    if (version != null ? !version.equals(version1.version) : version1.version != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = apiVersion != null ? apiVersion.hashCode() : 0;
    result = 31 * result + (arch != null ? arch.hashCode() : 0);
    result = 31 * result + (gitCommit != null ? gitCommit.hashCode() : 0);
    result = 31 * result + (goVersion != null ? goVersion.hashCode() : 0);
    result = 31 * result + (kernelVersion != null ? kernelVersion.hashCode() : 0);
    result = 31 * result + (os != null ? os.hashCode() : 0);
    result = 31 * result + (version != null ? version.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("apiVersion", apiVersion)
        .add("arch", arch)
        .add("gitCommit", gitCommit)
        .add("goVersion", goVersion)
        .add("kernelVersion", kernelVersion)
        .add("os", os)
        .add("version", version)
        .toString();
  }
}
