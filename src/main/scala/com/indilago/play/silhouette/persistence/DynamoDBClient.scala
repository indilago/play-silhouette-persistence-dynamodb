package com.indilago.play.silhouette.persistence

import com.amazonaws.AmazonWebServiceRequest
import com.amazonaws.handlers.AsyncHandler
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient
import com.amazonaws.services.dynamodbv2.model._

import scala.collection.JavaConverters._
import scala.concurrent.{ ExecutionContext, Future, Promise }

class DynamoDBClient[K, R](
  client: AmazonDynamoDBAsyncClient
)(implicit ec: ExecutionContext, km: DynamoMarshaller[K], rm: DynamoMarshaller[R]) {

  type JavaRecord = java.util.Map[String, AttributeValue]
  type ScalaRecord = Map[String, AttributeValue]

  protected def exec[Req <: AmazonWebServiceRequest, Res](handler: AsyncHandler[Req, Res] => Unit): Future[Res] = {
    val promise = Promise[Res]

    handler(new AsyncHandler[Req, Res] {
      override def onError(exception: Exception): Unit = promise.failure(exception)

      override def onSuccess(request: Req, response: Res): Unit = promise.success(response)
    })

    promise.future
  }

  protected def toOption[Res](extract: (Res => JavaRecord))(result: Res): Option[ScalaRecord] =
    extract(result) match {
      case null => None
      case res => Some(res.asScala.toMap)
    }

  protected def fromDynamo(record: Option[ScalaRecord]): Option[R] =
    record.map(rm.fromDynamo)

  def find(tableName: String, key: K): Future[Option[R]] = {
    val keyD = km.toDynamo(key).asJava

    exec[GetItemRequest, GetItemResult] { handler =>
      client.getItemAsync(tableName, keyD, handler)
    }
      .map(toOption(_.getItem))
      .map(fromDynamo)
  }

  def put(tableName: String, key: K, record: R): Future[R] = {
    val attr = km.toDynamo(key) ++ rm.toDynamo(record)

    exec[PutItemRequest, PutItemResult] { handler =>
      client.putItemAsync(tableName, attr.asJava, handler)
    }
      .map(result => rm.fromDynamo(result.getAttributes.asScala.toMap))
  }

  def delete(tableName: String, key: K): Future[Unit] = {
    exec[DeleteItemRequest, DeleteItemResult] { handler =>
      client.deleteItemAsync(tableName, km.toDynamo(key).asJava, handler)
    }
      .map(_ => {})
  }
}
