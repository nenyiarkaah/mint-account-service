package org.mint.utils

import akka.http.scaladsl.model.{HttpEntity, HttpMethods, HttpRequest, MediaTypes}
import org.mint.json.GenericJsonWriter

object RequestSupport {
  private val prefix = "/api/accounts"

  def insertRequest[T](e: T)(implicit w: GenericJsonWriter[T]): HttpRequest = {
    val entity = HttpEntity(MediaTypes.`application/json`, w.toJsonString(e))
    HttpRequest(uri = prefix, method = HttpMethods.POST, entity = entity)
  }
}
