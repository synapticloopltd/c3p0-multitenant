

> **This project requires JVM version of at least 1.7**


[![Download](https://api.bintray.com/packages/synapticloop/maven/c3p0-multitenant/images/download.svg)](https://bintray.com/synapticloop/maven/c3p0-multitenant/_latestVersion) [![GitHub Release](https://img.shields.io/github/release/synapticloop/c3p0-multitenant.svg)](https://github.com/synapticloop/c3p0-multitenant/releases) 

# c3p0-multitenant



> Multi tenanted c3p0 pool




# Building the Package

## *NIX/Mac OS X

From the root of the project, simply run

`./gradlew build`


## Windows

`./gradlew.bat build`


This will compile and assemble the artefacts into the `build/libs/` directory.

Note that this may also run tests (if applicable see the Testing notes)

# Running the Tests

## *NIX/Mac OS X

From the root of the project, simply run

`gradle --info test`

if you do not have gradle installed, try:

`gradlew --info test`

## Windows

From the root of the project, simply run

`gradle --info test`

if you do not have gradle installed, try:

`./gradlew.bat --info test`


The `--info` switch will also output logging for the tests

# Artefact Publishing - Github

This project publishes artefacts to [GitHib](https://github.com/)

> Note that the latest version can be found [https://github.com/synapticloopltd/c3p0-multitenant/releases](https://github.com/synapticloop/c3p0-multitenant/releases)

As such, this is not a repository, but a location to download files from.

# Artefact Publishing - Bintray

This project publishes artefacts to [bintray](https://bintray.com/)

> Note that the latest version can be found [https://bintray.com/synapticloop/maven/c3p0-multitenant/view](https://bintray.com/synapticloop/maven/c3p0-multitenant/view)

## maven setup

this comes from the jcenter bintray, to set up your repository:

```
<?xml version="1.0" encoding="UTF-8" ?>
<settings xsi:schemaLocation='http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd' xmlns='http://maven.apache.org/SETTINGS/1.0.0' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>
  <profiles>
    <profile>
      <repositories>
        <repository>
          <snapshots>
            <enabled>false</enabled>
          </snapshots>
          <id>central</id>
          <name>bintray</name>
          <url>http://jcenter.bintray.com</url>
        </repository>
      </repositories>
      <pluginRepositories>
        <pluginRepository>
          <snapshots>
            <enabled>false</enabled>
          </snapshots>
          <id>central</id>
          <name>bintray-plugins</name>
          <url>http://jcenter.bintray.com</url>
        </pluginRepository>
      </pluginRepositories>
      <id>bintray</id>
    </profile>
  </profiles>
  <activeProfiles>
    <activeProfile>bintray</activeProfile>
  </activeProfiles>
</settings>
```

## gradle setup

Repository

```
repositories {
	maven {
		url  "http://jcenter.bintray.com" 
	}
}
```

or just

```
repositories {
	jcenter()
}
```

## Dependencies - Gradle

```
dependencies {
	runtime(group: 'synapticloop', name: 'c3p0-multitenant', version: '1.0.0', ext: 'jar')

	compile(group: 'synapticloop', name: 'c3p0-multitenant', version: '1.0.0', ext: 'jar')
}
```

or, more simply for versions of gradle greater than 2.1

```
dependencies {
	runtime 'synapticloop:c3p0-multitenant:1.0.0'

	compile 'synapticloop:c3p0-multitenant:1.0.0'
}
```

## Dependencies - Maven

```
<dependency>
	<groupId>synapticloop</groupId>
	<artifactId>c3p0-multitenant</artifactId>
	<version>1.0.0</version>
	<type>jar</type>
</dependency>
```

## Dependencies - Downloads


You will also need to download the following dependencies:



### cobertura dependencies

  - net.sourceforge.cobertura:cobertura:2.1.1: (It may be available on one of: [bintray](https://bintray.com/net.sourceforge.cobertura/maven/cobertura/2.1.1/view#files/net.sourceforge.cobertura/cobertura/2.1.1) [mvn central](http://search.maven.org/#artifactdetails|net.sourceforge.cobertura|cobertura|2.1.1|jar))


### compile dependencies

  - com.mchange:c3p0:0.9.5.2: (It may be available on one of: [bintray](https://bintray.com/com.mchange/maven/c3p0/0.9.5.2/view#files/com.mchange/c3p0/0.9.5.2) [mvn central](http://search.maven.org/#artifactdetails|com.mchange|c3p0|0.9.5.2|jar))


### runtime dependencies

  - com.mchange:c3p0:0.9.5.2: (It may be available on one of: [bintray](https://bintray.com/com.mchange/maven/c3p0/0.9.5.2/view#files/com.mchange/c3p0/0.9.5.2) [mvn central](http://search.maven.org/#artifactdetails|com.mchange|c3p0|0.9.5.2|jar))


### testCompile dependencies

  - org.postgresql:postgresql:9.4.1209.jre7: (It may be available on one of: [bintray](https://bintray.com/org.postgresql/maven/postgresql/9.4.1209.jre7/view#files/org.postgresql/postgresql/9.4.1209.jre7) [mvn central](http://search.maven.org/#artifactdetails|org.postgresql|postgresql|9.4.1209.jre7|jar))
  - junit:junit:4.12: (It may be available on one of: [bintray](https://bintray.com/junit/maven/junit/4.12/view#files/junit/junit/4.12) [mvn central](http://search.maven.org/#artifactdetails|junit|junit|4.12|jar))


### testRuntime dependencies

  - org.postgresql:postgresql:9.4.1209.jre7: (It may be available on one of: [bintray](https://bintray.com/org.postgresql/maven/postgresql/9.4.1209.jre7/view#files/org.postgresql/postgresql/9.4.1209.jre7) [mvn central](http://search.maven.org/#artifactdetails|org.postgresql|postgresql|9.4.1209.jre7|jar))
  - junit:junit:4.12: (It may be available on one of: [bintray](https://bintray.com/junit/maven/junit/4.12/view#files/junit/junit/4.12) [mvn central](http://search.maven.org/#artifactdetails|junit|junit|4.12|jar))
  - org.slf4j:slf4j-api:1.7.13: (It may be available on one of: [bintray](https://bintray.com/org.slf4j/maven/slf4j-api/1.7.13/view#files/org.slf4j/slf4j-api/1.7.13) [mvn central](http://search.maven.org/#artifactdetails|org.slf4j|slf4j-api|1.7.13|jar))

**NOTE:** You may need to download any dependencies of the above dependencies in turn (i.e. the transitive dependencies)

# License

```
The MIT License (MIT)

Copyright (c) 2016 synapticloop

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```


--

> `This README.md file was hand-crafted with care utilising synapticloop`[`templar`](https://github.com/synapticloop/templar/)`->`[`documentr`](https://github.com/synapticloop/documentr/)

--

