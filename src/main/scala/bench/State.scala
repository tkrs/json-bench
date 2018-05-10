package bench

import java.util.concurrent.TimeUnit

import org.openjdk.jmh.annotations._

object State {

  case class Foo[F[_]](i: Int, foo: F[Foo[F]])

  private def genFoo(i: Int, foo: Foo[Option]): Foo[Option] = {
    if (i == 0) foo
    else genFoo(i - 1, Foo(i, Some(foo)))
  }

  @State(Scope.Thread)
  class Data {
    val foo10: Foo[Option] = genFoo(9, Foo(10, Option.empty[Foo[Option]]))
    val foo100: Foo[Option] = genFoo(99, Foo(100, Option.empty[Foo[Option]]))
    val foo1000: Foo[Option] = genFoo(999, Foo(1000, Option.empty[Foo[Option]]))

    @inline def get(i: Int): Foo[Option] = i match {
      case 10 => foo10
      case 100 => foo100
      case 1000 => foo1000
    }
  }
}

// jmh:run -jvmArgsAppend "-Xss5m"

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 10, time = 1)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(2)
abstract class Bench {
  import State._
  @Param(Array("10", "100", "1000"))
  var depth: Int = _

  protected def encode0(data: Data): String

  @Benchmark
  def encode(data: Data): String = encode0(data)
}

class CirceBench extends Bench {
  import io.circe.generic.auto._
  import io.circe.syntax._
  import State._

  def encode0(data: Data): String = data.get(depth).asJson.noSpaces
}

class SprayJsonBench extends Bench {
  import spray.json._
  import State._

  implicit object FooFormat extends JsonFormat[Foo[Option]] with AdditionalFormats with StandardFormats {
    def read(json: JsValue): Foo[Option] = ???
    // Is not stack-safe?
    def write(obj: Foo[Option]): JsValue = JsObject("i" -> obj.i.toJson, "foo" -> obj.foo.toJson)
  }

  def encode0(data: Data): String = data.get(depth).toJson.compactPrint
}

//class PlayJsonBench extends Bench {
//  import play.api.libs.json._
//  import play.api.libs.functional.syntax._
//  import State._
//
//  implicit val fooWrites: Writes[Foo[Option]] =
//    (JsPath \ "foo").write[Foo[Option]] { (a: JsValue) => println(a); a }
//
////  implicit def fooNullableWrites[A: Writes]: Writes[Option[A]] =
////    (JsPath \ "foo").writeNullable[A]
//
//  def encode(data: Data): String = Json.toJson(Option(data.get(depth))).toString()
//}
