Dropwizard Riak
===============
[![Build Status](https://travis-ci.org/smoketurner/dropwizard-riak.svg?branch=master)](https://travis-ci.org/smoketurner/dropwizard-riak)
[![Coverage Status](https://coveralls.io/repos/smoketurner/dropwizard-riak/badge.svg?branch=master)](https://coveralls.io/r/smoketurner/dropwizard-riak?branch=master)
[![Maven Central](https://img.shields.io/maven-central/v/com.smoketurner.dropwizard/dropwizard-riak.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/com.smoketurner.dropwizard/dropwizard-riak/)
[![GitHub license](https://img.shields.io/github/license/smoketurner/dropwizard-riak.svg?style=flat-square)](https://github.com/smoketurner/dropwizard-riak/tree/master)

`dropwizard-riak` is a [Dropwizard](http://dropwizard.io) bundle for interacting with [Riak](http://www.basho.com/riak) using the [riak-java-client](https://github.com/basho/riak-java-client).

Usage
-----

Within your `Configuration` class, add the following:

```java
@Valid
@NotNull
@JsonProperty
private final RiakConfiguration riak = new RiakConfiguration();

@JsonProperty
public RiakConfiguration getRiak() {
    return riak;
}
```

Then with your `Application` class' `run()` method, you can access a `RiakClient` by doing the following:

```java
@Override
public void run(MyConfiguration configuration, Environment environment) throws Exception {
    RiakConfiguration riakConfig = configuration.getRiak();
    RiakClient client = riakConfig.build(environment);
}
```

Maven Artifacts
---------------

This project is available on Maven Central. To add it to your project simply add the following dependencies to your `pom.xml`:

```xml
<dependency>
    <groupId>com.smoketurner.dropwizard</groupId>
    <artifactId>dropwizard-riak</artifactId>
    <version>1.0.5-3</version>
</dependency>
```

Support
-------

Please file bug reports and feature requests in [GitHub issues](https://github.com/smoketurner/dropwizard-riak/issues).


License
-------

Copyright (c) 2017 Smoke Turner, LLC

This library is licensed under the Apache License, Version 2.0.

See http://www.apache.org/licenses/LICENSE-2.0.html or the LICENSE file in this repository for the full license text.
