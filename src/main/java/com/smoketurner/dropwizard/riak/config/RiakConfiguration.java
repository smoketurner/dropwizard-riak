/**
 * Copyright 2016 Smoke Turner, LLC.
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
package com.smoketurner.dropwizard.riak.config;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotEmpty;
import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.core.RiakCluster;
import com.basho.riak.client.core.RiakNode;
import com.basho.riak.client.core.util.DefaultCharset;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import com.google.common.net.HostAndPort;
import com.google.common.primitives.Ints;
import com.smoketurner.dropwizard.riak.health.RiakHealthCheck;
import com.smoketurner.dropwizard.riak.managed.RiakClusterManager;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import io.dropwizard.validation.MinDuration;
import io.dropwizard.validation.ValidationMethod;

public class RiakConfiguration {

    @NotEmpty
    private List<HostAndPort> nodes = Collections.emptyList();

    private String username;

    private String password;

    private String keyStorePath;

    private String keyStorePassword;

    @NotEmpty
    private String keyStoreType = "JKS";

    private String trustStorePath;

    private String trustStorePassword;

    @NotEmpty
    private String trustStoreType = "JKS";

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

    @JsonProperty
    public String getUsername() {
        return username;
    }

    @JsonProperty
    public void setUsername(final String username) {
        this.username = username;
    }

    @JsonProperty
    public String getPassword() {
        return password;
    }

    @JsonProperty
    public void setPassword(final String password) {
        this.password = password;
    }

    @JsonProperty
    public String getKeyStorePath() {
        return keyStorePath;
    }

    @JsonProperty
    public void setKeyStorePath(final String filename) {
        this.keyStorePath = filename;
    }

    @JsonProperty
    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    @JsonProperty
    public void setKeyStorePassword(final String password) {
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

    @JsonProperty
    public String getTrustStorePath() {
        return trustStorePath;
    }

    @JsonProperty
    public void setTrustStorePath(final String filename) {
        this.trustStorePath = filename;
    }

    @JsonProperty
    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    @JsonProperty
    public void setTrustStorePassword(final String password) {
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

    @ValidationMethod(message = "keyStorePath should not be null")
    public boolean isValidKeyStorePath() {
        return keyStoreType.startsWith("Windows-") || keyStorePath != null;
    }

    @ValidationMethod(message = "keyStorePassword should not be null or empty")
    public boolean isValidKeyStorePassword() {
        return keyStoreType.startsWith("Windows-") || !Strings.isNullOrEmpty(keyStorePassword);
    }

    @JsonIgnore
    public RiakClient build(@Nonnull final Environment environment) throws Exception {
        Objects.requireNonNull(environment);

        final RiakNode.Builder builder = new RiakNode.Builder().withMinConnections(minConnections)
                .withMaxConnections(maxConnections)
                .withConnectionTimeout(Ints.checkedCast(connectionTimeout.toMilliseconds()))
                .withIdleTimeout(Ints.checkedCast(idleTimeout.toMilliseconds()))
                .withBlockOnMaxConnections(blockOnMaxConnections);

        final KeyStore keyStore = getKeyStore();
        final KeyStore trustStore = getTrustStore();

        builder.withAuth(username, password, trustStore, keyStore, keyStorePassword);

        final List<RiakNode> nodes = new ArrayList<>();
        for (HostAndPort address : this.nodes) {
            final RiakNode node = builder.withRemoteAddress(address.getHostText())
                    .withRemotePort(address.getPortOrDefault(RiakNode.Builder.DEFAULT_REMOTE_PORT)).build();
            nodes.add(node);
        }

        DefaultCharset.set(StandardCharsets.UTF_8);

        final RiakCluster cluster = RiakCluster.builder(nodes).withExecutionAttempts(executionAttempts).build();
        environment.lifecycle().manage(new RiakClusterManager(cluster));

        final RiakClient client = new RiakClient(cluster);
        environment.healthChecks().register("riak", new RiakHealthCheck(client));
        return client;
    }

    private KeyStore getKeyStore() throws Exception {
        final KeyStore keyStore;
        if (!Strings.isNullOrEmpty(keyStorePath)) {
            try (InputStream inputStream = new FileInputStream(keyStorePath)) {
                final CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                final X509Certificate cert = (X509Certificate) certFactory.generateCertificate(inputStream);

                keyStore = KeyStore.getInstance(keyStoreType);
                if (!Strings.isNullOrEmpty(keyStorePassword)) {
                    keyStore.load(null, keyStorePassword.toCharArray());
                }
                keyStore.setCertificateEntry("1", cert);
            }
        } else {
            keyStore = null;
        }
        return keyStore;
    }

    private KeyStore getTrustStore() throws Exception {
        KeyStore trustStore;
        if (!Strings.isNullOrEmpty(trustStorePath)) {
            try (InputStream inputStream = new FileInputStream(trustStorePath)) {
                final CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                final X509Certificate caCert = (X509Certificate) certFactory.generateCertificate(inputStream);

                trustStore = KeyStore.getInstance(trustStoreType);
                if (!Strings.isNullOrEmpty(trustStorePassword)) {
                    trustStore.load(null, trustStorePassword.toCharArray());
                }
                trustStore.setCertificateEntry("cacert", caCert);
            }
        } else {
            trustStore = null;
        }
        return trustStore;
    }
}
