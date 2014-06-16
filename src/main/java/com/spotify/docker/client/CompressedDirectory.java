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

package com.spotify.docker.client;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Set;

import static org.apache.commons.compress.archivers.tar.TarArchiveOutputStream.BIGNUMBER_POSIX;
import static org.apache.commons.compress.archivers.tar.TarArchiveOutputStream.LONGFILE_POSIX;

/**
 * This helper class is used during the docker build command to create a gzip tarball
 * of a directory containing a Dockerfile.
 */
class CompressedDirectory {

  private static final Logger log = LoggerFactory.getLogger(CompressedDirectory.class);

  /**
   * This method creates a gzip tarball of the specified directory. File permissions will be
   * retained. The file will be created in a temporary directory using the
   * {@link File#createTempFile(String, String)} method. If the method returns successfully, it is
   * the caller's responsibility to delete the file.
   *
   * @param directory the directory to compress
   * @return a File object representing the compressed directory
   * @throws IOException
   */
  public static File create(final String directory) throws IOException {
    return create(Paths.get(directory));
  }

  /**
   * This method creates a gzip tarball of the specified directory. File permissions will be
   * retained. The file will be created in a temporary directory using the
   * {@link File#createTempFile(String, String)} method. If the method returns successfully, it is
   * the caller's responsibility to delete the file.
   *
   * @param directory the directory to compress
   * @return a File object representing the compressed directory
   * @throws IOException
   */
  public static File create(final Path directory) throws IOException {
    final File file = File.createTempFile("docker-client-", ".tar.gz");

    try (FileOutputStream fileOut = new FileOutputStream(file);
         GzipCompressorOutputStream gzipOut = new GzipCompressorOutputStream(fileOut);
         TarArchiveOutputStream tarOut = new TarArchiveOutputStream(gzipOut)) {
      tarOut.setLongFileMode(LONGFILE_POSIX);
      tarOut.setBigNumberMode(BIGNUMBER_POSIX);
      Files.walkFileTree(directory,
                         EnumSet.of(FileVisitOption.FOLLOW_LINKS),
                         Integer.MAX_VALUE,
                         new Visitor(directory, tarOut));

    } catch (Throwable t) {
      // If an error occurs, delete temporary file before rethrowing exception.
      delete(file);
      throw t;
    }

    return file;
  }

  /**
   * Convenience method for deleting files. This method safely handles null values, and will never
   * throw an exception.
   * @param file the file to delete.
   * @return true if file was deleted successfully, otherwise false.
   */
  public static boolean delete(File file) {
    if (file == null) {
      return false;
    }

    boolean deleted = false;
    try {
      deleted = file.delete();
    } catch (Exception ignored) {
    }

    if (!deleted) {
      log.warn("Failed to delete temporary file {}", file.getPath());
    }

    return deleted;
  }

  private static class Visitor extends SimpleFileVisitor<Path> {

    private final Path root;
    private final TarArchiveOutputStream tarStream;

    private Visitor(final Path root, final TarArchiveOutputStream tarStream) {
      this.root = root;
      this.tarStream = tarStream;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
      final TarArchiveEntry entry = new TarArchiveEntry(file.toFile());
      final PosixFileAttributes attr = Files.readAttributes(file, PosixFileAttributes.class);
      final Set<PosixFilePermission> perm = attr.permissions();

      // retain permissions, note these values are octal
      int mode = 0100000;
      mode += 0100 * getModeFromPermissions(
          perm.contains(PosixFilePermission.OWNER_READ),
          perm.contains(PosixFilePermission.OWNER_WRITE),
          perm.contains(PosixFilePermission.OWNER_EXECUTE));

      mode += 010 * getModeFromPermissions(
          perm.contains(PosixFilePermission.GROUP_READ),
          perm.contains(PosixFilePermission.GROUP_WRITE),
          perm.contains(PosixFilePermission.GROUP_EXECUTE));

      mode += getModeFromPermissions(
          perm.contains(PosixFilePermission.OTHERS_READ),
          perm.contains(PosixFilePermission.OTHERS_WRITE),
          perm.contains(PosixFilePermission.OTHERS_EXECUTE));

      final Path relativePath = root.relativize(file);
      entry.setName(relativePath.toString());
      entry.setMode(mode);
      entry.setSize(attr.size());
      tarStream.putArchiveEntry(entry);
      Files.copy(file, tarStream);
      tarStream.closeArchiveEntry();
      return FileVisitResult.CONTINUE;
    }

    private static int getModeFromPermissions(boolean read, boolean write, boolean execute) {
      int result = 0;
      if (read) {
        result += 4;
      }
      if (write) {
        result += 2;
      }
      if (execute) {
        result += 1;
      }
      return result;
    }
  }
}
