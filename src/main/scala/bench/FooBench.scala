package bench

import java.util.concurrent.TimeUnit

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import org.openjdk.jmh.annotations._

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 2)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(2)
abstract class Bench

class CirceAutoPermBench extends Bench with CirceAutoBench with PermBench
class CirceAutoDeepBench extends Bench with CirceAutoBench with DeepBench

class CircePermBench extends Bench with CirceBench with PermBench
class CirceDeepBench extends Bench with CirceBench with DeepBench

class SprayJsonPerfmBench extends Bench with SprayJsonBench with PermBench
class SprayJsonDeepBench extends Bench with SprayJsonBench with DeepBench

class ArgonautPermBench extends Bench with ArgonautBench with PermBench
class ArgonautDeepBench extends Bench with ArgonautBench with DeepBench

class UPicklePermBench extends Bench with UPickleBench with PermBench
class UPickleDeepBench extends Bench with UPickleBench with DeepBench

class Json4sNativePermBench extends Bench with Json4sNativeBench with PermBench
class Json4sNativeDeepBench extends Bench with Json4sNativeBench with DeepBench

class JacksonScalaPermBench extends Bench with JacksonScalaBench with PermBench
class JacksonScalaDeepBench extends Bench with JacksonScalaBench with DeepBench

// class PlayJsonPermBench extends PlayJsonBench with PermBench
// class PlayJsonDeepBench extends PlayJsonBench with DeepBench

trait FooBench {
  import State._

  def length: Int
  def depth: Int

  protected def encode0(foos: Seq[Foo[Option]]): String

  @Benchmark
  def encode(data: Data): String = encode0(data.get(length, depth))
}

trait PermBench extends FooBench {

  @Param(Array("10", "100"))
  var length: Int = _

  @Param(Array("10", "100"))
  var depth: Int = _
}

trait DeepBench extends FooBench {

  @Param(Array("1"))
  var length: Int = _

  @Param(Array("1000"))
  var depth: Int = _
}

trait CirceAutoBench { self: FooBench =>
  import io.circe.generic.auto._
  import io.circe.syntax._
  import State._

  def encode0(foos: Seq[Foo[Option]]): String = foos.asJson.noSpaces
}

trait CirceBench { self: FooBench =>
  import io.circe.{Encoder, Json}
  import io.circe.syntax._
  import State._

  implicit val encodeFoo: Encoder[Foo[Option]] = Encoder.instance {
    case Foo(i, Some(f)) => Json.obj("i" -> Json.fromInt(i), "foo" -> f.asJson)
    case Foo(i, _) => Json.obj("i" -> Json.fromInt(i), "foo" -> Json.Null)
  }

  def encode0(foos: Seq[Foo[Option]]): String = foos.asJson.noSpaces
}

trait SprayJsonBench { self: FooBench =>
  import spray.json._
  import DefaultJsonProtocol._
  import State._

  implicit object FooFormat extends JsonFormat[Foo[Option]] {
    def read(json: JsValue): Foo[Option] = ???
    def write(obj: Foo[Option]): JsValue =
      JsObject("i" -> JsNumber(obj.i), "foo" -> obj.foo.toJson)
  }

  def encode0(foos: Seq[Foo[Option]]): String = foos.toJson.compactPrint
}

trait ArgonautBench { self: FooBench =>
  import argonaut._, Argonaut._
  import State._

  implicit val encodeFoo: EncodeJson[Foo[Option]] = EncodeJson[Foo[Option]] {
    case Foo(i, Some(foo)) => Json("i" -> Json.jNumber(i), "foo" -> foo.asJson)
    case Foo(i, _) => Json("i" -> Json.jNumber(i), "foo" -> Json.jNull)
  }

  def encode0(foos: Seq[Foo[Option]]): String = foos.toList.asJson.nospaces
}

trait UPickleBench { self: FooBench =>
  import upickle.default._
  import State._

  implicit val fooWriter: Writer[Foo[Option]] = macroW

  def encode0(foos: Seq[Foo[Option]]): String = write(foos)
}

trait Json4sNativeBench { self: FooBench =>
  import org.json4s._
  import native.Serialization._
  import State._

  implicit val noTypeHintsFormats: Formats = formats(NoTypeHints)

  def encode0(foos: Seq[Foo[Option]]): String = write(foos)
}

trait JacksonScalaBench { self: FooBench =>
  import State._

  val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)

  def encode0(foos: Seq[Foo[Option]]): String = mapper.writeValueAsString(foos)
}

// FIXME: NPE occurred...
abstract class PlayJsonBench { self: FooBench =>
  import play.api.libs.json._
  import play.api.libs.functional.syntax._
  import State._

  implicit val encodeFoo: Writes[Foo[Option]] = (
    (JsPath \ "i").write[Int] and
    (JsPath \ "foo").writeNullable[Foo[Option]]
  )(unlift[Foo[Option], (Int, Option[Foo[Option]])](a => Foo.unapply[Option](a)))

  def encode0(foos: Seq[Foo[Option]]): String = Json.stringify(Json.toJson(foos))
}

object State {

  case class Foo[F[_]](i: Int, foo: F[Foo[F]])

  private def genFoo(i: Int, foo: Foo[Option]): Foo[Option] = {
    if (i == 0) foo
    else genFoo(i - 1, Foo(i, Some(foo)))
  }

  @State(Scope.Benchmark)
  class Data {
    val foo1_1000: Seq[Foo[Option]] = List(genFoo(999, Foo(1000, Option.empty[Foo[Option]])))
    val foo10_10: Seq[Foo[Option]] = Iterator.continually(genFoo(9, Foo(10, Option.empty[Foo[Option]]))).take(10).toList
    val foo100_10: Seq[Foo[Option]] = Iterator.continually(genFoo(99, Foo(100, Option.empty[Foo[Option]]))).take(10).toList
    val foo10_100: Seq[Foo[Option]] = Iterator.continually(genFoo(9, Foo(10, Option.empty[Foo[Option]]))).take(100).toList
    val foo100_100: Seq[Foo[Option]] = Iterator.continually(genFoo(99, Foo(100, Option.empty[Foo[Option]]))).take(100).toList

    @inline final def get(length: Int, depth: Int): Seq[Foo[Option]] = (length, depth) match {
      case (1, 1000) => foo1_1000
      case (10, 10) => foo10_10
      case (100, 10) => foo100_10
      case (10, 100) => foo10_100
      case (100, 100) => foo100_100
      case (_, _) => ???
    }
  }
}
