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

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.regex.PatternSyntaxException;

import static org.hamcrest.Matchers.describedAs;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class CompressedDirectoryMatchFilepathTest {

  @Parameters(name = "Pattern {0} matching {1}: {2} throwing {3}")
  public static Iterable<Object[]> data() {

    // Data copy-pasted from http://golang.org/src/path/filepath/match_test.go#L22
    // Patterns we don't correctly handle are commented out
    return Arrays.asList(new Object[][] {
        {"abc", "abc", true, null},
        {"*", "abc", true, null},
        {"*c", "abc", true, null},
        {"a*", "a", true, null},
        {"a*", "abc", true, null},
        {"a*", "ab/c", false, null},
        {"a*/b", "abc/b", true, null},
        {"a*/b", "a/c/b", false, null},
        {"a*b*c*d*e*/f", "axbxcxdxe/f", true, null},
        {"a*b*c*d*e*/f", "axbxcxdxexxx/f", true, null},
        {"a*b*c*d*e*/f", "axbxcxdxe/xxx/f", false, null},
        {"a*b*c*d*e*/f", "axbxcxdxexxx/fff", false, null},
        {"a*b?c*x", "abxbbxdbxebxczzx", true, null},
        {"a*b?c*x", "abxbbxdbxebxczzy", false, null},
        {"ab[c]", "abc", true, null},
        {"ab[b-d]", "abc", true, null},
        {"ab[e-g]", "abc", false, null},
        {"ab[^c]", "abc", false, null},
        {"ab[^b-d]", "abc", false, null},
        {"ab[^e-g]", "abc", true, null},
        {"a\\*b", "a*b", true, null},
        {"a\\*b", "ab", false, null},
        {"a?b", "a☺b", true, null},
        {"a[^a]b", "a☺b", true, null},
        {"a???b", "a☺b", false, null},
        {"a[^a][^a][^a]b", "a☺b", false, null},
        {"[a-ζ]*", "α", true, null},
        {"*[a-ζ]", "A", false, null},
        {"a?b", "a/b", false, null},
        {"a*b", "a/b", false, null},
        {"[\\]a]", "]", true, null},
        {"[\\-]", "-", true, null},
        {"[x\\-]", "x", true, null},
        {"[x\\-]", "-", true, null},
        {"[x\\-]", "z", false, null},
        {"[\\-x]", "x", true, null},
        {"[\\-x]", "-", true, null},
        {"[\\-x]", "a", false, null},
        {"[]a]", "]", false, PatternSyntaxException.class},
        // {"[-]", "-", false, PatternSyntaxException.class},
        // {"[x-]", "x", false, PatternSyntaxException.class},
        // {"[x-]", "-", false, PatternSyntaxException.class},
        // {"[x-]", "z", false, PatternSyntaxException.class},
        // {"[-x]", "x", false, PatternSyntaxException.class},
        // {"[-x]", "-", false, PatternSyntaxException.class},
        // {"[-x]", "a", false, PatternSyntaxException.class},
        // {"\\", "a", false, PatternSyntaxException.class},
        // {"[a-b-c]", "a", false, PatternSyntaxException.class},
        {"[", "a", false, PatternSyntaxException.class},
        {"[^", "a", false, PatternSyntaxException.class},
        {"[^bc", "a", false, PatternSyntaxException.class},
        // {"a[", "a", false, null},
        {"a[", "ab", false, PatternSyntaxException.class},
        {"*x", "xxx", true, null},
        });
  }

  @Rule
  public final ExpectedException expectedException = ExpectedException.none();

  @Parameter(0)
  public String pattern;

  @Parameter(1)
  public String pathString;

  @Parameter(2)
  public boolean matched;

  @Parameter(3)
  public Class<? extends Exception> exception;

  private FileSystem fs;

  @Before
  public void setUp() throws Exception {
    fs = Jimfs.newFileSystem(Configuration.unix());
  }

  @Test
  public void testMatchFilepath() {
    if (exception != null) {
      expectedException.expect(exception);
    }

    final Path path = fs.getPath(pathString);
    final boolean result = CompressedDirectory.goPathMatcher(fs, pattern).matches(path);

    final String description;
    if (matched) {
      description = MessageFormat.format("the pattern {0} to match {1}", pattern, pathString);
    } else {
      description = MessageFormat.format("the pattern {0} not to match {1}", pattern, pathString);
    }

    assertThat(result, describedAs(description, is(matched)));
  }

}
