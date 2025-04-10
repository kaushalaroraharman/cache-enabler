[<img src="./images/logo.png" width="400" height="200"/>](./images/logo.png)

# Cache-Enabler
[![Build And Sonar scan](https://github.com/eclipse-ecsp/cache-enabler/actions/workflows/maven-build.yml/badge.svg)](https://github.com/eclipse-ecsp/cache-enabler/actions/workflows/maven-build.yml)
[![License Compliance](https://github.com/eclipse-ecsp/cache-enabler/actions/workflows/license-compliance.yml/badge.svg)](https://github.com/eclipse-ecsp/cache-enabler/actions/workflows/license-compliance.yml)
[![Deployment](https://github.com/eclipse-ecsp/cache-enabler/actions/workflows/maven-deploy.yml/badge.svg)](https://github.com/eclipse-ecsp/cache-enabler/actions/workflows/maven-deploy.yml)

The `cache-enabler` project provides Redis Server-backed caching capabilities to the services. It uses rich API objects support provided by a Redisson library and implements the execution of both synchronous and asynchronous commands on the Redis Server. Synchronous methods bear asynchronous variants.

Following are the supported Redis connection configurations:

1.  [Single node (default)](https://github.com/redisson/redisson/wiki/2.-Configuration#26-single-instance-mode)
2.  [Clustered nodes](https://github.com/redisson/redisson/wiki/2.-Configuration#24-cluster-mode)
3.  [Sentinel nodes](https://github.com/redisson/redisson/wiki/2.-Configuration#27-sentinel-mode)

`cache-enabler` supports CRUD operations into the Redis server for these objects:

- Any entity type object along with the capability to add a score.
- Any `String` object along with its score.

`cache-enabler` also provides the capability to perform CRUD operations on a Map of entities.

<b>Each object has a `mutationId` which is meant to be returned in asynchronous operations</b>

> **_NOTE:_** Support for pipelined batch executions are provided through the .*Async() methods. Clients of `redis-cache` are encouraged to use Async methods to improve throughput. For typical store and forget or store and retrieve slightly later use cases, Async methods will bring about quite a bit of throughput improvement.

# Table of Contents
* [Getting Started](#getting-started)
* [Usage](#usage)
* [How to contribute](#how-to-contribute)
* [Built with Dependencies](#built-with-dependencies)
* [Code of Conduct](#code-of-conduct)
* [Authors](#authors)
* [Security Contact Information](#security-contact-information)
* [Support](#support)
* [Troubleshooting](#troubleshooting)
* [License](#license)
* [Announcements](#announcements)


## Getting Started

To build the project in the local working directory after the project has been cloned/forked, run:

```mvn clean install```

from the command line interface.

### Prerequisites

1. Maven
2. Java 17
3. Copy pre-compiled binary of redis server and pdb into src/test/resources

### Installation

[How to set up maven](https://maven.apache.org/install.html)

[Install Java](https://stackoverflow.com/questions/52511778/how-to-install-openjdk-11-on-windows)

[Download pre-compiled Redis](https://<s3-link>)

### Running the tests

```mvn test```

Or run a specific test

```mvn test -Dtest="TheFirstUnitTest"```

To run a method from within a test

```mvn test -Dtest="TheSecondUnitTest#whenTestCase2_thenPrintTest2_1"```

### Deployment

The `redis-cache` project serves as a library for the services. It is not meant to be deployed as a service in a Cloud environment.

## Usage

Add the following dependency in the target project
```
<dependency>
  <groupId>org.eclipse.ecsp</groupId>
  <artifactId>cache-enabler</artifactId>
  <version>1.X.X</version>
</dependency>

```

### Cache Operations

|            Operation            | Parameter Required |        Parameter Type        |                                                                         Parameter Description                                                                         |         Output Type         |
|:-------------------------------:|:------------------:|:----------------------------:|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------:|:---------------------------:|
|            getString            |        key         |           `String`           |                                                                   Key in redis for a `String` value                                                                   |          `String`           |
|            getString            |      request       |     ` GetStringRequest`      |                                                      Object with key and boolean parameter for namespace enabled                                                      |          `String`           |
|            putString            |     putRequest     |      `PutStringRequest`      |                                          Object with new value for a `String` key in redis and other additional information                                           |           `void`            |
|            getEntity            |        key         |           `String`           |                                                                  Key in redis for an `IgniteEntity`                                                                   |       `IgniteEntity`        |
|            getEntity            |      request       |      `GetEntityRequest`      |                                                      Object with key and boolean parameter for namespace enabled                                                      |       `IgniteEntity`        |
|            putEntity            |     putRequest     |      `PutEntityRequest`      |                                   Object representation with `IgniteEntity` as value to put into cache along with other information                                   |           `void`            |
|   addStringToScoredSortedSet    |      request       |   `AddScoredStringRequest`   |                                             Object with score, key and value for adding a `String` to a scored sorted set                                             |           `void`            |
|  getStringsFromScoredSortedSet  |      request       |  `GetScoredStringsRequest`   |            Object with `key`, `start`, `end`, `namespaceEnabled` and `reversed` boolean parameter for getting a range of `String` from a scored sorted set            |       `List<String>`        |
| getEntitiesFromScoredSortedSet  |      request       |  `GetScoredEntitiesRequest`  |         Object with `key`, `start`, `end`, `namespaceEnabled` and `reversed` boolean parameter for getting a range of `IgniteEntity` from a scored sorted set         |    `List<IgniteEntity>`     |
|   addEntityToScoredSortedSet    |      request       |   `AddScoredEntityRequest`   |            Object with `key`, `entity`, `score`, `mutation Id` and boolean parameter namespace enabled for adding an `IgniteEntity` to a scored sorted set            |           `void`            |
|         putStringAsync          |     putRequest     |      `PutStringRequest`      |                                     Object with value and additional information to put a `String` in redis in asynchronous mode                                      |           `void`            |
|         putEntityAsync          |     putRequest     |      `PutEntityRequest`      |                        Object representation with `IgniteEntity` as value to put into cache in asynchronous mode  along with other information                        |           `void`            |
| addStringToScoredSortedSetAsync |      request       |   `AddScoredStringRequest`   |     Object with `key`, `value`, `score`, `mutation Id` and boolean parameter `namespaceEnabled` for adding a `String` to a scored sorted set in asynchronous mode     |           `void`            |
| addEntityToScoredSortedSetAsync |      request       |   `AddScoredEntityRequest`   | Object with `key`, `entity`, `score`, `mutation Id` and boolean parameter `namespaceEnabled` for adding an `IgniteEntity` to a scored sorted set in asynchronous mode |           `void`            |
|             delete              |        key         |           `String`           |                                                                        Key in redis to delete                                                                         |           `void`            |
|             delete              |      request       |     `DeleteEntryRequest`     |                                              Object with key and boolean parameter for namespace enabled to delete a key                                              |           `void`            |
|           deleteAsync           |      request       |     `DeleteEntryRequest`     |                                   Object with key and boolean parameter for namespace enabled to delete a key in asynchronous mode                                    |           `void`            |
|        putMapOfEntities         |     mapRequest     |  `PutMapOfEntitiesRequest`   |                                                    Object for putting a map of `IgniteEntity` into cache for a key                                                    |           `void`            |
|        getMapOfEntities         |     mapRequest     |  `GetMapOfEntitiesRequest`   |                            Object for getting a map of specific `IgniteEntity` specified by `fields` in the object for a key in the cache                             | `Map<String, IgniteEntity>` |
|       deleteMapOfEntities       |      request       | `DeleteMapOfEntitiesRequest` |                                Object for deleting a map of `IgniteEntity` specified by `fields` in the object for a key in the cache                                 |           `void`            |





### Redis connection configuration

All the redis related configuration needs to be specified on the environment, which can be referred [here](./src/test/resources/ignite-cache.properties)

Some configuration which decide the kind of connection to be made: 

1.  For running the library as a client of Redis with sentinels, the services need to set the below property:
   `redis.sentinels=<redis-sentinel-list>`

2. For running the library as a client of Redis clustered nodes, the services need to set the below property:
   `redis.cluster.masters=<xxxxx>`

3. If the above two configurations are not specified, the default configuration will be applied to run the application as a client for a single Redis server instance, the host of which is specified by:
   `redis.address=<xxxxxxx>`

### Health Check

`IgniteCacheRedisImpl` also serves as a health monitor for health monitoring provided by the `ignite-utils` dependency. 
If there are any exceptions raised in performing any operation with Redis, the health monitor is marked as Unhealthy.

### Batch Operations

`cache-enabler` provides the capability to execute batch operations with Redis in a reliable way. If a thread was performing a batch operation and another thread performed RBatch.execute() at the same time,
then first thread fails with IllegalStateException("Batch already has been executed"). 

## Built With Dependencies

|                              Dependency                              | Purpose                                                       |
|:--------------------------------------------------------------------:|:--------------------------------------------------------------|
|        [Ignite Utils](https://github.com/eclipse-ecsp/utils)         | Provides logging support and health monitor for cache service |
| [Ignite Transformers](https://github.com/eclipse-ecsp/transformers)  | Provides serialization and deserialization                    |
|            [Test Containers](https://testcontainers.com/)            | Test Container support for testing in dockerized environment  |
|      [Docker Java](https://github.com/docker-java/docker-java)       | Java API client for docker                                    |
|                  [Redisson](https://redisson.org/)                   | Redis Java Client with features of In-Memory Data Grid        |
|                  [Junit](https://junit.org/junit5/)                  | Testing framework                                             |
|                 [Mockito](https://site.mockito.org/)                 | Test Mocking framework                                        |
|      [Embedded Redis](https://github.com/kstyrc/embedded-redis)      | Redis embedded server for Java integration testing            |
|     [Commons IO](https://commons.apache.org/proper/commons-io/)      | Library to assist with IO functionality                       |

## How to contribute

Please read [CONTRIBUTING.md](./CONTRIBUTING.md) for details on our contribution guidelines, and the process for submitting pull requests to us.

## Code of Conduct

Please read [CODE_OF_CONDUCT.md](./CODE_OF_CONDUCT.md) for details on our code of conduct.

## Authors

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tbody>
    <tr>
	  <td align="center" valign="top" width="14.28%"><a href="https://github.com/kaushalaroraharman"><img src="https://github.com/kaushalaroraharman.png" width="100px;" alt="Kaushal Arora"/><br /><sub><b>Kaushal Arora</b></sub></a><br /><a href="https://github.com/all-contributors/all-contributors/commits?author=kaushalaroraharman" title="Code and Documentation">📖</a> <a href="https://github.com/all-contributors/all-contributors/pulls?q=is%3Apr+reviewed-by%3Akaushalaroraharman" title="Reviewed Pull Requests">👀</a></td>
    </tr>
  </tbody>
</table>

See also the list of [contributors](https://github.com/eclipse-ecsp/nosql-dao/graphs/contributors) who participated in this project.

## Security Contact Information

Please read [SECURITY.md](./SECURITY.md) to raise any security related issues.

## Support

Please write to us at [csp@harman.com](mailto:csp@harman.com)

## Troubleshooting

Please read [CONTRIBUTING.md](./CONTRIBUTING.md) for details on how to raise an issue and submit a pull request to us.

## License

This project is licensed under the Apache-2.0 License - see the [LICENSE](./LICENSE) file for details.

## Announcements
All updates to this library are documented in our [Release Notes](./release_notes.txt) and [releases](https://github.com/eclipse-ecsp/cache-enabler/releases).
For the versions available, see the [tags on this repository](https://github.com/eclipse-ecsp/cache-enabler/tags).
