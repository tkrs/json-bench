package bench

import java.util.concurrent.TimeUnit

import io.circe.{Encoder, Json}
import org.openjdk.jmh.annotations._

object State {

  case class Foo[F[_]](i: Int, foo: F[Foo[F]])

  private def genFoo(i: Int, foo: Foo[Option]): Foo[Option] = {
    if (i == 0) foo
    else genFoo(i - 1, Foo(i, Some(foo)))
  }

  @State(Scope.Thread)
  class Data {
    val foo10_10: Seq[Foo[Option]] = Iterator.continually(genFoo(9, Foo(10, Option.empty[Foo[Option]]))).take(10).toList
    val foo100_10: Seq[Foo[Option]] = Iterator.continually(genFoo(99, Foo(100, Option.empty[Foo[Option]]))).take(10).toList
    val foo10_100: Seq[Foo[Option]] = Iterator.continually(genFoo(9, Foo(10, Option.empty[Foo[Option]]))).take(100).toList
    val foo100_100: Seq[Foo[Option]] = Iterator.continually(genFoo(99, Foo(100, Option.empty[Foo[Option]]))).take(100).toList

    @inline def get(i: Int, j: Int): Seq[Foo[Option]] = (i, j) match {
      case (10, 10) => foo10_10
      case (100, 10) => foo100_10
      case (10, 100) => foo10_100
      case (100, 100) => foo100_100
      case (_, _) => ???
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

  @Param(Array("10", "100"))
  var length: Int = _

  @Param(Array("10", "100"))
  var depth: Int = _

  protected def encode0(foo: Seq[Foo[Option]]): String

  @Benchmark
  def encode(data: Data): String = encode0(data.get(length, depth))
}

class CirceAutoBench extends Bench {
  import io.circe.generic.auto._
  import io.circe.syntax._
  import State._

  def encode0(foo: Seq[Foo[Option]]): String = foo.asJson.noSpaces
}

class CirceBench extends Bench {
  import io.circe.syntax._
  import State._

  implicit val encodeFoo: Encoder[Foo[Option]] = Encoder.instance {
    case Foo(i, Some(f)) => Json.obj("i" -> Json.fromInt(i), "foo" -> f.asJson)
    case Foo(i, _) => Json.obj("i" -> Json.fromInt(i), "foo" -> Json.Null)
  }

  def encode0(foo: Seq[Foo[Option]]): String = foo.asJson.noSpaces
}

class SprayJsonBench extends Bench {
  import spray.json._
  import DefaultJsonProtocol._
  import State._

  implicit object FooFormat extends JsonFormat[Foo[Option]] {
    def read(json: JsValue): Foo[Option] = ???
    def write(obj: Foo[Option]): JsValue =
      JsObject("i" -> JsNumber(obj.i), "foo" -> obj.foo.toJson)
  }

  def encode0(foo: Seq[Foo[Option]]): String = foo.toJson.compactPrint
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
