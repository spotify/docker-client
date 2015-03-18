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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Info {

  @JsonProperty("Containers") private int containers;
  @JsonProperty("Images") private int images;
  @JsonProperty("Driver") private String storageDriver;
  @JsonProperty("DriverStatus") private List<List<String>> driverStatus;
  @JsonProperty("ExecutionDriver") private String executionDriver;
  @JsonProperty("KernelVersion") private String kernelVersion;
  @JsonProperty("NCPU") private int cpus;
  @JsonProperty("MemTotal") private long memTotal;
  @JsonProperty("Name") private String name;
  @JsonProperty("ID") private String id;
  @JsonProperty("OperatingSystem") private String operatingSystem;
  @JsonProperty("Debug") private int debug;
  @JsonProperty("NFd") private int fileDescriptors;
  @JsonProperty("NGoroutines") private int goroutines;
  @JsonProperty("NEventsListener") private int eventsListener;
  @JsonProperty("InitPath") private String initPath;
  @JsonProperty("InitSha1") private String initSha1;
  @JsonProperty("IndexServerAddress") private String indexServerAddress;
  @JsonProperty("Sockets") private List<String> sockets;
  @JsonProperty("MemoryLimit") private Boolean memoryLimit;
  @JsonProperty("SwapLimit") private Boolean swapLimit;
  @JsonProperty("IPv4Forwarding") private boolean ipv4Forwarding;
  @JsonProperty("Labels") private List<String> labels;
  @JsonProperty("DockerRootDir") private String dockerRootDir;

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

  public String operatingSystem() {
    return operatingSystem;
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

  public boolean memoryLimit() {
    return memoryLimit;
  }

  public boolean swapLimit() {
    return swapLimit;
  }
  
  public List<List<String>> driverStatus() {
    return driverStatus;
  }

  public int cpus() {
    return cpus;
  }

  public long memTotal() {
    return memTotal;
  }

  public String name() {
    return name;
  }

  public String id() {
    return id;
  }

  public String initSha1() {
    return initSha1;
  }

  public String indexServerAddress() {
    return indexServerAddress;
  }

  public boolean ipv4Forwarding() {
    return ipv4Forwarding;
  }

  public List<String> labels() {
    return labels;
  }

  public String dockerRootDir() {
    return dockerRootDir;
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
    if (memoryLimit != null ? !memoryLimit.equals(info.memoryLimit)
                            : info.memoryLimit != null) {
      return false;
    }
    if (swapLimit != null ? !swapLimit.equals(info.swapLimit)
                            : info.swapLimit != null) {
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
    result = 31 * result + (memoryLimit != null ? memoryLimit.hashCode() : 0);
    result = 31 * result + (swapLimit != null ? swapLimit.hashCode() : 0);
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
           ", memoryLimit=" + memoryLimit +
           ", swapLimit=" + swapLimit +
           '}';
  }
}
