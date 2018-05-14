package bench

import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

import org.openjdk.jmh.annotations._

import State._

@State(Scope.Benchmark)
trait Bench { self: Params =>

  var foos: Seq[Foo[Option]] = _

  @Setup
  def setup(data: Data): Unit = {
    foos = data.get(length, depth)
  }
}

trait Params {
  def length: Int
  def depth: Int
}

trait PermBenchParams extends Params {

  @Param(Array("10", "100"))
  var length: Int = _

  @Param(Array("10", "100"))
  var depth: Int = _
}

trait DeepBenchParams extends Params {

  @Param(Array("1"))
  var length: Int = _

  @Param(Array("1000"))
  var depth: Int = _
}

@State(Scope.Benchmark)
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 10, time = 2)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(2)
abstract class AllBench
    extends ArgonautBench
    with CirceAutoBench
    with CirceManualBench
    with JacksonScalaBench
    with Json4sBench
    // with PlayJsonBench
    with SprayJsonBench
    with UPickleBench { self: Params =>
}

class PermBench extends AllBench with PermBenchParams
class DeepBench extends AllBench with DeepBenchParams

trait ArgonautBench extends Bench { self: Params =>
  import argonaut._, Argonaut._

  implicit val encodeFooForArgonaut: EncodeJson[Foo[Option]] = EncodeJson[Foo[Option]] {
    case Foo(i, Some(foo)) => Json("i" -> Json.jNumber(i), "foo" -> foo.asJson)
    case Foo(i, _)         => Json("i" -> Json.jNumber(i), "foo" -> Json.jNull)
  }

  @Benchmark
  def encodeArgonaut: String = foos.toList.asJson.nospaces
}

trait CirceAutoBench extends Bench { self: Params =>
  import io.circe.generic.auto._
  import io.circe.syntax._
  import io.circe.jackson._

  @Benchmark
  def encodeCirceAuto: String = foos.asJson.noSpaces

  @Benchmark
  def encodeCirceAutoJackson: String = jacksonPrint(foos.asJson)

  @Benchmark
  final def encodeCirceAutoJacksonB: ByteBuffer = jacksonPrintByteBuffer(foos.asJson)
}

trait CirceManualBench extends Bench { self: Params =>
  import io.circe.{Encoder, Json}
  import io.circe.syntax._
  import io.circe.jackson._

  implicit val encodeFooCirce: Encoder[Foo[Option]] = Encoder.instance {
    case Foo(i, Some(f)) => Json.obj("i" := Json.fromInt(i), "foo" := f.asJson)
    case Foo(i, _)       => Json.obj("i" := Json.fromInt(i), "foo" := Json.Null)
  }

  @Benchmark
  def encodeCirceManual: String = foos.asJson.noSpaces

  @Benchmark
  def encodeCirceManualJackson: String = jacksonPrint(foos.asJson)

  @Benchmark
  def encodeCirceManualJacksonB: ByteBuffer = jacksonPrintByteBuffer(foos.asJson)
}

trait JacksonScalaBench extends Bench { self: Params =>
  import com.fasterxml.jackson.databind.ObjectMapper
  import com.fasterxml.jackson.module.scala.DefaultScalaModule
  import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

  val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)

  @Benchmark
  def encodeJackson: String = mapper.writeValueAsString(foos)

  @Benchmark
  def encodeJacksonB: ByteBuffer =
    ByteBuffer.wrap(mapper.writeValueAsBytes(foos))
}

trait Json4sBench extends Bench { self: Params =>
  import org.json4s._
  import native.Serialization.{write => nwrite, formats}
  import jackson.Serialization.{write => swrite}

  implicit val noTypeHintsFormats: Formats = formats(NoTypeHints)

  @Benchmark
  def encodeJson4sNative: String = nwrite(foos)

  @Benchmark
  def encodeJson4sJackson: String = swrite(foos)
}

// FIXME: NPE occurred...
trait PlayJsonBench extends Bench { self: Params =>
  import play.api.libs.json._
  import play.api.libs.functional.syntax._

  implicit val encodeFooPlayJson: Writes[Foo[Option]] = (
    (JsPath \ "i").write[Int] and
      (JsPath \ "foo").writeNullable[Foo[Option]]
  )(unlift(Foo.unapply[Option]))

  // @Benchmark
  def encodePlayJson: String = Json.stringify(Json.toJson(foos))

  // @Benchmark
  def encodePlayJsonB: ByteBuffer =
    ByteBuffer.wrap(Json.toBytes(Json.toJson(foos)))
}

trait SprayJsonBench extends Bench { self: Params =>
  import spray.json._
  import DefaultJsonProtocol._

  implicit object FooFormat extends JsonFormat[Foo[Option]] {
    def read(json: JsValue): Foo[Option] = ???
    def write(obj: Foo[Option]): JsValue =
      JsObject("i" -> JsNumber(obj.i), "foo" -> obj.foo.toJson)
  }

  @Benchmark
  final def encodeSprayJson: String = foos.toJson.compactPrint
}

trait UPickleBench extends Bench { self: Params =>
  import upickle.default._

  implicit val fooWriter: Writer[Foo[Option]] = macroW

  @Benchmark
  def encodeUPickle: String = write(foos)

  @Benchmark
  def encodeUPickleAst: String = writeJs(foos).render()
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
