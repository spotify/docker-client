/*
 * Copyright (c) 2015 Spotify AB.
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
package com.spotify.docker.client.messages.mount;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class VolumeOptions {

  @JsonProperty("NoCopy")
  private Boolean noCopy;

  @JsonProperty("Labels")
  private Map<String, String> labels;

  @JsonProperty("DriverConfig")
  private Driver driverConfig;

  public Boolean noCopy() {
    return noCopy;
  }

  public Map<String, String> labels() {
    return labels;
  }

  public Driver driverConfig() {
    return driverConfig;
  }

  public static class Builder {

    private VolumeOptions volume = new VolumeOptions();

    public Builder withNoCopy() {
      volume.noCopy = true;
      return this;
    }

    public Builder withNoCopy(boolean noCopy) {
      volume.noCopy = noCopy;
      return this;
    }

    public Builder withLabel(String label, String value) {
      if (volume.labels == null) {
        volume.labels = new HashMap<String, String>();
      }
      volume.labels.put(label, value);
      return this;
    }

    public Builder withDriverConfig(Driver driverConfig) {
      volume.driverConfig = driverConfig;
      return this;
    }

    public VolumeOptions build() {
      return volume;
    }
  }

  public static VolumeOptions.Builder builder() {
    return new VolumeOptions.Builder();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final VolumeOptions that = (VolumeOptions) o;

    return Objects.equals(this.noCopy, that.noCopy) && Objects.equals(this.labels, that.labels)
           && Objects.equals(this.driverConfig, that.driverConfig);
  }

  @Override
  public int hashCode() {
    return Objects.hash(noCopy, labels, driverConfig);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("noCopy", noCopy).add("labels", labels)
        .add("driverConfig", driverConfig).toString();
  }
}
