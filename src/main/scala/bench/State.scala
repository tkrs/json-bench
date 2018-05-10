package bench

import java.util.concurrent.TimeUnit

import org.openjdk.jmh.annotations._

object State {

  case class Foo[F[_]](foo: F[Foo[F]])

  private def genFoo(i: Int, foo: Foo[Option]): Foo[Option] = {
    if (i == 0) foo
    else genFoo(i - 1, Foo(Some(foo)))
  }

  @State(Scope.Thread)
  class Data {
    val foo10: Foo[Option] = genFoo(9, Foo(Option.empty[Foo[Option]]))
    val foo100: Foo[Option] = genFoo(99, Foo(Option.empty[Foo[Option]]))
    val foo1000: Foo[Option] = genFoo(999, Foo(Option.empty[Foo[Option]]))

    @inline def get(i: Int): Foo[Option] = i match {
      case 10 => foo10
      case 100 => foo100
      case 1000 => foo1000
    }
  }
}

@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 10, time = 1)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(2)
abstract class Bench

class CirceBench extends Bench {
  import io.circe.generic.auto._
  import io.circe.syntax._
  import State._

  @Param(Array("10", "100", "1000"))
  var depth: Int = _

  @Benchmark
  def encode(data: Data): String = data.get(depth).asJson.noSpaces
}
