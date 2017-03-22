[![Build Status](https://travis-ci.org/UnknownNPC/remote-debug-test-tool.svg?branch=development-0.1)](https://travis-ci.org/UnknownNPC/remote-debug-test-tool)
# Remote Debug Test Tool
`RDTT` is a simple tool for remote JVM variables search. 
In comparison with `Oracle JDB` you're able to configure several JVM targets with group of `fields` to search.
Current tool was created as help-tool for developer needs in process of distributed systems debug.

*I highly recommend do not use it for production instance monitoring.*

## Main Features ##
  * Configurable target JVM list
  * Configurable breakpoint list for target JVM
 
## Using ##
1. Download the [latest release](https://github.com/UnknownNPC/remote-debug-test-tool/releases) and unpack it.

2. Configure `conf/application.conf` file with target JVMs (`test-targets`) and debug points (`test-cases`). Please note that `test-cases.server-id` params should be equal to `test-targets.id`.

4. Make sure that target application was compiled with `-g` flag. More details [here](http://docs.oracle.com/javase/7/docs/technotes/tools/windows/javac.html)

3. Run `bin/run` for Windows or `bin/run.sh` for Linux

## Building ##
Build with `sbt universal:packageBin` and check `project/target/universal` folder for `remote-debug-test-tool-*.zip`

## Testing ##
Tests are run with `sbt test`

## Requirements & Compatibility ##
  * [Oracle JDI](https://docs.oracle.com/javase/7/docs/jdk/api/jpda/jdi/)
  * sbt `0.13.8`
  * Java `1.8`
  * Scala `2.12.1`
  * Akka actors `2.4.17`
