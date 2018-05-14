package bench

import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

import org.openjdk.jmh.annotations._

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 2)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(2)
abstract class Bench

class CirceAutoPermBenchS extends Bench with CirceAutoBenchS with PermBench[String]
class CirceAutoDeepBenchS extends Bench with CirceAutoBenchS with DeepBench[String]

class CirceJacksonAutoPermBenchS extends Bench with CirceAutoBenchS with PermBench[String]
class CirceJacksonAutoDeepBenchS extends Bench with CirceAutoBenchS with DeepBench[String]

class CirceJacksonAutoPermBenchB extends Bench with CirceJacksonAutoBenchB with PermBench[ByteBuffer]
class CirceJacksonAutoDeepBenchB extends Bench with CirceJacksonAutoBenchB with DeepBench[ByteBuffer]

class CirceJacksonPermBenchB extends Bench with CirceJacksonBenchB with PermBench[ByteBuffer]
class CirceJacksonDeepBenchB extends Bench with CirceJacksonBenchB with DeepBench[ByteBuffer]

class CircePermBenchS extends Bench with CirceBenchS with PermBench[String]
class CirceDeepBenchS extends Bench with CirceBenchS with DeepBench[String]

class SprayJsonPermBenchS extends Bench with SprayJsonBenchS with PermBench[String]
class SprayJsonDeepBenchS extends Bench with SprayJsonBenchS with DeepBench[String]

class ArgonautPermBenchS extends Bench with ArgonautBenchS with PermBench[String]
class ArgonautDeepBenchS extends Bench with ArgonautBenchS with DeepBench[String]

class UPicklePermBenchS extends Bench with UPickleBenchS with PermBench[String]
class UPickleDeepBenchS extends Bench with UPickleBenchS with DeepBench[String]

class Json4SNativePermBenchS extends Bench with Json4sNativeBenchS with PermBench[String]
class Json4SNativeDeepBenchS extends Bench with Json4sNativeBenchS with DeepBench[String]

class JacksonScalaPermBenchS extends Bench with JacksonScalaBenchS with PermBench[String]
class JacksonScalaDeepBenchS extends Bench with JacksonScalaBenchS with DeepBench[String]

class JacksonScalaPermBenchB extends Bench with JacksonScalaBenchB with PermBench[ByteBuffer]
class JacksonScalaDeepBenchB extends Bench with JacksonScalaBenchB with DeepBench[ByteBuffer]

// class PlayJsonPermBenchS extends PlayJsonBenchS with PermBench
// class PlayJsonDeepBenchS extends PlayJsonBenchS with DeepBench

import State._

trait FooBench[T] {

  def length: Int
  def depth: Int

  protected def encode0(foos: Seq[Foo[Option]]): T

  @Benchmark
  def encode(data: Data): T = encode0(data.get(length, depth))
}

trait PermBench[T] extends FooBench[T] {

  @Param(Array("10", "100"))
  var length: Int = _

  @Param(Array("10", "100"))
  var depth: Int = _
}

trait DeepBench[T] extends FooBench[T] {

  @Param(Array("1"))
  var length: Int = _

  @Param(Array("1000"))
  var depth: Int = _
}

trait CirceAutoBenchS { self: FooBench[String] =>
  import io.circe.generic.auto._
  import io.circe.syntax._

  final def encode0(foos: Seq[Foo[Option]]): String = foos.asJson.noSpaces
}

trait CirceJacksonAutoBenchS { self: FooBench[String] =>
  import io.circe.generic.auto._
  import io.circe.syntax._
  import io.circe.jackson.jacksonPrint

  final def encode0(foos: Seq[Foo[Option]]): String = jacksonPrint(foos.asJson)
}

trait CirceJacksonAutoBenchB { self: FooBench[ByteBuffer] =>
  import io.circe.generic.auto._
  import io.circe.syntax._
  import io.circe.jackson.jacksonPrintByteBuffer

  final def encode0(foos: Seq[Foo[Option]]): ByteBuffer = jacksonPrintByteBuffer(foos.asJson)
}

trait CirceBench {
  import io.circe.{Encoder, Json}
  import io.circe.syntax._

  implicit val encodeFoo: Encoder[Foo[Option]] = Encoder.instance {
    case Foo(i, Some(f)) => Json.obj("i" := Json.fromInt(i), "foo" := f.asJson)
    case Foo(i, _) => Json.obj("i" := Json.fromInt(i), "foo" := Json.Null)
  }
}

