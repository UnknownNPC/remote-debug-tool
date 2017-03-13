import akka.actor._
import org.slf4j.LoggerFactory


object Main {

  private val log = LoggerFactory.getLogger(this.getClass)

  def main(args: Array[String]) {

    implicit val system = ActorSystem("debug-test-tool")


  }

}