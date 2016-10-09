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

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class Mount {

  @JsonProperty("Type")
  private String type;

  @JsonProperty("Source")
  private String source;

  @JsonProperty("Target")
  private String target;

  @JsonProperty("ReadOnly")
  private Boolean readOnly;

  @JsonProperty("BindOptions")
  private BindOptions bindOptions;

  @JsonProperty("VolumeOptions")
  private VolumeOptions volumeOptions;

  public String type() {
    return type;
  }

  public String source() {
    return source;
  }

  public String target() {
    return target;
  }

  public Boolean readOnly() {
    return readOnly;
  }

  public BindOptions bindOptions() {
    return bindOptions;
  }

  public VolumeOptions volumeOptions() {
    return volumeOptions;
  }

  public static class Builder {

    private Mount mount = new Mount();

    public Builder withType(String type) {
      mount.type = type;
      return this;
    }

    public Builder withSource(String source) {
      mount.source = source;
      return this;
    }

    public Builder withTarget(String target) {
      mount.target = target;
      return this;
    }

    public Builder makeReadOnly() {
      mount.readOnly = true;
      return this;
    }

    public Builder makeReadOnly(boolean readOnly) {
      mount.readOnly = readOnly;
      return this;
    }

    public Builder withBindOptions(BindOptions bindOptions) {
      mount.bindOptions = bindOptions;
      return this;
    }

    public Builder withVolumeOptions(VolumeOptions volumeOptions) {
      mount.volumeOptions = volumeOptions;
      return this;
    }

    public Mount build() {
      return mount;
    }
  }

  public static Mount.Builder builder() {
    return new Mount.Builder();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final Mount that = (Mount) o;

    return Objects.equals(this.type, that.type) && Objects.equals(this.source, that.source)
           && Objects.equals(this.target, that.target)
           && Objects.equals(this.readOnly, that.readOnly)
           && Objects.equals(this.bindOptions, that.bindOptions)
           && Objects.equals(this.volumeOptions, that.volumeOptions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, source, target, readOnly, bindOptions, volumeOptions);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("type", type).add("source", source)
        .add("target", target).add("readOnly", readOnly).add("bindOptions", bindOptions)
        .add("volumeOptions", volumeOptions).toString();
  }
}
