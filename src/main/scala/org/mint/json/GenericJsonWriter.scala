package org.mint.json

trait GenericJsonWriter[T] {
  def toJsonString(e: T): String
}
