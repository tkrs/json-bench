package bench

import bench.State.Data
import org.scalatest._
import org.scalatest.funsuite._

class BenchSpec extends AnyFunSuite {

  def benchTest(f: => Seq[State.Foo[Option]]): Assertion =
    assert(t.foos === f)

  private val t = new Bench with Params {
    def length = 10
    def depth  = 10
  }
  t.setup(new Data)

  test("CirceManualBench")(benchTest(t.decodeCirce.toTry.get))
  test("CirceAutoBench")(benchTest(t.decodeCirceAuto.toTry.get))
  test("ArgonautBench")(benchTest(t.decodeArgonaut.toOption.get))
  test("SprayJsonBench")(benchTest(t.decodeSprayJson))
  test("JsoninterScalaBench")(benchTest(t.decodeJsoniterScalaFromBytes))
  test("UPickleBench")(benchTest(t.decodeUPickle))
}
