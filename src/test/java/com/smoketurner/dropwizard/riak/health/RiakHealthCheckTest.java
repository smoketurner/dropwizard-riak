/*
 * Copyright © 2018 Smoke Turner, LLC (contact@smoketurner.com)
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
package com.smoketurner.dropwizard.riak.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.core.RiakCluster;
import com.basho.riak.client.core.operations.PingOperation;
import com.codahale.metrics.health.HealthCheck.Result;
import io.dropwizard.util.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class RiakHealthCheckTest {

  private final RiakClient client = mock(RiakClient.class);
  private final RiakCluster cluster = mock(RiakCluster.class);
  private RiakHealthCheck check;

  @Before
  public void setUp() throws Exception {
    when(client.getRiakCluster()).thenReturn(cluster);
    check = new RiakHealthCheck(client, Duration.milliseconds(1));
  }

  @After
  public void tearDown() {
    reset(cluster);
  }

  @Test
  @Ignore
  public void testCheckHealthy() throws Exception {
    final Result actual = check.check();
    verify(cluster).execute(any(PingOperation.class));
    assertThat(actual.isHealthy()).isTrue();
  }

  @Test
  public void testCheckUnhealthy() throws Exception {
    final Result actual = check.check();
    verify(cluster).execute(any(PingOperation.class));
    assertThat(actual.isHealthy()).isFalse();
  }
}
