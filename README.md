# interpolation-processor

## Java annotation processor which (partially) implements JEP 465 style string interpolation

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/${groupId}/${rootArtifactId}/badge.svg)](https://maven-badges.herokuapp.com/maven-central/${groupId}/${rootArtifactId})
[![Build Status](https://travis-ci.org/toolisticon/${rootArtifactId}.svg?branch=master)](https://travis-ci.org/toolisticon/${rootArtifactId})
[![codecov](https://codecov.io/gh/toolisticon/${rootArtifactId}/branch/master/graph/badge.svg)](https://codecov.io/gh/toolisticon/${rootArtifactId})


Maven archetype used to generate the project:

`mvn archetype:generate -DarchetypeGroupId="io.toolisticon.maven.archetypes" -DarchetypeArtifactId="annotationprocessor-archetype" -DarchetypeVersion="0.10.0" -DgroupId=pragmasoft -DartifactId=interpolation-processor -Dversion="2025.11.1" -Dpackage=interpolation -DannotationName=InterpolationMethod`


# Why you should use this project?


# Features

Annotation processor that (partially) implements JEP 465 style string interpolation

# How does it work?

Just add the interpolation-processor annotation processor dependency to your dependencies

```xml`
<dependencies>
    <!-- must be on provided scope since it is just needed at compile time -->
    <dependency>
        <groupId>pragmasoft</groupId>
        <artifactId>interpolation-processor-processor</artifactId>
        <version>2025.11.1</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
``


## Preconditions

## Example
    
# Contributing

We welcome any kind of suggestions and pull requests.

## Building and developing the ${rootArtifactId} annotation processor

The ${rootArtifactId} is built using Maven.
A simple import of the pom in your IDE should get you up and running. To build the ${rootArtifactId} on the commandline, just run `mvn` or `mvn clean install`

## Requirements

The likelihood of a pull request being used rises with the following properties:

- You have used a feature branch.
- You have included a test that demonstrates the functionality added or fixed.
- You adhered to the [code conventions](http://www.oracle.com/technetwork/java/javase/documentation/codeconvtoc-136057.html).

## Contributions


# License

This project is released under the [GPL V2.0 License](LICENSE).

This project includes and repackages the [Annotation-Processor-Toolkit](https://github.com/holisticon/annotation-processor-toolkit) released under the  [MIT License](/3rdPartyLicenses/annotation-processor-toolkit/LICENSE.txt).
