package org.mint.utils

import akka.http.scaladsl.model.{HttpEntity, HttpMethods, HttpRequest, MediaTypes}
import org.mint.json.GenericJsonWriter

object RequestSupport {
  private val prefix = "/api/accounts"

  def insertRequest[T](e: T)(implicit w: GenericJsonWriter[T]): HttpRequest = {
    val entity = HttpEntity(MediaTypes.`application/json`, w.toJsonString(e))
    HttpRequest(uri = prefix, method = HttpMethods.POST, entity = entity)
  }

  def selectAllRequest: HttpRequest =
    HttpRequest(uri = prefix)

  def selectAllRequest(sort: String): HttpRequest =
    HttpRequest(uri = s"$prefix?sort=$sort")

  def existingTypeofAccountsRequest: HttpRequest =
    HttpRequest(uri = s"$prefix/existingtypeofaccounts")

  def updateRequest[T](e: T, id: Int)(implicit w: GenericJsonWriter[T]): HttpRequest = {
    val entity = HttpEntity(MediaTypes.`application/json`, w.toJsonString(e))
    HttpRequest(uri = s"$prefix/$id", method = HttpMethods.PUT, entity = entity)
  }
}
