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

import java.util.Date;
import javax.annotation.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class ImageInfo {

  @JsonProperty("Id")
  public abstract String id();

  @JsonProperty("Parent")
  public abstract String parent();

  @JsonProperty("Comment")
  public abstract String comment();

  @JsonProperty("Created")
  public abstract Date created();

  @JsonProperty("Container")
  public abstract String container();

  @JsonProperty("ContainerConfig")
  public abstract ContainerConfig containerConfig();

  @JsonProperty("DockerVersion")
  public abstract String dockerVersion();

  @JsonProperty("Author")
  public abstract String author();

  @JsonProperty("Config")
  public abstract ContainerConfig config();

  @JsonProperty("Architecture")
  public abstract String architecture();

  @JsonProperty("Os")
  public abstract String os();

  @JsonProperty("Size")
  public abstract Long size();

  @JsonProperty("VirtualSize")
  public abstract Long virtualSize();

  @Nullable
  @JsonProperty("RootFS")
  public abstract RootFs rootFs();

  @JsonCreator
  static ImageInfo create(
      @JsonProperty("Id") final String id,
      @JsonProperty("Parent") final String parent,
      @JsonProperty("Comment") final String comment,
      @JsonProperty("Created") final Date created,
      @JsonProperty("Container") final String container,
      @JsonProperty("ContainerConfig") final ContainerConfig containerConfig,
      @JsonProperty("DockerVersion") final String dockerVersion,
      @JsonProperty("Author") final String author,
      @JsonProperty("Config") final ContainerConfig config,
      @JsonProperty("Architecture") final String architecture,
      @JsonProperty("Os") final String os,
      @JsonProperty("Size") final Long size,
      @JsonProperty("VirtualSize") final Long virtualSize,
      @JsonProperty("RootFS") final RootFs rootFs) {
    return new AutoValue_ImageInfo(id, parent, comment, created, container, containerConfig,
        dockerVersion, author, config, architecture, os, size, virtualSize, rootFs);
  }
}
