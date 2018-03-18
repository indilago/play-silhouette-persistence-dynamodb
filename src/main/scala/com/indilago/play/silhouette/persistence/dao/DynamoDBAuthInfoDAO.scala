/**
 * Copyright 2018 Indilago Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.indilago.play.silhouette.persistence.dao

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient
import com.indilago.play.silhouette.persistence.{ DynamoDBClient, DynamoMarshaller }
import com.mohiva.play.silhouette.api.{ AuthInfo, LoginInfo }
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import play.api.Configuration
import play.api.libs.json._

import scala.concurrent.{ ExecutionContext, Future }
import scala.reflect.ClassTag

/**
 * An implementation of the auth info DAO which stores the data in a DynamoDB database.
 *
 * @param client DynamoDB Client.
 * @param config The Play configuration.
 * @tparam A The type of the auth info to store.
 */
class DynamoDBAuthInfoDAO[A <: AuthInfo: ClassTag: Format](
  client: DynamoDBClient[LoginInfo, A],
  config: Configuration
) extends DelegableAuthInfoDAO[A] {

  /**
   * The name of the auth info to store.
   */
  private val authInfoName = implicitly[ClassTag[A]].runtimeClass.getSimpleName

  /**
   * The name of the table in which to store the auth info.
   */
  private val tableName = config.getOptional[String](s"silhouette.persistence.dynamodb.table.$authInfoName")
    .getOrElse(s"authinfo-$authInfoName")

  /**
   * Finds the auth info which is linked with the specified login info.
   *
   * @param loginInfo The linked login info.
   * @return The retrieved auth info or None if no auth info could be retrieved for the given login info.
   */
  def find(loginInfo: LoginInfo): Future[Option[A]] = {
    client.find(tableName, loginInfo)
  }

  /**
   * Adds new auth info for the given login info.
   *
   * @param loginInfo The login info for which the auth info should be added.
   * @param authInfo  The auth info to add.
   * @return The added auth info.
   */
  def add(loginInfo: LoginInfo, authInfo: A): Future[A] = {
    client.put(tableName, loginInfo, authInfo)
  }

  /**
   * Updates the auth info for the given login info.
   *
   * @param loginInfo The login info for which the auth info should be updated.
   * @param authInfo  The auth info to update.
   * @return The updated auth info.
   */
  def update(loginInfo: LoginInfo, authInfo: A): Future[A] = {
    client.put(tableName, loginInfo, authInfo)
  }

  /**
   * Saves the auth info for the given login info.
   *
   * This method either adds the auth info if it doesn't exists or it updates the auth info
   * if it already exists.
   *
   * @param loginInfo The login info for which the auth info should be saved.
   * @param authInfo  The auth info to save.
   * @return The saved auth info.
   */
  def save(loginInfo: LoginInfo, authInfo: A): Future[A] = {
    client.put(tableName, loginInfo, authInfo)
  }

  /**
   * Removes the auth info for the given login info.
   *
   * @param loginInfo The login info for which the auth info should be removed.
   * @return A future to wait for the process to be completed.
   */
  def remove(loginInfo: LoginInfo): Future[Unit] = {
    client.delete(tableName, loginInfo)
  }
}

object DynamoDBAuthInfoDAO {
  def apply[A <: AuthInfo: ClassTag: Format](
    client: AmazonDynamoDBAsyncClient,
    config: Configuration
  )(implicit ec: ExecutionContext): DynamoDBAuthInfoDAO[A] = {

    implicit val keyMarshaller: DynamoMarshaller[LoginInfo] =
      DynamoMarshaller.naivejson[LoginInfo]

    implicit val recordMarshaller: DynamoMarshaller[A] =
      DynamoMarshaller.naivejson[A]

    new DynamoDBAuthInfoDAO[A](new DynamoDBClient[LoginInfo, A](client), config)
  }
}
