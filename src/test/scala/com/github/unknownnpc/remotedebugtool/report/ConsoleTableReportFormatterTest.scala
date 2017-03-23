package com.github.unknownnpc.remotedebugtool.report

import com.github.unknownnpc.remotedebugtool.domain.JvmReportRow
import org.scalatest.{Matchers, WordSpec}

class ConsoleTableReportFormatterTest extends WordSpec with Matchers {

  private val validTable = "\n\n" +
    "+--+---------+----+----------+---------------+--------------+" + "\n" +
    "|ID|  Address|Port|Class name|Breakpoint line|     JVM value|" + "\n" +
    "| 2|127.0.0.1|8080|          |               |              |" + "\n" +
    "|  |         |    |ClassNameC|             15|magic result C|" + "\n" +
    "|  |         |    |ClassNameD|             19|magic result D|" + "\n" +
    "|  |         |    |ClassNameE|             20|magic result E|" + "\n" +
    "| 1|localhost|8080|          |               |              |" + "\n" +
    "|  |         |    |ClassNameA|             13|magic result A|" + "\n" +
    "|  |         |    |ClassNameB|             14|magic result B|" + "\n" +
    "+--+---------+----+----------+---------------+--------------+" + "\n\n"


  private val emptyTable = "\n\n" +
    "+--+-------+----+----------+---------------+---------+" + "\n" +
    "|ID|Address|Port|Class name|Breakpoint line|JVM value|" + "\n" +
    "+--+-------+----+----------+---------------+---------+" + "\n\n"

  "ConsoleTableReportFormatter" should {

    "build valid table for several report rows" in {
      val reportRows = List(
        JvmReportRow(1, "localhost", 8080, 13, "ClassNameA", "magic result A"),
        JvmReportRow(1, "localhost", 8080, 14, "ClassNameB", "magic result B"),
        JvmReportRow(2, "127.0.0.1", 8080, 15, "ClassNameC", "magic result C"),
        JvmReportRow(2, "127.0.0.1", 8080, 19, "ClassNameD", "magic result D"),
        JvmReportRow(2, "127.0.0.1", 8080, 20, "ClassNameE", "magic result E")
      )
      val result = ConsoleTableReportFormatter.format(reportRows)
      result should equal(validTable)
    }

    "print empty string if nothing to do" in {
      val reportRows = List.empty
      val result = ConsoleTableReportFormatter.format(reportRows)
      result should equal(emptyTable)
    }

  }

}
