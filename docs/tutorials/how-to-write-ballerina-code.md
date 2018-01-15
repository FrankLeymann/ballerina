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

Build a program in a named package:
```bash
$ ballerina build <package_name> [-o output_name.balx]

# This generates <package_name>.balx
# Run the program already compiled into a .balx file
$ ballerina run [-s] <package_name>.balx

# You can build and run a named package, too:
$ ballerina run [-s] <package_name>
```

## Packages
A *package* is a collection of Ballerina source files that are part of the same namespace. A *program* is a package that has a `main()` function entry point or a set of `service<>` objects which will be hosted as non-terminating servers.

Packages:
1. Have a version. The default version is `0.0.0` if not specified by the developer.
2. Can import and depend upon other packages.
3. Have a namespace determined by its package name with dots `.` used as separators.

Your source files indicate their *package name* by including at the top of the file:
```
package some.name.with.a.namespace [version <identifer>];
```

You can version your packages with any non-space strings. There are no semantics associated with version identifiers. The only reuqirement is that all Ballerina files within the same package have package names that are identical. Version strings in the form of `<number>.<number>.<number>.<qualifier>` is reserved by the Ballerina system for future semantics.

### Importing Packages
Your Ballerina source files can import other packages:

```
import another.package [ [version <identifier>] [as <identifier>] ];
```

When importing a package, you can then use its functions, annotations and other objects in your code. You must reference these objects with their fully qualified name: `another.package.some_function()`. Or, you can reference the last name in the namespace as a default identifier.

Ballerina uses a colon `:` as the separator between a package namespace identifier and the objects contained within a package such as functions, structs, or annotations.

```
import ballerina.net.http;

service<http> hello {

    # The Request and Response structs come from the ballerina.net.http package
    # The package namespace is accessible with `http:`
    resource sayHello (http:Request req, http:Response res) {
        ...
     }
}
```

Or you can override the default identifier:
```
import ballerina.net.http as network;

service<http> hello {

    # The identifier has been renamed from `http` to `network`
    resource sayHello (network:Request req, network:Response res) {
        ...
     }
}
```

### Importing Different Versions of the Same Package
Your program can import multiple versions of the same package.

```
package my.package;

import org.company.wso2.identity version 3.0.1 as identity3;
import org.company.wso2.identity version 4.5.0 as identity4;

function main(string[] args) {
  identity3:person x = identity3:getPerson();
  identity4:person y = identity4:getPerson();
}
```

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
