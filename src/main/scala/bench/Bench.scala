package bench

import org.openjdk.jmh.annotations._
import State._

import scala.concurrent.duration._

@State(Scope.Benchmark)
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 10, time = 1)
@OutputTimeUnit(SECONDS)
@Fork(
  value = 2,
  jvmArgs = Array(
    "-server",
    "-Xss2m",
    "-Xms2g",
    "-Xmx2g",
    "-XX:NewSize=1g",
    "-XX:MaxNewSize=1g",
    "-XX:InitialCodeCacheSize=512m",
    "-XX:ReservedCodeCacheSize=512m",
    "-XX:+UseParallelGC",
    "-XX:-UseBiasedLocking",
    "-XX:+AlwaysPreTouch"
  )
)
abstract class Bench
    extends ArgonautBench
    with CirceAutoBench
    with CirceBench
    with CirceJacksonAutoBench
    with JsoniterScalaBench
    with SprayJsonBench
    with UPickleBench
    with Params

trait Params {

  var foos: Seq[Foo[Option]] = _
  def length: Int
  def depth: Int

  @Setup
  def setup(data: Data): Unit =
    foos = data.get(length, depth)
}

class Case1 extends Bench with Params {
  @Param(Array("10"))
  var length: Int = _
  @Param(Array("10"))
  var depth: Int  = _
}
class Case2 extends Bench with Params {
  @Param(Array("10"))
  var length: Int = _
  @Param(Array("100"))
  var depth: Int  = _
}
class Case3 extends Bench with Params {
  @Param(Array("100"))
  var length: Int = _
  @Param(Array("10"))
  var depth: Int  = _
}
class Case4 extends Bench with Params {
  @Param(Array("100"))
  var length: Int = _
  @Param(Array("100"))
  var depth: Int  = _
}
class Case5 extends Bench with Params {
  @Param(Array("1"))
  var length: Int = _
  @Param(Array("1000"))
  var depth: Int  = _
}

trait ArgonautBench { self: Params =>
  import argonaut._, Argonaut._

  implicit val decodeFooForArgonaut: DecodeJson[Foo[Option]] = DecodeJson[Foo[Option]] { hc =>
    val x = for {
      i   <- hc.downField("i").focus.flatMap(_.number.flatMap(_.toInt))
      foo <- hc.downField("foo").focus.flatMap(_.as[Option[Foo[Option]]].toOption)
    } yield Foo(i, foo)
    x match {
      case Some(f) => DecodeResult.ok(f)
      case None    => DecodeResult.fail[Foo[Option]]("Foo[Option]", hc.history)
    }
  }

  implicit val encodeFooForArgonaut: EncodeJson[Foo[Option]] = EncodeJson[Foo[Option]] { case Foo(i, foo) =>
    Json("i" -> Json.jNumber(i), "foo" -> foo.asJson)
  }

  private[this] lazy val rawJson = foos.toList.asJson.nospaces

  @Benchmark
  def decodeArgonaut: Either[Either[String, (String, CursorHistory)], Seq[Foo[Option]]] =
    rawJson.decode[List[Foo[Option]]]

  @Benchmark
  def encodeArgonaut: String = foos.toList.asJson.nospaces
}

trait CirceBench { self: Params =>
  import io.circe.{Decoder, DecodingFailure, Encoder, Json}
  import io.circe.parser.{decode => cdecode}
  import io.circe.syntax._

  implicit val decodeFooCirce: Decoder[Foo[Option]] = Decoder.instance { hc =>
    val x = for {
      i   <- hc.downField("i").focus.flatMap(_.asNumber.flatMap(_.toInt))
      foo <- hc.downField("foo").focus.flatMap(_.as[Option[Foo[Option]]].toOption)
    } yield Foo(i, foo)
    x match {
      case Some(f) => Right(f)
      case None    => Left(DecodingFailure("Foo[Option]", hc.history))
    }
  }

  implicit val encodeFooCirce: Encoder[Foo[Option]] = Encoder.instance { case Foo(i, foo) =>
    Json.obj("i" := Json.fromInt(i), "foo" := foo.asJson)
  }

  private[this] lazy val rawJson = foos.asJson.noSpaces

  @Benchmark
  def decodeCirce: Either[Throwable, Seq[Foo[Option]]] = cdecode[Seq[Foo[Option]]](rawJson)

