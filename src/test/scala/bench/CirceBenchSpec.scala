package bench

import bench.State.Data
import org.scalatest._

class CirceBenchSpec extends FlatSpec with Matchers {

  it should "create JSON string from the nested Foo instance" in {
    val bench = new CirceBench
    bench.depth = 10
    val json = bench.encode(new Data)
    json shouldBe """{"foo":{"foo":{"foo":{"foo":{"foo":{"foo":{"foo":{"foo":{"foo":{"foo":null}}}}}}}}}}"""
   }
}
