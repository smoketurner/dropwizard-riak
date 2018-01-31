Dropwizard Riak
===============
[![Build Status](https://travis-ci.org/smoketurner/dropwizard-riak.svg?branch=master)](https://travis-ci.org/smoketurner/dropwizard-riak)
[![Coverage Status](https://coveralls.io/repos/smoketurner/dropwizard-riak/badge.svg?branch=master)](https://coveralls.io/r/smoketurner/dropwizard-riak?branch=master)
[![Maven Central](https://img.shields.io/maven-central/v/com.smoketurner.dropwizard/dropwizard-riak.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/com.smoketurner.dropwizard/dropwizard-riak/)
[![GitHub license](https://img.shields.io/github/license/smoketurner/dropwizard-riak.svg?style=flat-square)](https://github.com/smoketurner/dropwizard-riak/tree/master)
[![Become a Patron](https://img.shields.io/badge/Patron-Patreon-red.svg)](https://www.patreon.com/bePatron?u=9567343)

A bundle for accessing [Riak](http://basho.com/products/riak-kv/) in Dropwizard applications using [riak-java-client](https://github.com/basho/riak-java-client).

Usage
-----

Within your `Configuration` class, add the following:

```java
@Valid
@NotNull
private final RiakFactory riak = new RiakFactory();

@JsonProperty
public RiakFactory getRiakFactory() {
    return riak;
}
```

Then with your `Application` class, you can access a `RiakClient` by doing the following:

```java
@Override
public void initialize(Bootstrap<MyConfiguration> bootstrap) {
    bootstrap.addBundle(new RiakBundle<MyConfiguration>() {
        @Override
        public RiakFactory getRiakFactory(MyConfiguration configuration) {
            return configuration.getRiakFactory();
        }
    });
}

@Override
public void run(MyConfiguration configuration, Environment environment) throws Exception {
    RiakClient client = configuration.getRiakFactory().build();
}
```

Maven Artifacts
---------------

This project is available on Maven Central. To add it to your project simply add the following dependencies to your `pom.xml`:

```xml
<dependency>
    <groupId>com.smoketurner.dropwizard</groupId>
    <artifactId>dropwizard-riak</artifactId>
    <version>1.2.3-2</version>
</dependency>
```

Support
-------

Please file bug reports and feature requests in [GitHub issues](https://github.com/smoketurner/dropwizard-riak/issues).


License
-------

Copyright (c) 2018 Smoke Turner, LLC

This library is licensed under the Apache License, Version 2.0.

See http://www.apache.org/licenses/LICENSE-2.0.html or the LICENSE file in this repository for the full license text.
