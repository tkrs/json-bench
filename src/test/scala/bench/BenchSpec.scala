package bench

import bench.State.Data
import org.scalatest._

class BenchSpec extends FunSuite {

  def benchTest(bench: Bench): Assertion =  {
    bench.depth = 10
    val json = bench.encode(new Data)
    assert(json === """{"foo":{"foo":{"foo":{"foo":{"foo":{"foo":{"foo":{"foo":{"foo":{"foo":null}}}}}}}}}}""")
  }

  test("CirceBench")(benchTest(new CirceBench))
  test("SprayJsonBench")(benchTest(new SprayJsonBench))
  // test("PlayJsonBench")(benchTest(new PlayJsonBench))
}
