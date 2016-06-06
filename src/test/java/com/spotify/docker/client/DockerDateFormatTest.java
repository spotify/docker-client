/*
 * Copyright (c) 2016
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

import com.fasterxml.jackson.databind.util.StdDateFormat;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class DockerDateFormatTest {

  private final DockerDateFormat dockerDateFormat = new DockerDateFormat();
  private final String millisecondDateString = "2015-09-18T17:44:28.145Z";
  private Date expected;

  @Before
  public void setUp() throws Exception {
    expected = new StdDateFormat().parse(millisecondDateString);
  }

  /**
   * Verify DockerDateFormat handles millisecond precision correctly
   */
  @Test
  public void testHandlesMillisecondPrecision() throws Exception {
    assertThat(dockerDateFormat.parse(millisecondDateString), equalTo(expected));
  }

  /**
   * Verify DockerDateFormat converts nanosecond precision down to millisecond precision
   */
  @Test
  public void testHandlesNanosecondPrecision() throws Exception {
    assertThat(dockerDateFormat.parse("2015-09-18T17:44:28.145855389Z"), equalTo(expected));
  }

  /**
   * Verify DockerDateFormat converts nanosecond precision with less than nine digits
   * down to millisecond precision
   */
  @Test
  public void testHandlesNanosecondWithLessThanNineDigits() throws Exception {
    assertThat(dockerDateFormat.parse("2015-09-18T17:44:28.1458553Z"), equalTo(expected));
  }

  @Test
  public void otherTimeZones() throws Exception {
    final Date expected =
        new DateTime(2016, 6, 3, 6, 57, 17, 478, DateTimeZone.forOffsetHours(-4)).toDate();
    assertThat(dockerDateFormat.parse("2016-06-03T06:57:17.4782869-04:00"), equalTo(expected));
  }
}
