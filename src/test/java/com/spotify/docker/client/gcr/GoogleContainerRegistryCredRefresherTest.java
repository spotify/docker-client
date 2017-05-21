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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.spotify.docker.client.gcr.GCloudProcess;
import com.spotify.docker.client.gcr.GoogleContainerRegistryCredRefresher;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class GoogleContainerRegistryCredRefresherTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();


  @Test
  public void refreshShellsOutToGCloudCli() throws IOException, InterruptedException {
    Process process = mock(Process.class);
    when(process.waitFor()).thenReturn(0);

    GCloudProcess gcloudProcess = mock(GCloudProcess.class);
    when(gcloudProcess.runGcloudDocker()).thenReturn(process);

    GoogleContainerRegistryCredRefresher googleContainerRegistryCredRefresher =
        new GoogleContainerRegistryCredRefresher(gcloudProcess);

    googleContainerRegistryCredRefresher.refresh();
    verify(gcloudProcess).runGcloudDocker();

  }

  @Test
  public void refreshThrowsIfSuccessCodeIsntReturnedFromCommand()
      throws InterruptedException, IOException {
    thrown.expect(IOException.class);
    thrown.expectMessage("ERROR: (gcloud.docker)");

    GCloudProcess gcloudProcess = mock(GCloudProcess.class);

    Process failedProc = mock(Process.class);
    when(failedProc.waitFor()).thenReturn(1);
    when(failedProc.getErrorStream())
        .thenReturn(
            new ByteArrayInputStream("ERROR: (gcloud.docker)".getBytes(StandardCharsets.UTF_8)));

    when(gcloudProcess.runGcloudDocker()).thenReturn(failedProc);

    GoogleContainerRegistryCredRefresher googleContainerRegistryCredRefresher =
        new GoogleContainerRegistryCredRefresher(gcloudProcess);

    googleContainerRegistryCredRefresher.refresh();;
  }

}
