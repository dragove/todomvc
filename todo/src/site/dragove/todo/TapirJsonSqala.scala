package site.dragove.todo

import scala.util.{Failure, Success, Try}
import sttp.tapir.*
import sttp.tapir.Codec.JsonCodec
import sttp.tapir.DecodeResult.Error.JsonDecodeException
import sttp.tapir.DecodeResult.{Error, Value}
import sqala.data.json.*

object TapirJsonSqala:
  def jsonBody[T: {JsonDecoder, JsonEncoder, Schema}]
      : EndpointIO.Body[String, T] = stringBodyUtf8AnyFormat(readWriterCodec[T])

  def jsonBodyWithRaw[T: {JsonDecoder, JsonEncoder, Schema}]
      : EndpointIO.Body[String, (String, T)] = stringBodyUtf8AnyFormat(
    implicitly[JsonCodec[(String, T)]]
  )

  implicit def readWriterCodec[T: {JsonDecoder, JsonEncoder, Schema}]
      : JsonCodec[T] =
    Codec.json[T] { s =>
      Try(fromJson[T](s)) match
        case Success(v) => Value(v)
        case Failure(e) => Error(s, JsonDecodeException(errors = List.empty, e))
    } { t => t.toJson }
