/*
 * Copyright (c) 2014 Spotify AB.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Info {

  @JsonProperty("Containers") private int containers;
  @JsonProperty("Images") private int images;
  @JsonProperty("Driver") private String storageDriver;
  @JsonProperty("ExecutionDriver") private String executionDriver;
  @JsonProperty("KernelVersion") private String kernelVersion;
  @JsonProperty("Debug") private int debug;
  @JsonProperty("NFd") private int fileDescriptors;
  @JsonProperty("NGoroutines") private int goroutines;
  @JsonProperty("NEventsListener") private int eventsListener;
  @JsonProperty("InitPath") private String initPath;
  @JsonProperty("Sockets") private List<String> sockets;

  public int containers() {
    return containers;
  }

  public int images() {
    return images;
  }

  public String storageDriver() {
    return storageDriver;
  }

  public String executionDriver() {
    return executionDriver;
  }

  public String kernelVersion() {
    return kernelVersion;
  }

  public boolean debug() {
    return debug != 0;
  }

  public int fileDescriptors() {
    return fileDescriptors;
  }

  public int goroutines() {
    return goroutines;
  }

  public int eventsListener() {
    return eventsListener;
  }

  public String initPath() {
    return initPath;
  }

  public List<String> sockets() {
    return sockets;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Info info = (Info) o;

    if (containers != info.containers) {
      return false;
    }
    if (debug != info.debug) {
      return false;
    }
    if (eventsListener != info.eventsListener) {
      return false;
    }
    if (fileDescriptors != info.fileDescriptors) {
      return false;
    }
    if (goroutines != info.goroutines) {
      return false;
    }
    if (images != info.images) {
      return false;
    }
    if (executionDriver != null ? !executionDriver.equals(info.executionDriver)
                                : info.executionDriver != null) {
      return false;
    }
    if (initPath != null ? !initPath.equals(info.initPath) : info.initPath != null) {
      return false;
    }
    if (kernelVersion != null ? !kernelVersion.equals(info.kernelVersion)
                              : info.kernelVersion != null) {
      return false;
    }
    if (sockets != null ? !sockets.equals(info.sockets) : info.sockets != null) {
      return false;
    }
    if (storageDriver != null ? !storageDriver.equals(info.storageDriver)
                              : info.storageDriver != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = containers;
    result = 31 * result + images;
    result = 31 * result + (storageDriver != null ? storageDriver.hashCode() : 0);
    result = 31 * result + (executionDriver != null ? executionDriver.hashCode() : 0);
    result = 31 * result + (kernelVersion != null ? kernelVersion.hashCode() : 0);
    result = 31 * result + debug;
    result = 31 * result + fileDescriptors;
    result = 31 * result + goroutines;
    result = 31 * result + eventsListener;
    result = 31 * result + (initPath != null ? initPath.hashCode() : 0);
    result = 31 * result + (sockets != null ? sockets.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Info{" +
           "containers=" + containers +
           ", images=" + images +
           ", storageDriver='" + storageDriver + '\'' +
           ", executionDriver='" + executionDriver + '\'' +
           ", kernelVersion='" + kernelVersion + '\'' +
           ", debug=" + debug +
           ", fileDescriptors=" + fileDescriptors +
           ", goroutines=" + goroutines +
           ", eventsListener=" + eventsListener +
           ", initPath='" + initPath + '\'' +
           ", sockets=" + sockets +
           '}';
  }
}
