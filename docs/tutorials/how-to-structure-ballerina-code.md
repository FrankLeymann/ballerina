# Introduction
This document demonstrates the development of a Ballerina project and shows how to use the `ballerina` tool to fetch, build, and install Ballerina packages and commands with repositories that are local and remote. 

Ballerina Central is a globally hosted package management system to discover, download, and publish packages.

The `ballerina` tool requires you to organize your code in a specific way. This document explains the simplest way to get up and running with a Ballerina installation.

# Code Organization
## Overview
* Ballerina progammers can either place their code into a single source code file or in a *project* directory.
* A Ballerina *program* is a compiled and linked binary.
* A *package* is a directory that contains Ballerina source code files.
* A *repository* is versioned collections of compiled or source code *packages*.
* A *project* atomically manages a collection of *packages* and *programs*.
  * A user-managed manifest file, `Ballerina.toml`
  * A Ballerina-managed `.ballerina/` folder with implementation metadata and cache
  * A project repository for storing dependencies

## Program
A *program* is a runtime executable, ending with a `.balx` extension. A *program* is the transitive closure of one Ballerina package without including `ballerina/*` packages, since those are dynamically linked within Ballerina's runtime engine during execution. A *package* that is a *program* compiles into a a file with `.balx` extension, otherwise it is treated as a to-be-linked library that ends with a `.balo` extension.

The program's package must contain a `main()` function (a process entry point) or contain a `service<>` (a network-accessible API).

A program can import dependent *packages* which are stored within a *repository*. 

Suppose you have the following structure:

```
/local/ballerina/src
  sample.bal
```

and `sample.bal` contained both a `main()` entry point and a `service<>`:

```ballerina
import ballerina/http;
import ballerina/io;

function main (string[] args) {
    io:println("Hello, World!");
}

service<http:Service> hello bind { port: 9090 } {
    resource sayHello (endpoint caller, http:Request req) {
        http:Response res = new;
        res.setStringPayload("Hello, World!");
        _ = res->response(res);
    }
}
```

### Build and Run Programs
You can build and run the `main()` function of a Ballerina file by:
```bash
# Run from any location
$ ballerina run /local/ballerina/src/sample.bal

# Run from within the local directory
$ cd /local/ballerina/src
$ ballerina run sample.bal
```

Build and run the services of a file in the default package:
```bash

# Run from any location
$ ballerina run -s /local/ballerina/src/sample.bal
```

If your package contains only services, then you may omit the `-s` flag.

You can build the program without running it:
```bash
$ cd /local/ballerina/src
$ ballerina build sample.bal

# This generates 'sample.balx'
# You can run an existing program that has a '.balx'
$ ballerina run [-s] sample.balx
```

## Packages
A *package* is a directory that contains Ballerina source code files and are part of a namespace. Packages faciliate collaboration, sharing, and reuse. Packages can include functions, connectors, constants, annotations, enumerations, services, and objects. Packages are shared among programs, projects, and users by being pushed into a repository.

Packages:
1. May or may not have a version
2. However, packages cannot be pushed into a registry for sharing without a version
3. Are referenced by `<org-name>/<package-name>` where `<org-name>` is a namespace from within a repository.

Package names can contain alphanumeric characters including dots `.`. Dots in a package name have no meaning other than the last segment after the final dot being used as a default alias within your source code. 

### Importing Packages
Your Ballerina source files can import packages:

```ballerina
import [<org-name>]/<package-name> her.package [ [version <string>] [as <identifier>] ];
```

When importing a package, you can then use its functions, annotations and other objects in your code. You reference these objects with a qualified identifier followed by a colon `:`, such as `<identifier>:<package-object>`.

Identifiers are either derived or explicit. The default identifier is either the package name, or if the package name has dots `.` include, then the last word after the last dot. For example, `import ballerina/http;` will have `http:` be the derived identifer. The package `import tyler/net.http.exception` would have `exception:` as the default identifier. 

You can have an explicit identifier by using the `as <identifier>` syntax.

```ballerina
import ballerina/http;

# The 'Service' object comes from the imported package.
service<http:Service> hello bind { port:9090 } {

    # The 'Request' object comes from the imported package.
    resource sayHello (endpoint caller, http:Reqeust req) {
        ...
     }
}
```

Or you can override the default identifier:
```ballerina
import ballerina/http as network;

service<network:Service> hello bind { port:9090 } {

    # The 'Request' object comes from the imported package.
    resource sayHello (endpoint caller, network:Reqeust req) {
        ...
     }
}
```

### Package Version Dependency
If your source file is not part of a project, then you can explicitly manage version dependencies of imported packages within the source file using the `import` statement.

If an import statement does not explicitly specify a version, then the compiler will use the `latest` package version from a repository, if one exists. 

```ballerina
import tyler/http version 3.0.1;

function main(string[] args) {
  http:Person x = http:getPerson();
}
```

### Importing Different Versions of the Same Package
Your program can import multiple versions of the same package.

```ballerina
import tyler/http version 3.0.1 as identity3;
import tyler/http version 4.5.0 as identity4;

function main(string[] args) {
  identity3:Person x = identity3:getPerson();
  identity4:Person y = identity4:getPerson();
}
```

### Compiled Packages
A compiled package is the compiled representation of a single package of Ballerina code, which including transitive dependencies into the compiled unit.

Packages can only be created, versioned, and pushed into a repository as part of a *project*.

### Running Compiled Packages
An entrypoint such as a `main()` or a `service<>` that is compiled as part of a named package is automatically linked into a `.balx`. You can run a named, compiled package:

