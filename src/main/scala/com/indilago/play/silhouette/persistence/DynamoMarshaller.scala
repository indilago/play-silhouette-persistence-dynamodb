package com.indilago.play.silhouette.persistence

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import play.api.libs.json._

import scala.reflect.ClassTag

trait DynamoMarshaller[R] {
  def toDynamo(record: R): Map[String, AttributeValue]

  def fromDynamo(attr: Map[String, AttributeValue]): R
}

object DynamoMarshaller {
  def naivejson[A: ClassTag: Format]: DynamoMarshaller[A] = new DynamoMarshaller[A] {
    def toDynamo(record: A): Map[String, AttributeValue] = {
      Json.toJson(record).asInstanceOf[JsObject].value.map {
        case (key, jsVal) => key -> (jsVal match {
          case JsString(str) => new AttributeValue().withS(str)
          case JsNumber(num) => new AttributeValue().withN(num.toString)
          case JsBoolean(bool) => new AttributeValue().withBOOL(bool)
          case x: JsValue => throw new UnsupportedOperationException(s"Currently support Strings, Numbers, Booleans. Got $x")
        })
      }.toMap
    }

    def fromDynamo(attr: Map[String, AttributeValue]): A = {
      JsObject(attr.map {
        case (key, v) => key -> {
          if (v.getS != null) JsString(v.getS)
          else if (v.getN != null) JsNumber(BigDecimal(v.getN))
          else if (v.getB != null) JsBoolean(v.getBOOL)
          else throw new UnsupportedOperationException(s"Currently support Strings, Numbers, Booleans. Got $v")
        }
      }).validate[A].getOrElse(throw new RuntimeException(s"Error casting $attr to ${implicitly[ClassTag[A]].runtimeClass.getSimpleName}"))
    }
  }
}

