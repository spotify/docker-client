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

package com.spotify.docker.client;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.io.Resources;

import java.io.BufferedInputStream;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.junit.Test;

public class CompressedDirectoryTest {

  @Test
  public void testFile() throws Exception {
    // note: Paths.get(someURL.toUri()) is the platform-neutral way to convert a URL to a Path
    final URL dockerDirectory = Resources.getResource("dockerDirectory");
    try (CompressedDirectory dir = CompressedDirectory.create(Paths.get(dockerDirectory.toURI()));
         BufferedInputStream fileIn = new BufferedInputStream(Files.newInputStream(dir.file()));
         GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(fileIn);
         TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn)) {

      final List<String> names = new ArrayList<>();
      TarArchiveEntry entry;
      while ((entry = tarIn.getNextTarEntry()) != null) {
        final String name = entry.getName();
        names.add(name);
      }
      assertThat(names,
                 containsInAnyOrder("Dockerfile", "bin/", "bin/date.sh",
                                    "innerDir/", "innerDir/innerDockerfile"));
    }
  }

  @Test
  public void testFileWithIgnore() throws Exception {
    // note: Paths.get(someURL.toUri()) is the platform-neutral way to convert a URL to a Path
    final URL dockerDirectory = Resources.getResource("dockerDirectoryWithIgnore");
    try (CompressedDirectory dir = CompressedDirectory.create(Paths.get(dockerDirectory.toURI()));
         BufferedInputStream fileIn = new BufferedInputStream(Files.newInputStream(dir.file()));
         GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(fileIn);
         TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn)) {

      final List<String> names = new ArrayList<>();
      TarArchiveEntry entry;
      while ((entry = tarIn.getNextTarEntry()) != null) {
        final String name = entry.getName();
        names.add(name);
      }
      assertThat(names, containsInAnyOrder("Dockerfile", "bin/", "bin/date.sh", "subdir2/",
                                           "subdir2/keep.me", "subdir2/do-not.ignore",
                                           "subdir3/do.keep", ".dockerignore"));
    }
  }

  @Test
  public void testFileWithEmptyDirectory() throws Exception {
    Path tempDir = Files.createTempDirectory("dockerDirectoryEmptySubdirectory");
    tempDir.toFile().deleteOnExit();
    assertThat(new File(tempDir.toFile(), "emptySubDir").mkdir(), is(true));

    try (CompressedDirectory dir = CompressedDirectory.create(tempDir);
         BufferedInputStream fileIn = new BufferedInputStream(Files.newInputStream(dir.file()));
         GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(fileIn);
         TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn)) {

      final List<String> names = new ArrayList<>();
      TarArchiveEntry entry;
      while ((entry = tarIn.getNextTarEntry()) != null) {
        final String name = entry.getName();
        names.add(name);
      }
      assertThat(names, contains("emptySubDir/"));
    }
  }

}
