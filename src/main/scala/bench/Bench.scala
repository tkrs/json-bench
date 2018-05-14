package bench

import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

import org.openjdk.jmh.annotations._

import State._

@State(Scope.Benchmark)
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 10, time = 2)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(2)
abstract class Bench[T] { self: Params =>

  var foos: Seq[Foo[Option]] = _

  @Setup
  def setup(data: Data): Unit = {
    foos = data.get(length, depth)
  }

  protected def encode0(foos: Seq[Foo[Option]]): T

  @Benchmark
  def encode: T = encode0(foos)
}

trait Params {
  def length: Int
  def depth: Int
}

trait PermBench extends Params {

  @Param(Array("10", "100"))
  var length: Int = _

  @Param(Array("10", "100"))
  var depth: Int = _
}

trait DeepBench extends Params {

  @Param(Array("1"))
  var length: Int = _

  @Param(Array("1000"))
  var depth: Int = _
}

class ArgonautPermBenchS extends ArgonautBenchS with PermBench
class ArgonautDeepBenchS extends ArgonautBenchS with DeepBench

class CirceAutoPermBenchS        extends CirceAutoBenchS with PermBench
class CirceAutoDeepBenchS        extends CirceAutoBenchS with DeepBench
class CirceAutoJacksonPermBenchS extends CirceAutoJacksonBenchS with PermBench
class CirceAutoJacksonDeepBenchS extends CirceAutoJacksonBenchS with DeepBench
class CirceAutoJacksonPermBenchB extends CirceAutoJacksonBenchB with PermBench
class CirceAutoJacksonDeepBenchB extends CirceAutoJacksonBenchB with DeepBench

class CirceCorePermBenchS        extends CirceCoreBenchS with PermBench
class CirceCoreDeepBenchS        extends CirceCoreBenchS with DeepBench
class CirceCoreJacksonPermBenchB extends CirceCoreJacksonBenchB with PermBench
class CirceCoreJacksonDeepBenchB extends CirceCoreJacksonBenchB with DeepBench

class Json4sNativePermBenchS  extends Json4sNativeBenchS with PermBench
class Json4sNativeDeepBenchS  extends Json4sNativeBenchS with DeepBench
class Json4sJacksonPermBenchS extends Json4sJacksonBenchS with PermBench
class Json4sJacksonDeepBenchS extends Json4sJacksonBenchS with DeepBench

class JacksonScalaPermBenchS extends JacksonScalaBenchS with PermBench
class JacksonScalaDeepBenchS extends JacksonScalaBenchS with DeepBench
class JacksonScalaPermBenchB extends JacksonScalaBenchB with PermBench
class JacksonScalaDeepBenchB extends JacksonScalaBenchB with DeepBench

// class PlayJsonPermBenchS extends PlayJsonBenchS with PermBench
// class PlayJsonDeepBenchS extends PlayJsonBenchS with DeepBench
// class PlayJsonPermBenchB extends PlayJsonBenchB with PermBench
// class PlayJsonDeepBenchB extends PlayJsonBenchB with DeepBench

class SprayJsonPermBenchS extends SprayJsonBenchS with PermBench
class SprayJsonDeepBenchS extends SprayJsonBenchS with DeepBench

class UPicklePermBenchS    extends UPickleBenchS with PermBench
class UPickleDeepBenchS    extends UPickleBenchS with DeepBench
class UPickleASTPermBenchS extends UPickleASTBenchS with PermBench
class UPickleASTDeepBenchS extends UPickleASTBenchS with DeepBench

trait ArgonautBench {
  import argonaut._, Argonaut._

  implicit val encodeFoo: EncodeJson[Foo[Option]] = EncodeJson[Foo[Option]] {
    case Foo(i, Some(foo)) => Json("i" -> Json.jNumber(i), "foo" -> foo.asJson)
    case Foo(i, _)         => Json("i" -> Json.jNumber(i), "foo" -> Json.jNull)
  }
}

abstract class ArgonautBenchS extends Bench[String] with ArgonautBench { self: Params =>
  import argonaut._, Argonaut._

  final def encode0(foos: Seq[Foo[Option]]): String = foos.toList.asJson.nospaces
}

abstract class CirceAutoBenchS extends Bench[String] { self: Params =>
  import io.circe.generic.auto._
  import io.circe.syntax._

  final def encode0(foos: Seq[Foo[Option]]): String = foos.asJson.noSpaces
}

abstract class CirceAutoJacksonBenchS extends Bench[String] { self: Params =>
  import io.circe.generic.auto._
  import io.circe.syntax._
  import io.circe.jackson.jacksonPrint

  final def encode0(foos: Seq[Foo[Option]]): String = jacksonPrint(foos.asJson)
}

