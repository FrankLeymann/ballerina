# Introduction
This document demonstrates the development of a BAllerina package and shows how to use the `ballerina` tool to fetch, build, and install Ballerina packages and commands with repositories that are local and remote. Ballerina Central is a globally hosted package management system to discover, download, and publish packages.

The `ballerina` tool requires you to organize your code in a specific way. This document explains the simplest way to get up and running with a Ballerina installation.

# Code Organization
## Overview
* Ballerina progammers keep all their Ballerina code in a single *program directory*.
* A Ballerina *program* contains:
  * *packages* written by you and saved in the *program directory*, and:
  * imported dependent *packages* from a *repository*

* A *package* is a directory that contains Ballerina source code files.
* A *repository* is versioned collections of compiled or source code *packages*.

## Program Directory
The program directory is the absolute path of the *source root*, the base directory for packages. 

```
MyProgramDirectoryCanBeAnywhere/
  apackagefolder/
    file1.bal
    file2.bal
  a.package.can.have.dots.to.create.a.namespace/
    another_file.bal
  another.package.with.a.namespace/
    yet_another_file.bal
  ...
```

You can optionally include Ballerina source files in the *empty package*, in the *source root* folder and not containing a package name. 

```
MyProgramDirectory/
  aballerinafile.bal
```

## Program
A *program* is the transitive closure of one Ballerina package without including `ballerina.*` packages as they are contained within Ballerina's runtime engine. A *package* that is a *program* will be compiled into a file that ends with `.balx`. A *package* that is not a *program* will be compiled into a file that ends with `.balo`.

The program's package must contain a `main()` function (a process entry point) or contain a `service<>` (a network-accessible API).

Suppose you have the following structure:

```
/local/ballerina/src
  sample.bal
```

and `sample.bal` contained both a `main()` entry point and a `service<>`:

```
import ballerina.net.http;

function main (string[] args) {
    println("Hello, World!");
}

service<http> hello {
    resource sayHello (http:Request req, http:Response res) {
        res.setStringPayload("Hello, World!");
        _ = res.send();
    }
}
```

### Build and Run Programs

Build and run the `main()` function of a file in the default package:
```bash
# Run from any location
$ ballerina run /local/ballerina/src/sample.bal

# Run from within the program directory
$ cd /local/ballerina/src
$ ballerina run sample.bal
```

Build and run the services of a file in the default package:
```bash

# Run from any location
$ ballerina run -s /local/ballerina/src/sample.bal

# Alternative way to identify the program directory source root
$ ballerina run -sourceroot /local/ballerina/src run -s sample.bal
```

Build a program in the default package (ie, build a package):
```bash
$ cd /local/ballerina/src
$ ballerina build sample.bal

# This generates sample.balx
# You can run a program already compiled into a .balx file
$ ballerina run [-s] sample.balx
```

Build a program in a named package (ie, build a named package):
```bash
$ ballerina build <package_name> [-o output_name.balx]

# This generates <package_name>.balx
# Run the program already compiled into a .balx file
$ ballerina run [-s] <package_name>.balx

# You can build and run a named package, too:
$ ballerina run [-s] <package_name>
```

* A *package* can depend upon other packages.
* A *package* can be given a namespace using dots `.` as separators.
* A *package* is versioned.

## BALLERINA_PATH Environment
## Import Path
## Executable Program
## Packages
# Testing
# Remote Packages
## GitHub
## Ballerina Central



## Next steps

Now that you're familiar with running Ballerina in standalone and server mode, using the Composer to build a program, and creating a service and resource, you are ready to learn more. 

* Read the [Key Concepts](../key-concepts.md) page to familiarize yourself with the rest of the primary features you need to know about.
* Read about the [Tools](../tools.md) that you can use with Ballerina, such as using an IDE instead of the Composer. 
* Run through the rest of the [Tutorials](index.md) to get hands-on experience.  
