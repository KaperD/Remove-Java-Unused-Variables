# Java Unused Variables Remover
CLI tool which removes unused variables in Java source code

Java source code should be Java 17 compatible

Tool operates with one Java file

NB: tool also **removes unused public properties** in classes and interfaces

Note: tool removes only variables which were unused before transformation, but not after it:

```java
class Main {

    void f() {
        int x = 3;
        int y = x;
    }
}
```
Will become 
```java
class Main {

    void f() {
        int x = 3;
    }
}
```
Not
```java
class Main {

    void f() {
    }
}
```

## Build and run

```shell
./gradlew installDist
./build/install/remove-unused/bin/remove-unused
```

## Usage
```text
Usage: remove-unused [OPTIONS] [INPUT_FILE]

Options:
  -r, --raw          Enables saving original layout of code
  -o, --output PATH  File to save result
  -h, --help         Show this message and exit
```

If input file is not specified, tool will read Java code from STDIN

If output file is not specified, tool will print result to STDOUT

## Example
```shell
$ cat Main.java
public class Main {
    int x;

    void f() {
        int x = 3;
        int y = x;
        int z = this.x;
    }
}

interface I {
    int y = 1;
}
$ ./build/install/remove-unused/bin/remove-unused Main.java
public class Main {

    int x;

    void f() {
        int x = 3;
    }
}

interface I {
}

```
