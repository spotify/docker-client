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

package com.spotify.docker.client.messages;

import com.google.common.base.MoreObjects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigInteger;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class MemoryStats {

  @JsonProperty("stats")
  private Stats stats;
  @JsonProperty("max_usage")
  private Long maxUsage;
  @JsonProperty("usage")
  private Long usage;
  @JsonProperty("failcnt")
  private Long failcnt;
  @JsonProperty("limit")
  private Long limit;

  public Stats stats() {
    return stats;
  }

  public Long maxUsage() {
    return maxUsage;
  }

  public Long usage() {
    return usage;
  }

  public Long failcnt() {
    return failcnt;
  }

  public Long limit() {
    return limit;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final MemoryStats that = (MemoryStats) o;

    return Objects.equals(this.stats, that.stats) &&
           Objects.equals(this.maxUsage, that.maxUsage) &&
           Objects.equals(this.usage, that.usage) &&
           Objects.equals(this.failcnt, that.failcnt) &&
           Objects.equals(this.limit, that.limit);
  }

  @Override
  public int hashCode() {
    return Objects.hash(stats, maxUsage, usage, failcnt, limit);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("stats", stats)
        .add("failcnt", failcnt)
        .add("limit", limit)
        .add("maxUsage", maxUsage)
        .add("usage", usage)
        .toString();
  }

  public static class Stats {

    @JsonProperty("total_pgmajfault")
    private Long totalPgmajfault;
    @JsonProperty("cache")
    private Long cache;
    @JsonProperty("mapped_file")
    private Long mappedFile;
    @JsonProperty("total_inactive_file")
    private Long totalInactiveFile;
    @JsonProperty("pgpgout")
    private Long pgpgout;
    @JsonProperty("rss")
    private Long rss;
    @JsonProperty("total_mapped_file")
    private Long totalMappedFile;
    @JsonProperty("writeback")
    private Long writeback;
    @JsonProperty("unevictable")
    private Long unevictable;
    @JsonProperty("pgpgin")
    private Long pgpgin;
    @JsonProperty("total_unevictable")
    private Long totalUnevictable;
    @JsonProperty("pgmajfault")
    private Long pgmajfault;
    @JsonProperty("total_rss")
    private Long totalRss;
    @JsonProperty("total_rss_huge")
    private Long totalRssHuge;
    @JsonProperty("total_writeback")
    private Long totalWriteback;
    @JsonProperty("total_inactive_anon")
    private Long totalInactiveAnon;
    @JsonProperty("rss_huge")
    private Long rssHuge;
    @JsonProperty("hierarchical_memory_limit")
    private BigInteger hierarchicalMemoryLimit;
    @JsonProperty("total_pgfault")
    private Long totalPgfault;
    @JsonProperty("total_active_file")
    private Long totalActiveFile;
    @JsonProperty("active_anon")
    private Long activeAnon;
    @JsonProperty("total_active_anon")
    private Long totalActiveAnon;
    @JsonProperty("total_pgpgout")
    private Long totalPgpgout;
    @JsonProperty("total_cache")
    private Long totalCache;
    @JsonProperty("inactive_anon")
    private Long inactiveAnon;
    @JsonProperty("active_file")
    private Long activeFile;
    @JsonProperty("pgfault")
    private Long pgfault;
    @JsonProperty("inactive_file")
    private Long inactiveFile;
    @JsonProperty("total_pgpgin")
    private Long totalPgpgin;

    public Long totalPgmajfault() {
      return totalPgmajfault;
    }

    public Long cache() {
      return cache;
    }

    public Long mappedFile() {
      return mappedFile;
    }

    public Long totalInactiveFile() {
      return totalInactiveFile;
    }

    public Long pgpgout() {
      return pgpgout;
    }

    public Long rss() {
      return rss;
    }

    public Long totalMappedFile() {
      return totalMappedFile;
    }

    public Long writeback() {
      return writeback;
    }

    public Long unevictable() {
      return unevictable;
    }

    public Long pgpgin() {
      return pgpgin;
    }

    public Long totalUnevictable() {
      return totalUnevictable;
    }

    public Long pgmajfault() {
      return pgmajfault;
    }

    public Long totalRss() {
      return totalRss;
    }

    public Long totalRssHuge() {
      return totalRssHuge;
    }

    public Long totalWriteback() {
      return totalWriteback;
    }

    public Long totalInactiveAnon() {
      return totalInactiveAnon;
    }

    public Long rssHuge() {
      return rssHuge;
    }

    public BigInteger hierarchicalMemoryLimit() {
      return hierarchicalMemoryLimit;
    }

    public Long totalPgfault() {
      return totalPgfault;
    }

    public Long totalActiveFile() {
      return totalActiveFile;
    }

    public Long activeAnon() {
      return activeAnon;
    }

    public Long totalActiveAnon() {
      return totalActiveAnon;
    }

    public Long totalPgpgout() {
      return totalPgpgout;
    }

    public Long totalCache() {
      return totalCache;
    }

    public Long inactiveAnon() {
      return inactiveAnon;
    }

    public Long activeFile() {
      return activeFile;
    }

    public Long pgfault() {
      return pgfault;
    }

    public Long inactiveFile() {
      return inactiveFile;
    }

    public Long totalPgpgin() {
      return totalPgpgin;
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }

      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      final Stats that = (Stats) o;

      return Objects.equals(this.totalPgmajfault, that.totalPgmajfault) &&
             Objects.equals(this.cache, that.cache) &&
             Objects.equals(this.mappedFile, that.mappedFile) &&
             Objects.equals(this.totalInactiveFile, that.totalInactiveFile) &&
             Objects.equals(this.pgpgout, that.pgpgout) &&
             Objects.equals(this.rss, that.rss) &&
             Objects.equals(this.totalMappedFile, that.totalMappedFile) &&
             Objects.equals(this.writeback, that.writeback) &&
             Objects.equals(this.unevictable, that.unevictable) &&
             Objects.equals(this.pgpgin, that.pgpgin) &&
             Objects.equals(this.totalUnevictable, that.totalUnevictable) &&
             Objects.equals(this.pgmajfault, that.pgmajfault) &&
             Objects.equals(this.totalRss, that.totalRss) &&
             Objects.equals(this.totalRssHuge, that.totalRssHuge) &&
             Objects.equals(this.totalWriteback, that.totalWriteback) &&
             Objects.equals(this.totalInactiveAnon, that.totalInactiveAnon) &&
             Objects.equals(this.rssHuge, that.rssHuge) &&
             Objects.equals(this.hierarchicalMemoryLimit,
                            that.hierarchicalMemoryLimit) &&
             Objects.equals(this.totalPgfault, that.totalPgfault) &&
             Objects.equals(this.totalActiveFile, that.totalActiveFile) &&
             Objects.equals(this.activeAnon, that.activeAnon) &&
             Objects.equals(this.totalActiveAnon, that.totalActiveAnon) &&
             Objects.equals(this.totalPgpgout, that.totalPgpgout) &&
             Objects.equals(this.totalCache, that.totalCache) &&
             Objects.equals(this.inactiveAnon, that.inactiveAnon) &&
             Objects.equals(this.activeFile, that.activeFile) &&
             Objects.equals(this.pgfault, that.pgfault) &&
             Objects.equals(this.inactiveFile, that.inactiveFile) &&
             Objects.equals(this.totalPgpgin, that.totalPgpgin);
    }

    @Override
    public int hashCode() {
      return Objects.hash(totalPgmajfault, cache, mappedFile, totalInactiveFile,
                          pgpgout, rss, totalMappedFile, writeback, unevictable, pgpgin,
                          totalUnevictable,
                          pgmajfault, totalRss, totalRssHuge, totalWriteback, totalInactiveAnon,
                          rssHuge,
                          hierarchicalMemoryLimit, totalPgfault, totalActiveFile, activeAnon,
                          totalActiveAnon, totalPgpgout, totalCache, inactiveAnon, activeFile,
                          pgfault,
                          inactiveFile, totalPgpgin);
    }
  }
}
