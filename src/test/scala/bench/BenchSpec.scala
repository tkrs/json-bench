package bench

import bench.State.Data
import org.scalatest._
import org.scalatest.funsuite._

class BenchSpec extends AnyFunSuite {

  def benchTest(f: => String, nullable: Boolean = true): Assertion = {
    val foo = (1 to 10).foldRight("null") {
      case (i, acc) if i == 10 && !nullable => s"""{"i":$i}"""
      case (i, acc)                         => s"""{"i":$i,"foo":$acc}"""
    }
    val expected = (1 to 10).map(_ => foo).mkString("[", ",", "]")
    val foos     = f
    // println(foos)
    assert(foos === expected)
  }

  private val t = new Bench with Params {
    def length = 10
    def depth  = 10
  }
  t.setup(new Data)

  test("CirceManualBench")(benchTest(t.encodeCirce))
  test("CirceAutoBench")(benchTest(t.encodeCirceAuto))
  test("ArgonautBench")(benchTest(t.encodeArgonaut))
  test("SprayJsonBench")(benchTest(t.encodeSprayJson))
  test("Json4sNativeBench")(benchTest(t.encodeJson4s))
  test("Json4sJacksonBench")(benchTest(t.encodeJson4sJackson))
  test("JsoninterScalaBench")(benchTest(new String(t.encodeJsoniterScalaToBytes, "UTF-8"), false)) // TODO: Use a custom codec to serialize optional fields with None as JSON key/value pair with null values
  test("JacksonScalaBench")(benchTest(t.encodeJackson))
  test("UPickleBench")(benchTest(t.encodeUPickle))
  // test("PlayJsonBench")(benchTest(t.encodePlayJson)) // TODO: NullPointerException occurred
}
