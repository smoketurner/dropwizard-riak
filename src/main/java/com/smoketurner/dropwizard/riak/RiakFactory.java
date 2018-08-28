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
package com.smoketurner.dropwizard.riak;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.core.RiakCluster;
import com.basho.riak.client.core.RiakNode;
import com.basho.riak.client.core.util.DefaultCharset;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import com.google.common.net.HostAndPort;
import com.google.common.primitives.Ints;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.dropwizard.util.Duration;
import io.dropwizard.validation.MinDuration;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotEmpty;

public class RiakFactory {

  private final AtomicReference<RiakClient> clientRef = new AtomicReference<>();

  @NotEmpty private List<HostAndPort> nodes = Collections.emptyList();

  @Nullable private String username;

  @Nullable private String password;

  @Nullable private String keyStorePath;

  @Nullable private String keyStorePassword;

  @NotEmpty private String keyStoreType = "JKS";

  @Nullable private String trustStorePath;

  @Nullable private String trustStorePassword;

  @NotEmpty private String trustStoreType = "JKS";

  @Min(1)
  private int minConnections = 10;

  @Min(0)
  private int maxConnections = RiakNode.Builder.DEFAULT_MAX_CONNECTIONS;

  @Min(1)
  private int executionAttempts = RiakCluster.Builder.DEFAULT_EXECUTION_ATTEMPTS;

  private boolean blockOnMaxConnections = false;

  @NotNull
  @MinDuration(value = 1, unit = TimeUnit.MILLISECONDS)
  private Duration idleTimeout = Duration.milliseconds(1000);

  @NotNull
  @MinDuration(value = 0, unit = TimeUnit.MILLISECONDS)
  private Duration connectionTimeout = Duration.milliseconds(0);

  @JsonProperty
  public List<HostAndPort> getNodes() {
    return nodes;
  }

  @JsonProperty
  public void setNodes(final List<HostAndPort> nodes) {
    this.nodes = nodes;
  }

  @Nullable
  @JsonProperty
  public String getUsername() {
    return username;
  }

  @JsonProperty
  public void setUsername(@Nullable final String username) {
    this.username = username;
  }

  @Nullable
  @JsonProperty
  public String getPassword() {
    return password;
  }

  @JsonProperty
  public void setPassword(@Nullable final String password) {
    this.password = password;
  }

  @Nullable
  @JsonProperty
  public String getKeyStorePath() {
    return keyStorePath;
  }

  @JsonProperty
  public void setKeyStorePath(@Nullable final String filename) {
    this.keyStorePath = filename;
  }

  @Nullable
  @JsonProperty
  public String getKeyStorePassword() {
    return keyStorePassword;
  }

  @JsonProperty
  public void setKeyStorePassword(@Nullable final String password) {
    this.keyStorePassword = password;
  }

  @JsonProperty
  public String getKeyStoreType() {
    return keyStoreType;
  }

  @JsonProperty
  public void setKeyStoreType(final String keyStoreType) {
    this.keyStoreType = keyStoreType;
  }

  @Nullable
  @JsonProperty
  public String getTrustStorePath() {
    return trustStorePath;
  }

  @JsonProperty
  public void setTrustStorePath(@Nullable final String filename) {
    this.trustStorePath = filename;
  }

  @Nullable
  @JsonProperty
  public String getTrustStorePassword() {
    return trustStorePassword;
  }

  @JsonProperty
  public void setTrustStorePassword(@Nullable final String password) {
    this.trustStorePassword = password;
  }

  @JsonProperty
  public String getTrustStoreType() {
    return trustStoreType;
  }

  @JsonProperty
  public void setTrustStoreType(final String trustStoreType) {
    this.trustStoreType = trustStoreType;
  }

  @JsonProperty
  public int getMinConnections() {
    return minConnections;
  }

  @JsonProperty
  public void setMinConnections(final int connections) {
    this.minConnections = connections;
  }

  @JsonProperty
  public int getMaxConnections() {
    return maxConnections;
  }

  @JsonProperty
  public void setMaxConnections(final int connections) {
    this.maxConnections = connections;
  }

  @JsonProperty
  public int getExecutionAttempts() {
    return executionAttempts;
  }

  @JsonProperty
  public void setExecutionAttempts(final int attempts) {
    this.executionAttempts = attempts;
  }

  @JsonProperty
  public Duration getIdleTimeout() {
    return idleTimeout;
  }

  @JsonProperty
  public void setIdleTimeout(final Duration timeout) {
    this.idleTimeout = timeout;
  }

  @JsonProperty
  public Duration getConnectionTimeout() {
    return connectionTimeout;
  }

  @JsonProperty
  public void setConnectionTimeout(final Duration timeout) {
    this.connectionTimeout = timeout;
  }

  @JsonIgnore
  public RiakClient build() throws Exception {
    if (clientRef.get() != null) {
      return clientRef.get();
    }

    final RiakNode.Builder builder =
        new RiakNode.Builder()
            .withMinConnections(minConnections)
            .withMaxConnections(maxConnections)
            .withConnectionTimeout(Ints.checkedCast(connectionTimeout.toMilliseconds()))
            .withIdleTimeout(Ints.checkedCast(idleTimeout.toMilliseconds()))
            .withBlockOnMaxConnections(blockOnMaxConnections);

    final KeyStore keyStore = getKeyStore();
    final KeyStore trustStore = getTrustStore();

    builder.withAuth(username, password, trustStore, keyStore, keyStorePassword);

    final List<RiakNode> nodes =
        this.nodes
            .stream()
            .map(
                address ->
                    builder
                        .withRemoteAddress(address.getHost())
                        .withRemotePort(
                            address.getPortOrDefault(RiakNode.Builder.DEFAULT_REMOTE_PORT))
                        .build())
            .collect(Collectors.toList());

    DefaultCharset.set(StandardCharsets.UTF_8);

    final RiakCluster cluster =
        RiakCluster.builder(nodes).withExecutionAttempts(executionAttempts).build();

    final RiakClient client = new RiakClient(cluster);
    if (clientRef.compareAndSet(null, client)) {
      return client;
    }

    return build();
  }

  @Nullable
  private KeyStore getKeyStore() throws Exception {
    if (Strings.isNullOrEmpty(keyStorePath)) {
      return null;
    }

    final KeyStore keyStore;
    try (InputStream inputStream = new FileInputStream(keyStorePath)) {
      final CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
      final X509Certificate cert = (X509Certificate) certFactory.generateCertificate(inputStream);
      inputStream.close();

      keyStore = KeyStore.getInstance(keyStoreType);
      if (!Strings.isNullOrEmpty(keyStorePassword)) {
        keyStore.load(null, keyStorePassword.toCharArray());
      }
      keyStore.setCertificateEntry("1", cert);
    }
    return keyStore;
  }

  @Nullable
  private KeyStore getTrustStore() throws Exception {
    if (Strings.isNullOrEmpty(trustStorePath)) {
      return null;
    }

    final KeyStore trustStore;
    try (InputStream inputStream = new FileInputStream(trustStorePath)) {
      final CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
      final X509Certificate caCert = (X509Certificate) certFactory.generateCertificate(inputStream);
      inputStream.close();

      trustStore = KeyStore.getInstance(trustStoreType);
      if (!Strings.isNullOrEmpty(trustStorePassword)) {
        trustStore.load(null, trustStorePassword.toCharArray());
      }
      trustStore.setCertificateEntry("cacert", caCert);
    }
    return trustStore;
  }
}