  @Benchmark
  def encodeCirce: String = foos.asJson.dropNullValues.noSpaces
}

trait CirceAutoBench { self: Params =>
  import io.circe.generic.auto._
  import io.circe.parser.{decode => cdecode}
  import io.circe.syntax._

  private[this] lazy val rawJson = foos.asJson.noSpaces

  @Benchmark
  def decodeCirceAuto: Either[Throwable, Seq[Foo[Option]]] = cdecode[Seq[Foo[Option]]](rawJson)

  @Benchmark
  def encodeCirceAuto: String = foos.asJson.dropNullValues.noSpaces
}

trait CirceJacksonAutoBench { self: Params =>
  import io.circe.generic.auto._
  import io.circe.syntax._
  import io.circe.jackson._

  private[this] lazy val rawJson = jacksonPrintByteBuffer(foos.asJson)

  @Benchmark
  def decodeCirceAutoFromBytes: Seq[Foo[Option]] =
    decodeByteArray[Seq[Foo[Option]]](rawJson.array()).getOrElse(???)

  @Benchmark
  def encodeCirceAutoToBytes: Array[Byte] = jacksonPrintByteBuffer(foos.asJson.dropNullValues).array()
}

trait JsoniterScalaBench { self: Params =>
  import com.github.plokhotnyuk.jsoniter_scala.macros._
  import com.github.plokhotnyuk.jsoniter_scala.core._

  implicit val jsoniterScalaCodec: JsonValueCodec[Seq[Foo[Option]]] =
    JsonCodecMaker.make[Seq[Foo[Option]]](CodecMakerConfig.withAllowRecursiveTypes(true))

  val jsoniterReaderConfig = ReaderConfig.withPreferredBufSize(1024 * 1024) // 1Mb
  val jsoniterWriteConfig  = WriterConfig.withPreferredBufSize(1024 * 1024) // 1Mb

  private[this] lazy val rawJson = writeToArray(foos)

  @Benchmark
  def decodeJsoniterScalaFromBytes: Seq[Foo[Option]] = readFromArray(rawJson, jsoniterReaderConfig)

  @Benchmark
  def encodeJsoniterScalaToBytes: Array[Byte] = writeToArray(foos, jsoniterWriteConfig)
}

trait SprayJsonBench { self: Params =>
  import spray.json._
  import DefaultJsonProtocol._

  implicit val formatFooSprayJson: JsonFormat[Foo[Option]] = new JsonFormat[Foo[Option]] {
    def read(json: JsValue): Foo[Option] = json match {
      case JsObject(m) =>
        val i   = m.get("i").map(_.convertTo[Int]).getOrElse(0)
        val foo = m.get("foo").flatMap {
          case JsNull => None
          case a      => a.convertTo[Option[Foo[Option]]]
        }
        Foo(i, foo)
      case x           =>
        throw new Exception(x.toString)
    }
    def write(obj: Foo[Option]): JsValue =
      JsObject("i" -> JsNumber(obj.i), "foo" -> obj.foo.toJson)
  }

  private[this] lazy val rawJson = foos.toJson.compactPrint

  @Benchmark
  final def decodeSprayJson: Seq[Foo[Option]] = rawJson.parseJson.convertTo[Seq[Foo[Option]]]

  @Benchmark
  final def encodeSprayJson: String = foos.toJson.compactPrint
}

trait UPickleBench { self: Params =>
  import upickle.default._

  implicit val fooUPickleRW: ReadWriter[Foo[Option]] = macroRW[Foo[Option]]

  private[this] lazy val rawJson = write(foos)

  @Benchmark
  def decodeUPickle: Seq[Foo[Option]] = read[Seq[Foo[Option]]](rawJson)

  @Benchmark
  def encodeUPickle: String = write(foos)
}

object State {

  case class Foo[F[_]](i: Int, foo: F[Foo[F]])

  private def genFoo(i: Int, foo: Foo[Option]): Foo[Option] =
    if (i == 0) foo
    else genFoo(i - 1, Foo(i, Some(foo)))

  @State(Scope.Benchmark)
  class Data {

    def get(length: Int, depth: Int): Seq[Foo[Option]] =
      Iterator
        .continually(genFoo(depth - 1, Foo(depth, Option.empty[Foo[Option]])))
        .take(length)
        .toList
  }
}