trait CirceBenchS extends CirceBench { self: FooBench[String] =>
  import io.circe.syntax._

  final def encode0(foos: Seq[Foo[Option]]): String = foos.asJson.noSpaces
}

trait CirceJacksonBenchS extends CirceBench { self: FooBench[String] =>
  import io.circe.syntax._
  import io.circe.jackson.jacksonPrint

  final def encode0(foos: Seq[Foo[Option]]): String = jacksonPrint(foos.asJson)
}

trait CirceJacksonBenchB extends CirceBench { self: FooBench[ByteBuffer] =>
  import io.circe.syntax._
  import io.circe.jackson.jacksonPrintByteBuffer

  final def encode0(foos: Seq[Foo[Option]]): ByteBuffer = jacksonPrintByteBuffer(foos.asJson)
}

trait SprayJsonBench {
  import spray.json._
  import DefaultJsonProtocol._

  implicit object FooFormat extends JsonFormat[Foo[Option]] {
    def read(json: JsValue): Foo[Option] = ???
    def write(obj: Foo[Option]): JsValue =
      JsObject("i" -> JsNumber(obj.i), "foo" -> obj.foo.toJson)
  }
}

trait SprayJsonBenchS extends SprayJsonBench { self: FooBench[String] =>
  import spray.json._
  import DefaultJsonProtocol._

  final def encode0(foos: Seq[Foo[Option]]): String = foos.toJson.compactPrint
}

trait ArgonautBench {
  import argonaut._, Argonaut._

  implicit val encodeFoo: EncodeJson[Foo[Option]] = EncodeJson[Foo[Option]] {
    case Foo(i, Some(foo)) => Json("i" -> Json.jNumber(i), "foo" -> foo.asJson)
    case Foo(i, _) => Json("i" -> Json.jNumber(i), "foo" -> Json.jNull)
  }
}

trait ArgonautBenchS extends ArgonautBench { self: FooBench[String] =>
  import argonaut._, Argonaut._

  final def encode0(foos: Seq[Foo[Option]]): String = foos.toList.asJson.nospaces
}

trait UPickleBench {
  import upickle.default._

  implicit val fooWriter: Writer[Foo[Option]] = macroW
}

trait UPickleBenchS extends UPickleBench { self: FooBench[String] =>
  import upickle.default._

  final def encode0(foos: Seq[Foo[Option]]): String = write(foos)
}

trait Json4sNativeBench {
  import org.json4s._
  import native.Serialization._

  implicit val noTypeHintsFormats: Formats = formats(NoTypeHints)
}

trait Json4sNativeBenchS extends Json4sNativeBench { self: FooBench[String] =>
  import org.json4s._
  import native.Serialization._

  final def encode0(foos: Seq[Foo[Option]]): String = write(foos)
}

trait JacksonScalaBench {
  import com.fasterxml.jackson.databind.ObjectMapper
  import com.fasterxml.jackson.module.scala.DefaultScalaModule
  import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

  val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)

}

trait JacksonScalaBenchS extends JacksonScalaBench { self: FooBench[String] =>

  final def encode0(foos: Seq[Foo[Option]]): String = mapper.writeValueAsString(foos)
}

trait JacksonScalaBenchB extends JacksonScalaBench { self: FooBench[ByteBuffer] =>

  final def encode0(foos: Seq[Foo[Option]]): ByteBuffer = ByteBuffer.wrap(mapper.writeValueAsBytes(foos))
}

// FIXME: NPE occurred...
trait PlayJsonBench {
  import play.api.libs.json._
  import play.api.libs.functional.syntax._

  implicit val encodeFoo: Writes[Foo[Option]] = (
    (JsPath \ "i").write[Int] and
      (JsPath \ "foo").writeNullable[Foo[Option]]
    )(unlift[Foo[Option], (Int, Option[Foo[Option]])](a => Foo.unapply[Option](a)))
}

trait PlayJsonBenchS extends PlayJsonBench { self: FooBench[String] =>
  import play.api.libs.json._

  final def encode0(foos: Seq[Foo[Option]]): String = Json.stringify(Json.toJson(foos))
}

trait PlayJsonBenchB extends PlayJsonBench { self: FooBench[ByteBuffer] =>
  import play.api.libs.json._

  final def encode0(foos: Seq[Foo[Option]]): ByteBuffer = ByteBuffer.wrap(Json.toBytes(Json.toJson(foos)))
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
