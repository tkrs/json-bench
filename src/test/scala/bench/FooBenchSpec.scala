package bench

import bench.State.Data
import org.scalatest._

class FooBenchSpec extends FunSuite {

  trait T extends FooBench[String] {
    def length: Int = 10
    def depth: Int = 10
  }

  def benchTest(bench: FooBench[String]): Assertion = {
    val json = bench.encode(new Data)
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

  test("CirceBench")(benchTest(new CirceBenchS with T {}))
  test("CirceAutoBench")(benchTest(new CirceAutoBenchS with T {}))
  test("ArgonautBench")(benchTest(new ArgonautBenchS with T {}))
  test("SprayJsonBench")(benchTest(new SprayJsonBenchS with T {}))
  // TODO: Unexpected JSON was created
  test("UPickleBench")(benchTest(new UPickleBenchS with T {}))
  // TODO: Unexpected JSON was created
  test("Json4sNativeBench")(benchTest(new Json4sNativeBenchS with T {}))
  test("JacksonScalaBench")(benchTest(new JacksonScalaBenchS with T {}))
  // test("PlayJsonBench")(benchTest(new PlayJsonBench with T {}))
}
