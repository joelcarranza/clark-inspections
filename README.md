# CPU Mobile Work Management

Navigate module developed for Clark Public Utilities which allows a user to
receive and complete work orders in the field. 

The module class is:

	gks.clark.inspections.PlannerModule

This project is built using maven

## Building

This project is built using the java build tool [Maven](https://maven.apache.org/)

The recommended way to build this project is to run the following command:

    mvn install

This builds the project and creates a zip "package" of all distributable files in `target/mwm-<VERSION>-pkg.zip`

## Installation

Installation of the build package should be done using Manifestly. From the target site's `zip/` directory run the following command

    Manifestly --root=.. --deploy clark-inspections-<VERSION>-pkg.zip

Copy the file `conf/mwm.properties.default` to `conf/mwm.properties` and modify the configuration values as appropriate for the installation

Add the following entry to `conf/navlinks.conf`

	<Tool inspections>
	    Module  gks.clark.inspections.PlannerModule
	    ModuleConfig http:/scripts/inspections/module
	    Tooltip	Inspections Planner Module
	    Icon	none
	</Tool>

Modify `conf/navigate.properties` and ensure that the jar file in package is listed: 

    java_classpath = clark-inspections, <jar names>

and modify the `links` property to include `mwm`

	links = inspections

Finally from site's `src/` directory, run the build process

    ant.bat

## Creating a Release

Releases are managed using the [release plugin](https://maven.apache.org/maven-release/maven-release-plugin/). You can generate a release by running the following commands

    # update pom to non-SNAPSHOT version and tag 
    mvn release:prepare
    # build from tag and publish to artifact repository
    mvn release:perform

For minor releases, you can choose to use the most sensible values and avoid any prompting and do both steps at once by running the following command:

    mvn -B release:prepare release:perform

## License

Copyright 2024 by Gatekeeper Systems All Rights Reserved.

Unpublished Work -- Protected under the copyright laws of the United States.

Restricted Rights Legend: Use, duplication or disclosure of the software
contained hereon is governed by the terms of a license agreement.  In
the absence of an agreement, use, duplication or disclosure by the United
States Government is subject to restrictions stated in subparagraph
(c)(1) of the Commercial Computer Software -- Restricted Rights clause
at FAR 52.227-9 or subparagraph (c)(1)(ii) of the Rights in Technical
Data and Computer Software clause at DFARS 252.227-7013, as applicable.

Contractor/Manufacturer:

Gatekeeper Systems  
99 East C Street Ste. 209  
Upland, Ca. 91786  

Tel: (626) 449-8135  
Fax: (626) 440-1742  

E-Mail: info@gatekeeper.com  
URL:    https://www.gatekeeper.com/