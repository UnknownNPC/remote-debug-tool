# Remote Debug Tool
`RDT` is a simple tool for remote JVM variables search. 
In comparison with `Oracle JDB` you're able to configure several JVM targets with group of `fields` to search.
Current tool was created as help-tool for developer needs in process of distributed systems debug.

*I highly recommend do not use it for production instance monitoring.*

## Main Features ##
  * Configurable target JVM list
  * Configurable breakpoint list for target JVM
 
## Using ##
1. Download the [latest release](https://github.com/UnknownNPC/remote-debug-test-tool/releases) and unpack it.

2. Configure `conf/application.conf` file with target JVMs (`servers`) and debug points (`breakpoints`). Please note that `breakpoint.server-id` params should be equal to `server.id`.

4. Make sure that target application was compiled with `-g` flag. More details [here](http://docs.oracle.com/javase/7/docs/technotes/tools/windows/javac.html)

3. Run `bin/run` for Windows or `bin/run.sh` for Linux

## Building ##
Build with `sbt universal:packageBin` and check `project/target/universal` folder for `remote-debug-tool-*.zip`

## Testing ##
Tests are run with `sbt test`

## Requirements & Compatibility ##
  * [Oracle JDI](https://docs.oracle.com/javase/7/docs/jdk/api/jpda/jdi/)
  * sbt `1.3.13`
  * Java `1.11`
  * Scala `2.12.17`
