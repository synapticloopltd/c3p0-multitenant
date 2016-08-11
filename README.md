

> **This project requires JVM version of at least 1.7**




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

## Dependencies - Gradle

```
dependencies {
	runtime(group: 'synapticloop', name: 'c3p0-multitenant', version: '0.0.1', ext: 'jar')

	compile(group: 'synapticloop', name: 'c3p0-multitenant', version: '0.0.1', ext: 'jar')
}
```

or, more simply for versions of gradle greater than 2.1

```
dependencies {
	runtime 'synapticloop:c3p0-multitenant:0.0.1'

	compile 'synapticloop:c3p0-multitenant:0.0.1'
}
```

## Dependencies - Maven

```
<dependency>
	<groupId>synapticloop</groupId>
	<artifactId>c3p0-multitenant</artifactId>
	<version>0.0.1</version>
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

**NOTE:** You may need to download any dependencies of the above dependencies in turn (i.e. the transitive dependencies)

--

> `This README.md file was hand-crafted with care utilising synapticloop`[`templar`](https://github.com/synapticloop/templar/)`->`[`documentr`](https://github.com/synapticloop/documentr/)

--

