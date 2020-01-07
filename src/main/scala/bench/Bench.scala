package bench

import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

import org.openjdk.jmh.annotations._
import State._

@State(Scope.Benchmark)
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 10, time = 1)
@OutputTimeUnit(TimeUnit.SECONDS)
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
    with JacksonScalaBench
    with Json4sBench
    with Json4sJacksonBench
    with JsoniterScalaBench
    // with PlayJsonBench
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
  var depth: Int = _
}
class Case2 extends Bench with Params {
  @Param(Array("10"))
  var length: Int = _
  @Param(Array("100"))
  var depth: Int = _
}
class Case3 extends Bench with Params {
  @Param(Array("100"))
  var length: Int = _
  @Param(Array("10"))
  var depth: Int = _
}
class Case4 extends Bench with Params {
  @Param(Array("100"))
  var length: Int = _
  @Param(Array("100"))
  var depth: Int = _
}
class Case5 extends Bench with Params {
  @Param(Array("1"))
  var length: Int = _
  @Param(Array("1000"))
  var depth: Int = _
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

  implicit val encodeFooForArgonaut: EncodeJson[Foo[Option]] = EncodeJson[Foo[Option]] {
    case Foo(i, foo) => Json("i" -> Json.jNumber(i), "foo" -> foo.asJson)
  }

  private[this] lazy val rawJson = foos.toList.asJson.nospaces

  @Benchmark
  def decodeArgonaut: Seq[Foo[Option]] = rawJson.decode[List[Foo[Option]]].toOption.get

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

  implicit val encodeFooCirce: Encoder[Foo[Option]] = Encoder.instance {
    case Foo(i, foo) => Json.obj("i" := Json.fromInt(i), "foo" := foo.asJson)
  }

  private[this] lazy val rawJson = foos.asJson.noSpaces

  @Benchmark
  def decodeCirce: Seq[Foo[Option]] = cdecode[Seq[Foo[Option]]](rawJson).getOrElse(???)

  @Benchmark
  def encodeCirce: String = foos.asJson.noSpaces
}

trait CirceAutoBench { self: Params =>
  import io.circe.generic.auto._
  import io.circe.parser.{decode => cdecode}
  import io.circe.syntax._

  private[this] lazy val rawJson = foos.asJson.noSpaces

  @Benchmark
  def decodeCirceAuto: Seq[Foo[Option]] = cdecode[Seq[Foo[Option]]](rawJson).getOrElse(???)

  @Benchmark
  def encodeCirceAuto: String = foos.asJson.noSpaces
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
  def encodeCirceAutoToBytes: Array[Byte] = jacksonPrintByteBuffer(foos.asJson).array()
}

trait JacksonScalaBench { self: Params =>
  import com.fasterxml.jackson.databind.ObjectMapper
  import com.fasterxml.jackson.module.scala.DefaultScalaModule
  import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

  private[this] val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)

  private[this] lazy val rawJson = mapper.writeValueAsString(foos)

  @Benchmark
  def decodeJackson: Seq[Foo[Option]] = mapper.readValue(rawJson, classOf[Seq[Foo[Option]]])

  @Benchmark
  def encodeJackson: String = mapper.writeValueAsString(foos)
}

trait Json4sBench { self: Params =>
  import org.json4s._
  import native.Serialization.{write => nwrite, read => nread}

  // FIXME: MappingException occurs during decoding; I don't know why that happens.
  implicit private[this] val json4sDefaultFormats: Formats = DefaultFormats.preservingEmptyValues

  private[this] lazy val rawJson = nwrite(foos)

  @Benchmark
  def decodeJson4s: Seq[Foo[Option]] = nread(rawJson)

  @Benchmark
  def encodeJson4s: String = nwrite(foos)
}

trait Json4sJacksonBench { self: Params =>
  import org.json4s._
  import jackson.Serialization.{write => swrite, read => sread}

  // FIXME: MappingException occurs during decoding; I don't know why that happens.
  implicit private[this] val json4sDefaultFormats: Formats = DefaultFormats.preservingEmptyValues

  private[this] lazy val rawJson = swrite(foos)

  @Benchmark
  def decodeJson4sJackson: Seq[Foo[Option]] = sread(rawJson)

  @Benchmark
  def encodeJson4sJackson: String = swrite(foos)
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

trait PlayJsonBench { self: Params =>
  import play.api.libs.json._
  import play.api.libs.functional.syntax._

  // FIXME: NPE occurred...
  implicit val encodeFooPlayJson: Writes[Foo[Option]] = (
    ((JsPath \ "i")
      .write[Int])
      .and((JsPath \ "foo").writeNullable[Foo[Option]])
    )(unlift(Foo.unapply[Option]))

  // @Benchmark
  def encodePlayJson: String = Json.stringify(Json.toJson(foos))

  // @Benchmark
  def encodePlayJsonB: ByteBuffer =
    ByteBuffer.wrap(Json.toBytes(Json.toJson(foos)))
}

trait SprayJsonBench { self: Params =>
  import spray.json._
  import DefaultJsonProtocol._

  implicit val fooFormat: JsonFormat[Foo[Option]] = new JsonFormat[Foo[Option]] {
    def read(json: JsValue): Foo[Option] = json match {
      case JsObject(m) =>
        val i = m.get("i").map(_.convertTo[Int]).getOrElse(0)
        val foo = m.get("foo").flatMap {
          case JsNull => None
          case a      => a.convertTo[Option[Foo[Option]]]
        }
        Foo(i, foo)
      case x => throw new Exception(x.toString)
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

  // Note
  //   uPickle encodes Option[_] to `arr` value.
  //   But this benchmark needs handle as nullable value.
  def optionWriter[T: Writer]: Writer[Option[T]] =
    writer[T].comapNulls[Option[T]](_.getOrElse(null.asInstanceOf[T]))
  def optionReader[T: Reader]: Reader[Option[T]] =
    reader[T].map[Option[T]](Option.apply)
  implicit def optionRW[T: Reader: Writer]: ReadWriter[Option[T]] =
    ReadWriter.join(optionReader, optionWriter)

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
