/*
 * Copyright © 2018 Smoke Turner, LLC (github@smoketurner.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.smoketurner.dropwizard.riak.managed;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.core.RiakCluster;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

public class RiakClientManagerTest {

  private final RiakClient client = mock(RiakClient.class);
  private final RiakCluster cluster = mock(RiakCluster.class);
  private final RiakClientManager manager = new RiakClientManager(client);

  @Before
  public void setUp() {
    when(client.getRiakCluster()).thenReturn(cluster);
  }

  @Test
  public void testStart() throws Exception {
    manager.start();
    verify(client.getRiakCluster()).start();
  }

  @Test
  public void testStop() throws Exception {
    when(client.shutdown())
        .thenReturn(
            new Future<Boolean>() {

              @Override
              public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
              }

              @Override
              public boolean isCancelled() {
                return false;
              }

              @Override
              public boolean isDone() {
                return true;
              }

              @Override
              public Boolean get() throws InterruptedException, ExecutionException {
                return true;
              }

              @Override
              public Boolean get(long timeout, TimeUnit unit)
                  throws InterruptedException, ExecutionException, TimeoutException {
                return true;
              }
            });

    manager.stop();
    final InOrder inOrder = inOrder(client);
    inOrder.verify(client).shutdown();
    inOrder.verify(client).cleanup();
  }
}
