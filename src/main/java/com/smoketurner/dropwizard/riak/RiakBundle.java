/*
 * Copyright © 2019 Smoke Turner, LLC (github@smoketurner.com)
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
package com.smoketurner.dropwizard.riak;

import com.basho.riak.client.api.RiakClient;
import com.smoketurner.dropwizard.riak.health.RiakHealthCheck;
import com.smoketurner.dropwizard.riak.managed.RiakClientManager;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public abstract class RiakBundle<C extends Configuration>
    implements ConfiguredBundle<C>, RiakConfiguration<C> {

  @Override
  public void initialize(Bootstrap<?> bootstrap) {
    // nothing to initialize
  }

  @Override
  public void run(final C configuration, final Environment environment) throws Exception {
    final RiakClient client = getRiakFactory(configuration).build();
    environment.lifecycle().manage(new RiakClientManager(client));
    environment.healthChecks().register("riak", new RiakHealthCheck(client));
  }
}
