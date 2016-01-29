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

package com.spotify.docker.client;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Pattern;

import static org.apache.commons.compress.archivers.tar.TarArchiveOutputStream.BIGNUMBER_POSIX;
import static org.apache.commons.compress.archivers.tar.TarArchiveOutputStream.LONGFILE_POSIX;

/**
 * This helper class is used during the docker build command to create a gzip tarball of a directory
 * containing a Dockerfile.
 */
class CompressedDirectory implements Closeable {

  private static final Logger log = LoggerFactory.getLogger(CompressedDirectory.class);

  /**
   * Default mode to be applied to tar file entries if detailed Posix-compliant mode cannot be
   * obtained.
   */
  private static final int DEFAULT_FILE_MODE = TarArchiveEntry.DEFAULT_FILE_MODE;

  /**
   * Identifier used to indicate the OS supports a Posix compliant view of the file system.
   *
   * @see PosixFileAttributeView#name()
   */
  private static final String POSIX_FILE_VIEW = "posix";

  private final Path file;

  private CompressedDirectory(Path file) {
    this.file = file;
  }

  /**
   * The file for the created compressed directory archive.
   * @return a Path object representing the compressed directory
   */
  public Path file() {
    return file;
  }

  /**
   * This method creates a gzip tarball of the specified directory. File permissions will be
   * retained. The file will be created in a temporary directory using the {@link
   * Files#createTempFile(String, String, FileAttribute[])} method. The returned object is
   * auto-closeable, and upon closing it, the archive file will be deleted.
   *
   * @param directory the directory to compress
   * @return a Path object representing the compressed directory
   * @throws IOException if the compressed directory could not be created.
   */
  public static CompressedDirectory create(final Path directory) throws IOException {
    final Path file = Files.createTempFile("docker-client-", ".tar.gz");

    final Path dockerIgnorePath = directory.resolve(".dockerignore");
    final ImmutableSet<PathMatcher> ignoreMatchers = parseDockerIgnore(dockerIgnorePath);

    try (final OutputStream fileOut = Files.newOutputStream(file);
         final GzipCompressorOutputStream gzipOut = new GzipCompressorOutputStream(fileOut);
         final TarArchiveOutputStream tarOut = new TarArchiveOutputStream(gzipOut)) {
      tarOut.setLongFileMode(LONGFILE_POSIX);
      tarOut.setBigNumberMode(BIGNUMBER_POSIX);
      Files.walkFileTree(directory,
                         EnumSet.of(FileVisitOption.FOLLOW_LINKS),
                         Integer.MAX_VALUE,
                         new Visitor(directory, ignoreMatchers, tarOut));

    } catch (Throwable t) {
      // If an error occurs, delete temporary file before rethrowing exception.
      try {
        Files.delete(file);
      } catch (IOException e) {
        // So we don't lose track of the reason the file was deleted... might be important
        t.addSuppressed(e);
      }

      throw t;
    }

    return new CompressedDirectory(file);
  }

  @Override
  public void close() throws IOException {
    Files.delete(file);
  }

  static ImmutableSet<PathMatcher> parseDockerIgnore(Path dockerIgnorePath)
      throws IOException {
    final ImmutableSet.Builder<PathMatcher> matchersBuilder = ImmutableSet.builder();

    if (Files.isReadable(dockerIgnorePath) && Files.isRegularFile(dockerIgnorePath)) {
      for (final String line : Files.readAllLines(dockerIgnorePath, StandardCharsets.UTF_8)) {
        final String pattern = createPattern(line);
        if (pattern.isEmpty()) {
          log.debug("Will skip '{}' - cause it's empty after trimming", line);
          continue;
        }
        matchersBuilder.add(goPathMatcher(dockerIgnorePath.getFileSystem(), pattern));
      }
    }

    return matchersBuilder.build();
  }

  private static String createPattern(String line) {

    String pattern = line.trim();

    if (OSUtils.isLinux()) {
      return pattern;
    }

    return pattern.replace("/", "\\\\");
  }

