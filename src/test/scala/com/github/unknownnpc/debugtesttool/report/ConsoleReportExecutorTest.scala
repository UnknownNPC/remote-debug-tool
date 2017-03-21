package com.github.unknownnpc.debugtesttool.report

import com.github.unknownnpc.debugtesttool.domain.JvmCaseSummary
import org.scalatest.{Matchers, WordSpec}

class ConsoleReportExecutorTest extends WordSpec with Matchers {

  val validTable = "+--+---------+----+----------+---------------+--------------+" + "\n" +
    "|ID|  Address|Port|Class name|Breakpoint line|     JVM value|" + "\n" +
    "| 2|127.0.0.1|8080|          |               |              |" + "\n" +
    "|  |         |    |ClassNameC|             15|magic result C|" + "\n" +
    "|  |         |    |ClassNameD|             19|magic result D|" + "\n" +
    "|  |         |    |ClassNameE|             20|magic result E|" + "\n" +
    "| 1|localhost|8080|          |               |              |" + "\n" +
    "|  |         |    |ClassNameA|             13|magic result A|" + "\n" +
    "|  |         |    |ClassNameB|             14|magic result B|" + "\n" +
    "+--+---------+----+----------+---------------+--------------+"


  val emptyTable = "+--+-------+----+----------+---------------+---------+" + "\n" +
    "|ID|Address|Port|Class name|Breakpoint line|JVM value|" + "\n" +
    "+--+-------+----+----------+---------------+---------+"

  "ConsoleReportExecutor" should {

    "build valid table for several summaries" in {
      val summaries = List(
        JvmCaseSummary(1, "localhost", 8080, 13, "ClassNameA", "magic result A"),
        JvmCaseSummary(1, "localhost", 8080, 14, "ClassNameB", "magic result B"),
        JvmCaseSummary(2, "127.0.0.1", 8080, 15, "ClassNameC", "magic result C"),
        JvmCaseSummary(2, "127.0.0.1", 8080, 19, "ClassNameD", "magic result D"),
        JvmCaseSummary(2, "127.0.0.1", 8080, 20, "ClassNameE", "magic result E")
      )
      val result = ConsoleReportExecutor.execute(summaries)
      result should equal(validTable)
    }

    "print empty string if nothing to do" in {
      val summaries = List.empty
      val result: String = ConsoleReportExecutor.execute(summaries)
      result should equal(emptyTable)
    }

  }

}
