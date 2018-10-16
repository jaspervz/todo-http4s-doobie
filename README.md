# ms-contact
A sample project of a microservice using [http4s](http://http4s.org/), [doobie](http://tpolecat.github.io/doobie/),
and [circe](https://github.com/circe/circe).

The microservice allows CRUD of Contact items with a description and an importance (high, medium, low).

## End points
The end points are:

Method | Url            | Description
------ | -------------- | -----------
GET    | /contacts      | Returns all Contacts.
GET    | /contacts/{id} | Returns the Contact for the specified id, 404 when no Contact present with this id.
POST   | /contacts      | Creates a Contact, give as body JSON with the description and importance, returns a 201 with the created Contact.
PUT    | /contacts/{id} | Updates an existing Contact, give as body JSON with the description and importance, returns a 200 with the updated Contact when a Contact is present with the specified id, 404 otherwise.
DELETE | /contacts/{id} | Deletes the Contact with the specified Contact, 404 when no Contact present with this id.

Here are some examples on how to use the microservice with curl, assuming it runs on the default port 8080:

Create a Contact:
```curl -X POST --header "Content-Type: application/json" --data '{"description": "my Contact", "importance": "high"}' http://localhost:8080/contacts```

Get all Contacts:
```curl http://localhost:8080/contacts```

Get a single Contact (assuming the id of the Contact is 1):
```curl http://localhost:8080/contacts/1```

Update a Contact (assuming the id of the Contact is 1):
```curl -X PUT --header "Content-Type: application/json" --data '{"description": "my Contact", "importance": "low"}' http://localhost:8080/contacts/1```

Delete a Contact (assuming the id of the Contact is 1):
```curl -X DELETE http://localhost:8080/contacts/1```

## http4s
[http4s](http://http4s.org/) is used as the HTTP layer. http4s provides streaming and functional HTTP for Scala.
This example project uses [cats-effect](https://github.com/typelevel/cats-effect), but is possible to use
http4s with another effect monad.

By using an effect monad, side effects are postponed until the last moment.

http4s uses [fs2](https://github.com/functional-streams-for-scala/fs2) for streaming. This allows to return
streams in the HTTP layer so the response doesn't need to be generated in memory before sending it to the client.

In the example project this is done for the `GET /contacts` endpoint.

## doobie
[doobie](http://tpolecat.github.io/doobie/) is used to connect to the database. This is a pure functional JDBC layer for Scala.
This example project uses [cats-effect](https://github.com/typelevel/cats-effect) in combination with doobie,
but doobie can use another effect monad.

Because both http4s and doobie use an effect monad, the combination is still pure and functional.

## circe
[circe](https://github.com/circe/circe) is the recommended JSON library to use with http4s. It provides
automatic derivation of JSON Encoders and Decoders.

## Configuration
[pureconfig](https://github.com/pureconfig/pureconfig) is used to read the configuration file `application.conf`.
This library allows reading a configuration into well typed objects.

## Database
[h2](http://www.h2database.com/) is used as a database. This is an in memory database, so when stopping the application, the state of the
microservice is lost.

Using [Flyway](https://flywaydb.org/) the database migrations are performed when starting the server.

## Tests
This example project contains both unit tests, which mock the contact.repository that accesses the database, and
integration tests that use the [http4s](http://http4s.org/) HTTP client to perform actual requests.

## Running
You can run the microservice with `sbt run`. By default it listens to port number 8080, you can change
this in the `application.conf`.
