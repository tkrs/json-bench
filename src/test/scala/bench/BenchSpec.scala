package bench

import bench.State.Data
import org.scalatest._

class BenchSpec extends FunSuite {

  def benchTest(f: => String): Assertion = {
    val expected = Seq(
      """{"i":1,"foo":{"i":2,"foo":{"i":3,"foo":{"i":4,"foo":{"i":5,"foo":{"i":6,"foo":{"i":7,"foo":{"i":8,"foo":{"i":9,"foo":{"i":10,"foo":null}}}}}}}}}}""",
      """{"i":1,"foo":{"i":2,"foo":{"i":3,"foo":{"i":4,"foo":{"i":5,"foo":{"i":6,"foo":{"i":7,"foo":{"i":8,"foo":{"i":9,"foo":{"i":10,"foo":null}}}}}}}}}}""",
      """{"i":1,"foo":{"i":2,"foo":{"i":3,"foo":{"i":4,"foo":{"i":5,"foo":{"i":6,"foo":{"i":7,"foo":{"i":8,"foo":{"i":9,"foo":{"i":10,"foo":null}}}}}}}}}}""",
      """{"i":1,"foo":{"i":2,"foo":{"i":3,"foo":{"i":4,"foo":{"i":5,"foo":{"i":6,"foo":{"i":7,"foo":{"i":8,"foo":{"i":9,"foo":{"i":10,"foo":null}}}}}}}}}}""",
      """{"i":1,"foo":{"i":2,"foo":{"i":3,"foo":{"i":4,"foo":{"i":5,"foo":{"i":6,"foo":{"i":7,"foo":{"i":8,"foo":{"i":9,"foo":{"i":10,"foo":null}}}}}}}}}}""",
      """{"i":1,"foo":{"i":2,"foo":{"i":3,"foo":{"i":4,"foo":{"i":5,"foo":{"i":6,"foo":{"i":7,"foo":{"i":8,"foo":{"i":9,"foo":{"i":10,"foo":null}}}}}}}}}}""",
      """{"i":1,"foo":{"i":2,"foo":{"i":3,"foo":{"i":4,"foo":{"i":5,"foo":{"i":6,"foo":{"i":7,"foo":{"i":8,"foo":{"i":9,"foo":{"i":10,"foo":null}}}}}}}}}}""",
      """{"i":1,"foo":{"i":2,"foo":{"i":3,"foo":{"i":4,"foo":{"i":5,"foo":{"i":6,"foo":{"i":7,"foo":{"i":8,"foo":{"i":9,"foo":{"i":10,"foo":null}}}}}}}}}}""",
      """{"i":1,"foo":{"i":2,"foo":{"i":3,"foo":{"i":4,"foo":{"i":5,"foo":{"i":6,"foo":{"i":7,"foo":{"i":8,"foo":{"i":9,"foo":{"i":10,"foo":null}}}}}}}}}}""",
      """{"i":1,"foo":{"i":2,"foo":{"i":3,"foo":{"i":4,"foo":{"i":5,"foo":{"i":6,"foo":{"i":7,"foo":{"i":8,"foo":{"i":9,"foo":{"i":10,"foo":null}}}}}}}}}}"""
    ).mkString("[", ",", "]")
    val foos = f
    println(foos)
    assert(foos === expected)
  }

  private val t = new Bench with Params {
    def length = 10
    def depth  = 10
  }
  t.setup(new Data)

  test("CirceManualBench")(benchTest(t.encodeCirceManual))
  test("CirceAutoBench")(benchTest(t.encodeCirceAuto))
  test("ArgonautBench")(benchTest(t.encodeArgonaut))
  test("SprayJsonBench")(benchTest(t.encodeSprayJson))
  test("Json4sNativeBench")(benchTest(t.encodeJson4sNative))
  test("Json4sJacksonBench")(benchTest(t.encodeJson4sJackson))
  //test("JsoninterScalaBench")(benchTest(new String(t.encodeJsoniterScalaToBytes, "UTF-8"))) // TODO: Use a custom codec to serialize optional fields with None as JSON key/value pair with null values
  test("JacksonScalaBench")(benchTest(t.encodeJackson))
  test("UPickleBench")(benchTest(t.encodeUPickle))
  // test("PlayJsonBench")(benchTest(t.encodePlayJson)) // TODO: NullPointerException occurred
}
