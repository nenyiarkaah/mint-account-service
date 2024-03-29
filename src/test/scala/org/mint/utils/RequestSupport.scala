package org.mint.unit.utils

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

  def selectByRequest(id: Int): HttpRequest =
    HttpRequest(uri = s"$prefix/$id")

  def deleteRequest(id: Int): HttpRequest =
    HttpRequest(uri = s"$prefix/$id", method = HttpMethods.DELETE)

  def existingTypeofAccountsRequest: HttpRequest =
    HttpRequest(uri = s"$prefix/existingtypeofaccounts")

  def isConfiguredForImportsRequest(id: Int): HttpRequest =
    HttpRequest(uri = s"$prefix/isconfiguredforimports?id=$id", method = HttpMethods.GET)

  def updateRequest[T](e: T, id: Int)(implicit w: GenericJsonWriter[T]): HttpRequest = {
    val entity = HttpEntity(MediaTypes.`application/json`, w.toJsonString(e))
    HttpRequest(uri = s"$prefix/$id", method = HttpMethods.PUT, entity = entity)
  }
  def healthRequest: HttpRequest =
    HttpRequest(uri = s"$prefix/health", method = HttpMethods.GET)
}
