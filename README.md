# DECOmap IF-MAP client

This is a modular IF-MAP client based on Java and the ifmapj IF-MAP library. It supports to read log information from several different services and publish them to a MAP-Server. This refactored version was developed during the [SIMU](http://www.simu-project.de/) research project and supports monitoring of the following services:

* Snort
* iptables
* Icinga
* Nagios
* LDAP
* OpenVPN
* FreeRADIUS

## Preparation ##

This project requires the SIMU Metadata Factory to be installed in your local Maven repository. The factory may be [downloaded here](https://github.com/decoit/simu-metadata-factory). Besides this library there are some further requirements to compile and run the file integrity monitor:

* Java 7 or higher
* Maven 3

To compile this project the Oracle JDK is preferred but it may work as well on other JDK implementations. Any Java 7 compatible JRE (Oracle, OpenJDK, Apple) should be able to run the application.

## Installation ##

Follow these steps to compile the project:

* Make sure that you have the SIMU Metadata Factory installed
* Open a command prompt and change directory to the root of this project
* Execute `mvn package`
* Unpack the contents of `target/decomap-0.2.0.0.dist.zip` or `target/decomap-0.2.0.0.dist.tar.gz` to your chosen installation directory

## Configuration ##

The application may be configured manually by editing the files in the config/ directory, but it is recommended to use the configuration GUI, which is available from [this repository](https://github.com/decoit/decomap-config-gui). The .tpl files in the config/ directory include comments that describe what a specific property does.

## Run the application ##

To run the application simple execute `java -jar decomap-0.2.0.0.jar` inside the installation directory. All clients except iptables will run under any user account that has access to the log files to be monitored. The iptables module requires root rights because it must interact with iptables directly.

## License
The source code and all other contents of this repository are copyright by DECOIT GmbH and licensed under the terms of the [Apache License Version 2.0](http://www.apache.org/licenses/). A copy of the license may be found inside the LICENSE file.
