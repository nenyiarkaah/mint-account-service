package org.mint.json

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.mint.models.{Account, AccountTypes, CommandResult, ImportStatus}
import spray.json.{DeserializationException, JsString, JsValue, RootJsonFormat}
import spray.json.DefaultJsonProtocol._
import spray.json._

trait SprayJsonFormat extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val accountFormat: RootJsonFormat[Account] = jsonFormat6(Account)
  implicit val accountTypesFormat: RootJsonFormat[AccountTypes] = jsonFormat1(AccountTypes)
  implicit val commandResultFormat: RootJsonFormat[CommandResult] = jsonFormat1(CommandResult)
  implicit val importStatusFormat: RootJsonFormat[ImportStatus] = jsonFormat2(ImportStatus)

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

  def genericJsonWriter[T: RootJsonFormat]: GenericJsonWriter[T] = (e: T) => spray.json.jsonWriter[T].write(e).toString()

  implicit val genericAccountWriter: GenericJsonWriter[Account] = genericJsonWriter[Account]
  implicit val genericAccountTypesWriter: GenericJsonWriter[AccountTypes] = genericJsonWriter[AccountTypes]
  implicit val genericCommandResultWriter: GenericJsonWriter[CommandResult] = genericJsonWriter[CommandResult]
  implicit val genericImportStatusWriter: GenericJsonWriter[ImportStatus] = genericJsonWriter[ImportStatus]
}

object SprayJsonFormat extends SprayJsonFormat
