/*-
 * -\-\-
 * docker-client
 * --
 * Copyright (C) 2016 Spotify AB
 * Copyright (c) 2015 Jeppe Schou
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

public final class VersionCompare {

  private VersionCompare() {
  }

  /**
   * Compares two version strings.
   *
   * <p>https://stackoverflow.com/questions/6701948/efficient-way-to-compare-version-strings-in-java
   *
   * <p>Use this instead of String.compareTo() for a non-lexicographical comparison that works for
   * version strings. e.g. "1.10".compareTo("1.6").
   *
   * @param str1 a string of ordinal numbers separated by decimal points.
   * @param str2 a string of ordinal numbers separated by decimal points.
   * @return The result is a negative integer if str1 is _numerically_ less than str2. The result is
   *         a positive integer if str1 is _numerically_ greater than str2. The result is zero if
   *         the strings are _numerically_ equal. N.B. It does not work if "1.10" is supposed to be
   *         equal to "1.10.0".
   */
  public static int compareVersion(final String str1, final String str2) {
    final String[] vals1 = str1.split("\\.");
    final String[] vals2 = str2.split("\\.");
    int idx = 0;
    // set index to first non-equal ordinal or length of shortest version string
    while (idx < vals1.length && idx < vals2.length && vals1[idx].equals(vals2[idx])) {
      idx++;
    }
    // compare first non-equal ordinal number
    if (idx < vals1.length && idx < vals2.length) {
      final int diff = Integer.valueOf(vals1[idx]).compareTo(Integer.valueOf(vals2[idx]));
      return Integer.signum(diff);
    } else {
      // the strings are equal or one string is a substring of the other
      // e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
      return Integer.signum(vals1.length - vals2.length);
    }
  }
}
