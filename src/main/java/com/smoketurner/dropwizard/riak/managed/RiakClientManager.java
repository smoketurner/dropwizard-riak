/**
 * Copyright 2017 Smoke Turner, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.smoketurner.dropwizard.riak.managed;

import java.util.Objects;
import javax.annotation.Nonnull;
import com.basho.riak.client.api.RiakClient;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.util.Duration;

public class RiakClientManager implements Managed {

    private static final Duration timeout = Duration.seconds(5);
    private final RiakClient client;

    /**
     * Constructor
     *
     * @param client
     *            Riak client instance to manage
     */
    public RiakClientManager(@Nonnull final RiakClient client) {
        this.client = Objects.requireNonNull(client);
    }

    @Override
    public void start() throws Exception {
        client.getRiakCluster().start();
    }

    @Override
    public void stop() throws Exception {
        client.shutdown().get(timeout.getQuantity(), timeout.getUnit());
        client.cleanup();
    }
}
