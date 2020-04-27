package org.mint.json

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.mint.models.{Account, Accounts, CommandResult}
import spray.json.{DeserializationException, JsString, JsValue, RootJsonFormat}
import spray.json.DefaultJsonProtocol._

trait SprayJsonFormat extends SprayJsonSupport {
  implicit val accountFormat: RootJsonFormat[Account] = jsonFormat7(Account)
  implicit val accountsFormat: RootJsonFormat[Accounts] = jsonFormat1(Accounts)
  implicit val commandResultFormat: RootJsonFormat[CommandResult] = jsonFormat1(CommandResult)

  // Generic Enumeration formatter
  implicit def enumFormat[T <: Enumeration](implicit enu: T): RootJsonFormat[T#Value] =
    new RootJsonFormat[T#Value] {
      def write(obj: T#Value): JsValue = JsString(obj.toString)
      def read(json: JsValue): T#Value = {
        json match {
          case JsString(txt) => enu.withName(txt)
          case somethingElse =>
            throw DeserializationException(s"Expected a value from enum $enu instead of $somethingElse")
        }
      }
    }

  def genericJsonWriter[T: RootJsonFormat]: GenericJsonWriter[T] =
    (e: T) => spray.json.jsonWriter[T].write(e).toString()

  implicit val genericAccountWriter: GenericJsonWriter[Account] = genericJsonWriter[Account]
  implicit val genericAccountsWriter: GenericJsonWriter[Accounts] = genericJsonWriter[Accounts]
  implicit val genericCommandResultWriter: GenericJsonWriter[CommandResult] = genericJsonWriter[CommandResult]
}

object SprayJsonFormat extends SprayJsonFormat