abstract class CirceAutoJacksonBenchB extends Bench[ByteBuffer] { self: Params =>
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
    case Foo(i, _)       => Json.obj("i" := Json.fromInt(i), "foo" := Json.Null)
  }
}

abstract class CirceCoreBenchS extends Bench[String] with CirceBench { self: Params =>
  import io.circe.syntax._

  final def encode0(foos: Seq[Foo[Option]]): String = foos.asJson.noSpaces
}

abstract class CirceJacksonBenchS extends Bench[String] with CirceBench { self: Params =>
  import io.circe.syntax._
  import io.circe.jackson.jacksonPrint

  final def encode0(foos: Seq[Foo[Option]]): String = jacksonPrint(foos.asJson)
}

abstract class CirceCoreJacksonBenchB extends Bench[ByteBuffer] with CirceBench { self: Params =>
  import io.circe.syntax._
  import io.circe.jackson.jacksonPrintByteBuffer

  final def encode0(foos: Seq[Foo[Option]]): ByteBuffer = jacksonPrintByteBuffer(foos.asJson)
}

trait JacksonScalaBench {
  import com.fasterxml.jackson.databind.ObjectMapper
  import com.fasterxml.jackson.module.scala.DefaultScalaModule
  import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

  val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)
}

abstract class JacksonScalaBenchS extends Bench[String] with JacksonScalaBench { self: Params =>

  final def encode0(foos: Seq[Foo[Option]]): String = mapper.writeValueAsString(foos)
}

abstract class JacksonScalaBenchB extends Bench[ByteBuffer] with JacksonScalaBench { self: Params =>

  final def encode0(foos: Seq[Foo[Option]]): ByteBuffer =
    ByteBuffer.wrap(mapper.writeValueAsBytes(foos))
}

trait Json4sNativeBench {
  import org.json4s._
  import native.Serialization._

  implicit val noTypeHintsFormats: Formats = formats(NoTypeHints)
}

abstract class Json4sNativeBenchS extends Bench[String] with Json4sNativeBench { self: Params =>
  import org.json4s._
  import native.Serialization._

  final def encode0(foos: Seq[Foo[Option]]): String = write(foos)
}

abstract class Json4sJacksonBenchS extends Bench[String] with Json4sNativeBench { self: Params =>
  import org.json4s._
  import jackson.Serialization._

  final def encode0(foos: Seq[Foo[Option]]): String = write(foos)
}

// FIXME: NPE occurred...
trait PlayJsonBench {
  import play.api.libs.json._
  import play.api.libs.functional.syntax._

  implicit val encodeFoo: Writes[Foo[Option]] = (
    (JsPath \ "i").write[Int] and
      (JsPath \ "foo").writeNullable[Foo[Option]]
  )(unlift(Foo.unapply[Option]))
}

abstract class PlayJsonBenchS extends Bench[String] with PlayJsonBench { self: Params =>
  import play.api.libs.json._

  final def encode0(foos: Seq[Foo[Option]]): String = Json.stringify(Json.toJson(foos))
}

abstract class PlayJsonBenchB extends Bench[ByteBuffer] with PlayJsonBench { self: Params =>
  import play.api.libs.json._

  final def encode0(foos: Seq[Foo[Option]]): ByteBuffer =
    ByteBuffer.wrap(Json.toBytes(Json.toJson(foos)))
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

abstract class SprayJsonBenchS extends Bench[String] with SprayJsonBench { self: Params =>
  import spray.json._
  import DefaultJsonProtocol._

  final def encode0(foos: Seq[Foo[Option]]): String = foos.toJson.compactPrint
}

trait UPickleBench {
  import upickle.default._

  implicit val fooWriter: Writer[Foo[Option]] = macroW
}

abstract class UPickleBenchS extends Bench[String] with UPickleBench { self: Params =>
  import upickle.default._

  final def encode0(foos: Seq[Foo[Option]]): String = write(foos)
}

abstract class UPickleASTBenchS extends Bench[String] with UPickleBench { self: Params =>
  import upickle.default._

  final def encode0(foos: Seq[Foo[Option]]): String = writeJs(foos).render()
}

object State {

  case class Foo[F[_]](i: Int, foo: F[Foo[F]])

  private def genFoo(i: Int, foo: Foo[Option]): Foo[Option] = {
    if (i == 0) foo
    else genFoo(i - 1, Foo(i, Some(foo)))
  }

  @State(Scope.Benchmark)
  class Data {

    def get(length: Int, depth: Int): Seq[Foo[Option]] =
      Iterator
        .continually(genFoo(depth - 1, Foo(depth, Option.empty[Foo[Option]])))
        .take(length)
        .toList
  }
}
