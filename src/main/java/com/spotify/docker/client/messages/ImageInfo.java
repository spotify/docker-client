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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class ImageInfo {

  @JsonProperty("Id")
  private String id;
  @JsonProperty("Parent")
  private String parent;
  @JsonProperty("Comment")
  private String comment;
  @JsonProperty("Created")
  private Date created;
  @JsonProperty("Container")
  private String container;
  @JsonProperty("ContainerConfig")
  private ContainerConfig containerConfig;
  @JsonProperty("DockerVersion")
  private String dockerVersion;
  @JsonProperty("Author")
  private String author;
  @JsonProperty("Config")
  private ContainerConfig config;
  @JsonProperty("Architecture")
  private String architecture;
  @JsonProperty("Os")
  private String os;
  @JsonProperty("Size")
  private Long size;
  @JsonProperty("VirtualSize")
  private Long virtualSize;

  public String id() {
    return id;
  }

  public String parent() {
    return parent;
  }

  public String comment() {
    return comment;
  }

  public Date created() {
    return created == null ? null : new Date(created.getTime());
  }

  public String container() {
    return container;
  }

  public ContainerConfig containerConfig() {
    return containerConfig;
  }

  public String dockerVersion() {
    return dockerVersion;
  }

  public String author() {
    return author;
  }

  public ContainerConfig config() {
    return config;
  }

  public String architecture() {
    return architecture;
  }

  public String os() {
    return os;
  }

  public Long size() {
    return size;
  }

  public Long virtualSize() {
    return virtualSize;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final ImageInfo that = (ImageInfo) o;

    return Objects.equals(this.id, that.id) &&
           Objects.equals(this.parent, that.parent) &&
           Objects.equals(this.comment, that.comment) &&
           Objects.equals(this.created, that.created) &&
           Objects.equals(this.container, that.container) &&
           Objects.equals(this.containerConfig, that.containerConfig) &&
           Objects.equals(this.dockerVersion, that.dockerVersion) &&
           Objects.equals(this.author, that.author) &&
           Objects.equals(this.config, that.config) &&
           Objects.equals(this.architecture, that.architecture) &&
           Objects.equals(this.os, that.os) &&
           Objects.equals(this.size, that.size) &&
           Objects.equals(this.virtualSize, that.virtualSize);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, parent, comment, created, container, containerConfig, dockerVersion,
                        author, config, architecture, os, size, virtualSize);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("parent", parent)
        .add("comment", comment)
        .add("created", created)
        .add("container", container)
        .add("containerConfig", containerConfig)
        .add("dockerVersion", dockerVersion)
        .add("author", author)
        .add("config", config)
        .add("architecture", architecture)
        .add("os", os)
        .add("size", size)
        .add("virtualSize", virtualSize)
        .toString();
  }
}
