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

package com.spotify.docker.client.gcr;

import com.spotify.docker.client.gcr.GCloudProcess;
import java.io.IOException;
import org.apache.commons.io.IOUtils;

public class GoogleContainerRegistryCredRefresher {

  private final GCloudProcess gcloudProcess;

  public GoogleContainerRegistryCredRefresher(GCloudProcess gcloudProcess) {
    this.gcloudProcess = gcloudProcess;
  }

  public void refresh() throws IOException {
    Process process = gcloudProcess.runGcloudDocker();
    try {
      if (process.waitFor() != 0) {
        throw new IOException(IOUtils.toString(process.getErrorStream(), "UTF-8"));
      }
    } catch (InterruptedException ex) {
      throw new IOException(ex);
    }

  }

}