  @VisibleForTesting
  static PathMatcher goPathMatcher(FileSystem fs, String pattern) {
    // Supposed to work the same way as Go's path.filepath.match.Match:
    // http://golang.org/src/path/filepath/match.go#L34

    final String notSeparatorPattern = getNotSeparatorPattern(fs.getSeparator());

    final String starPattern = String.format("%s*", notSeparatorPattern);

    final StringBuilder patternBuilder = new StringBuilder();

    boolean inCharRange = false;
    boolean inEscape = false;

    // This is of course hugely inefficient, but it passes most of the test suite, TDD ftw...
    for (int i = 0; i < pattern.length(); i++) {
      final char c = pattern.charAt(i);
      if (inCharRange) {
        if (inEscape) {
          patternBuilder.append(c);
          inEscape = false;
        } else {
          switch (c) {
            case '\\':
              patternBuilder.append('\\');
              inEscape = true;
              break;
            case ']':
              patternBuilder.append(']');
              inCharRange = false;
              break;
            default:
              patternBuilder.append(c);
          }
        }
      } else {
        if (inEscape) {
          patternBuilder.append(Pattern.quote(Character.toString(c)));
          inEscape = false;
        } else {
          switch (c) {
            case '*':
              patternBuilder.append(starPattern);
              break;
            case '?':
              patternBuilder.append(notSeparatorPattern);
              break;
            case '[':
              patternBuilder.append("[");
              inCharRange = true;
              break;
            case '\\':
              inEscape = true;
              break;
            default:
              patternBuilder.append(Pattern.quote(Character.toString(c)));
          }
        }
      }
    }

    return fs.getPathMatcher("regex:" + patternBuilder.toString());
  }

  private static String getNotSeparatorPattern(String separator) {
    switch (separator) {
      case "/":
        return "[^/]";
      case "\\":
        return "[^\\\\]";
      default:
        final String message = MessageFormat.format(
            "Filepath matching not supported for file system separator {0}",
            separator);
        throw new UnsupportedOperationException(message);
    }
  }

  private static class Visitor extends SimpleFileVisitor<Path> {

    private final Path root;
    private final ImmutableSet<PathMatcher> ignoreMatchers;
    private final TarArchiveOutputStream tarStream;

    private Visitor(final Path root, ImmutableSet<PathMatcher> ignoreMatchers,
                    final TarArchiveOutputStream tarStream) {
      this.root = root;
      this.ignoreMatchers = ignoreMatchers;
      this.tarStream = tarStream;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

      final Path relativePath = root.relativize(file);

      if (anyMatches(ignoreMatchers, relativePath)) {
        return FileVisitResult.CONTINUE;
      }

      final TarArchiveEntry entry = new TarArchiveEntry(file.toFile());
      entry.setName(relativePath.toString());
      entry.setMode(getFileMode(file));
      entry.setSize(attrs.size());
      tarStream.putArchiveEntry(entry);
      Files.copy(file, tarStream);
      tarStream.closeArchiveEntry();
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
        throws IOException {
      final Path relativePath = root.relativize(dir);

      if (anyMatches(ignoreMatchers, relativePath)) {
        return FileVisitResult.SKIP_SUBTREE;
      }

      return super.preVisitDirectory(dir, attrs);
    }

    private static boolean anyMatches(ImmutableSet<PathMatcher> matchers, Path path) {
      for (PathMatcher matcher : matchers) {
        if (matcher.matches(path)) {
          return true;
        }
      }
      return false;
    }

    private static int getFileMode(Path file) throws IOException {
      if (isPosixComplantFS()) {
        return getPosixFileMode(file);
      } else {
        return DEFAULT_FILE_MODE;
      }
    }

    private static boolean isPosixComplantFS() {
      return FileSystems.getDefault().supportedFileAttributeViews().contains(POSIX_FILE_VIEW);
    }

    private static int getPosixFileMode(Path file) throws IOException {
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

      return mode;
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
