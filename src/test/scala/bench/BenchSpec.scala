package bench

import bench.State.Data
import org.scalatest._

class BenchSpec extends FunSuite {

  trait T extends Params {
    def length: Int = 10
    def depth: Int  = 10
  }

  def benchTest(bench: Bench[String]): Assertion = {
    bench.setup(new Data)
    val json = bench.encode
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
    assert(json === expected)
  }

  test("CirceCoreBench")(benchTest(new CirceCoreBenchS with T         {}))
  test("CirceAutoBench")(benchTest(new CirceAutoBenchS with T         {}))
  test("ArgonautBench")(benchTest(new ArgonautBenchS with T           {}))
  test("SprayJsonBench")(benchTest(new SprayJsonBenchS with T         {}))
  test("UPickleBench")(benchTest(new UPickleBenchS with T             {})) // TODO: Unexpected JSON was created
  test("Json4sNativeBench")(benchTest(new Json4sNativeBenchS with T   {})) // TODO: Unexpected JSON was created
  test("Json4sJacksonBench")(benchTest(new Json4sJacksonBenchS with T {}))
  test("JacksonScalaBench")(benchTest(new JacksonScalaBenchS with T   {}))
  test("PlayJsonBench")(benchTest(new PlayJsonBenchS with T           {})) // TODO: NullPointerException occurred
}
