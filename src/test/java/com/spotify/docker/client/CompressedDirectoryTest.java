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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.io.Resources;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
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

  // assert that the compressed directory contains
  // a link pair where one of the provided entry
  // names contains a real file and the other is
  // a link to that entry
  private static void verifyLinkPair(final Path contentDir,
                                     final String fileA,
                                     final String fileB) throws Exception {
    boolean foundLink = false;
    try (CompressedDirectory dir = CompressedDirectory.create(contentDir);
         BufferedInputStream fileIn = new BufferedInputStream(Files.newInputStream(dir.file()));
         GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(fileIn);
         TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn)) {

      TarArchiveEntry entry;
      String linkName = null;
      while ((entry = tarIn.getNextTarEntry()) != null) {
        if (fileA.equals(entry.getName())
            || fileB.equals(entry.getName())) {
          final byte[] contents = new byte[16];

          // the first entry of the pair must be the one with data based
          // on the requirements of a tar stream. we don't necessarily
          // know which file will be added first since the ordering is
          // filesystem implementation specific.
          if (linkName == null) {
            assertThat(entry.isLink(), is(false));
            assertThat(entry.getSize(), is(13L));
            assertThat(tarIn.read(contents, 0, contents.length), is(13));
            assertThat(new String(contents, 0, 13, UTF_8), equalTo("Hello, World!"));
            linkName = entry.getName();
          } else {
            assertThat(entry.isLink(), is(true));
            assertThat(entry.getLinkName(), equalTo(linkName));
            assertThat(entry.getSize(), is(0L));
            assertThat(tarIn.read(contents, 0, contents.length), is(-1));
            foundLink = true;
          }
        }
      }
    }
    assertThat(foundLink, is(true));
  }

  @Test
  public void testFileWithHardLinks() throws Exception {
    final Path tempDir = Files.createTempDirectory("dockerDirectoryHardLinks");
    final Path withLinksDir = tempDir.resolve("withLinks");
    final Path withLinksSubDir = withLinksDir.resolve("sub");
    final Path fileA = withLinksDir.resolve("file-a.txt");
    final Path fileB = withLinksSubDir.resolve("file-b.txt");
    final Path fileC = withLinksSubDir.resolve("file-c.txt");
    final Path fileD = withLinksDir.resolve("file-d.txt");
    tempDir.toFile().deleteOnExit();

    assertThat(withLinksDir.toFile().mkdir(), is(true));
    assertThat(withLinksSubDir.toFile().mkdir(), is(true));

    final String payload = "Hello, World!";

    // create a directory tree with four files pointing to two inodes:
    //
    //   file-a.txt ------+-> inode a
    //   file-d.txt ----+ |
    //   sub/           | |
    //     file-b.txt --|-+
    //     file-c.txt --+---> inode c
    try (BufferedWriter writerA = Files.newBufferedWriter(fileA, UTF_8);
         BufferedWriter writerC = Files.newBufferedWriter(fileC, UTF_8)) {
      writerA.write(payload);
      writerC.write(payload);
    }
    Files.createLink(fileB, fileA);
    Files.createLink(fileD, fileC);

    try (CompressedDirectory dir = CompressedDirectory.create(withLinksDir);
         BufferedInputStream fileIn = new BufferedInputStream(Files.newInputStream(dir.file()));
         GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(fileIn);
         TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn)) {

      final List<String> names = new ArrayList<>();
      TarArchiveEntry entry;
      while ((entry = tarIn.getNextTarEntry()) != null) {
        final String name = entry.getName();
        names.add(name);
      }

      assertThat(names, containsInAnyOrder("file-a.txt",
                                           "file-d.txt",
                                           "sub/",
                                           "sub/file-b.txt",
                                           "sub/file-c.txt"));
    }

    verifyLinkPair(withLinksDir, "file-a.txt", "sub/file-b.txt");
    verifyLinkPair(withLinksDir, "sub/file-c.txt", "file-d.txt");
  }

}
