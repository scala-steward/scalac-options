import io.circe.derivation.deriveCodec
import io.circe.{Codec, Encoder}
import sjsonnew.BasicJsonProtocol.{flatUnionFormat2, isoStringFormat}
import sjsonnew.{IsoString, JsonFormat}


sealed trait FlagSegment

object FlagSegment {
  case class Literal(text: String) extends FlagSegment

  case class Parameter(name: String) extends FlagSegment

  implicit val literalIsoString: IsoString[Literal] =
    IsoString.iso(_.text, Literal)
  implicit val parameterIsoString: IsoString[Parameter] =
    IsoString.iso(_.name, Parameter)
  implicit val flagSegmentFormat: JsonFormat[FlagSegment] =
    flatUnionFormat2[FlagSegment, Literal, Parameter]

  implicit val codecLiteral: Codec[Literal] =
    deriveCodec {
      case "text" => "lit"
      case s      => s
    }

  implicit val codecParameter: Codec[Parameter] = deriveCodec {
    case "name" => "param"
    case s      => s
  }

  implicit val codec: Codec[FlagSegment] = Codec.from(
    codecLiteral.or(codecParameter.map(identity)),
    Encoder.instance {
      case literal: Literal     => codecLiteral(literal)
      case parameter: Parameter => codecParameter(parameter)
    }
  )
}