```bash
ballerina run [-s] <org-name>/<package-name>
```

## Projects
* A *project* is a directory which atomically manages a collection of *packages* and *programs*. It has:
  * A user-managed manifest file, `Ballerina.toml`
  * A Ballerina-managed `.ballerina/` folder with implementation metadata and cache
  * A project repository for storing dependencies

Projects are atomically managed, so dependency management, compilation, unit tests, and artifact generation are done collectively across the source code files and packages defined within a project.

### Create a Project
You can create a project from any folder:

```bash
ballerina init [-i]
```

The command will initialize a simple project with a package inside of it. If the folder where this command is run has Ballerina source files or subfolders, those will be placed into the new project.

### Create a Package
Each subdirectory of the project root folder defines a single package. The subdirectory's name will be used to name the package. Any additional subdirectories within the package have no semantic meaning and can be used by the developer for organizing files. The package subdirectories can have as many Ballerina source files and all will be included within the package when it is built.

The folders `.ballerina/`, `tests/`, and `resources/` are reserved folder names that can exist within the project root which are ignored as packages. These folders may also exist within a package and are ignored, instead treated as special case folders that contain unit tests or other files that must be included within a package.

### Project Structure
```
/
  .gitignore
  Ballerina-lock.toml  # Generated during build, used to rebuild identical binary
  Ballerina.toml       # Configuration that defines project intent
  .ballerina/          # Internal cache management and contains project repository
                       # Project repository is built or downloaded package dependencies

  main.bal             # Part of the “unnamed” package, compiled into a main.balx, error if not an entrypoint
                       # You can have many files in the "unnamed" package, though it is not advisable

  package1/            # The source in this directory will be named “<org-name>/package1” 
    Package.md         # Optional, contains descriptive metadata for display at Ballerina Central
    *.bal              # In this dir and recursively in subdirs except tests/ and resources/
    [tests/]           # Package-specific unit and integration tests
    [resources/]       # Package-specific resources
      *.jar            # Optional, if package includes native Java libraries to link + embed 
    
  packages.can.include.dots.in.dir.name/
    Package.md
    *.bal
    *.jar
    [tests/]         
    [resources/]     
      *.jar            # Optional, if package includes native Java libraries to link + embed 

  [tests/]             # Tests executed for every package in the project
  [resources/]         # Resources included with every package in the project

  target/              # Compiled binaries and other artifacts end up here
      main.balx
      package1.balo
      packages.can.include.dots.in.dir.name.bal
```

You can optionally include Ballerina source files in the *empty package*, in the *source root* folder and not containing a package name. 

```
MyProgramDirectory/
  aballerinafile.bal
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


Your source files indicate their *package name* by including at the top of the file:
```ballerina
package some.name.with.a.namespace [version <identifer>];
```

You can version your packages with any non-space strings. There are no semantics associated with version identifiers. The only reuqirement is that all Ballerina files within the same package have package names that are identical. Version strings in the form of `<number>.<number>.<number>.<qualifier>` is reserved by the Ballerina system for future semantics.


## Repositories
A repository is a collection of compiled and / or source code packages of Ballerina code. A repository helps organize packages used by multiple programs by managing their versions and assets in a central location.

There are four kinds of repositories:
1. User Repository. Located locally on the developer's machine, this repository is located at the path defined by `BALLERINA_REPOSITORY`, or `~\.ballerina` if not specified. The developer must copy packages into their repository by installing them from their program directory or a remote location.

2. System Repository. A special repository that is embedded within the Ballerina distribution. This repository contains `ballerina.*` core packages. These packages are dynamically linked into your programs when they are started.

3. Extensions Repository. A special repository that is embedded within the Ballerina distribution. It contains packages from the community that are included as standard extensions. These packages are statically included into compiled Ballerina programs by the `ballerina build` command.

4. Ballerina Central. Located at central.ballerina.io, this centrally managed repository is a community hub to discover, download, and publish Ballerina code and tool extensions as packages. 

A repository is structured as:
```
repository-path/
  artifacts/
    src/
      packageName/
        versionNumber/
             ballerina-source-file1.bal
             ballerina-source-file2.bal
        ..
    obj/
      packageName/
        versionNumber/
          ballerinaPackageName.balo # compiled code and jars
  metadata/                         # internal stuff that helps Ballerina execute faster 
```

When compiling or running a program from within your program directory, the build and runtime utilities will search for packages in the system repository, then the extension repository, your program directory, and then the user repository. Packages in Ballerina Central must be downloaded and installed into your user repository to be included within a program.

### Install a Package Into Your User Repository
If you are in in a program directory and have an already compiled package, you can install it into your user repository with the `install` command:
```bash
ballerina install <compiled-package-file.balo> [-r repository-path]
```

You can also install the source code into the repository as well, which is useful if you would like third parties to have tracing and debugging of your package within their IDE tools:
```bash
ballerina install <package-name> [-r repository-path]
```

You can remove a package from the user repository with the `uninstall` command:
```bash
ballerina uninstall <package-name> [-r repository-path]
```

# Testing
# Remote Packages

## GitHub
## Ballerina Central
## Publishing Packages



## Next steps

Now that you're familiar with running Ballerina in standalone and server mode, using the Composer to build a program, and creating a service and resource, you are ready to learn more. 

* Read the [Key Concepts](../key-concepts.md) page to familiarize yourself with the rest of the primary features you need to know about.
* Read about the [Tools](../tools.md) that you can use with Ballerina, such as using an IDE instead of the Composer. 
* Run through the rest of the [Tutorials](index.md) to get hands-on experience.  
