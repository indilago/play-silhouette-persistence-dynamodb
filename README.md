Silhouette Persistence DynamoDB [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.indilago/play-silhouette-persistence-dynamodb_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.indilago/play-silhouette-persistence-dynamodb_2.11) [![Build Status](https://travis-ci.org/indilago/play-silhouette-persistence-dynamodb.png)](https://travis-ci.org/indilago/play-silhouette-persistence-dynamodb) [![Coverage Status](https://coveralls.io/repos/indilago/play-silhouette-persistence-dynamodb/badge.svg?branch=master&service=github)](https://coveralls.io/github/indilago/play-silhouette-persistence-dynamodb?branch=master)
==========

An implementation of the [Silhouette] persistence layer using [DynamoDB].

## Usage

In your project/Build.scala:

```scala
libraryDependencies ++= Seq(
  "com.indilago" %% "play-silhouette-persistence-dynamodb" % "0.1"
)
```

An instance of the DAO can be created as follow:

```scala
implicit lazy val format = Json.format[OAuth1Info]
val dao = DynamoDBAuthInfoDAO[OAuth1Info](dynamoClient, config)
```

The Json format is needed to serialize the auth info data into Json. It will be passed implicitly to the DAO instance.

To provide bindings for Guice, you should implement a provider for every auth info type:

```scala
/**
 * Provides implementation for the OAuth1 auth info DAO.
 *
 * @param dynamoClient DynamoDB Client
 * @param config Play configuration.
 * @return Implementation of the delegable OAuth1 auth info DAO.
 */
@Provides
def provideOAuth1InfoDAO(dynamoClient: AmazonDynamoDBAsyncClient, config: Configuration): DelegableAuthInfoDAO[OAuth1Info] = {
  implicit lazy val format = Json.format[OAuth1Info]
  DynamoDBAuthInfoDAO[OAuth1Info](dynamoClient, config)
}
```

## Configuration

The DynamoDB table name in which to store auth info data can be specified in configuration in the form `silhouette.persistence.dynamodb.table.[AuthInfo]`:

```
silhouette {
  persistence.dynamodb.table {
    OAuth1Info = "authinfo-oauth1"
    OAuth2Info = "authinfo-oauth2"
    OpenIDInfo = "authinfo-openid"
    PasswordInfo = "authinfo-password"
  }
}
```

If no configuration can be found, table name will be assumed as the name of the auth info class prefixed with `authinfo-`.
So for the `OAuth1Info` type, it uses the table name `authinfo-OAuth1Info`.

## License

The code is licensed under [Apache License v2.0] and the documentation under [CC BY 3.0].

[Silhouette]: https://www.silhouette.rocks/
[DynamoDB]: https://aws.amazon.com/dynamodb/
[Apache License v2.0]: https://www.apache.org/licenses/LICENSE-2.0
[CC BY 3.0]: https://creativecommons.org/licenses/by/3.0/
