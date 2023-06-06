# Functional Programming in Scala by me :)




## Who is me?

- I'm Cristian Cantarero
- I'm based on Madrid, Spain
- Last two years on Depop as tech contractor
- Why? Share personal experience using Scala and FP on Depop.

Note:
- Hello everyone, my name is Cristian Cantarero
- Based on Madrid, Spain
- I have been working on Depop for these last two years, in Sellers, Ads and now Fulfilment teams.
- Tech contractor from 47 Degrees (now Xebia Functional) 
- I'm going to talk to you about some of my experience using Scala and functional programming here.
- My idea is to share my path of approaches, challenges, and may some lessons I've learned along the way.




## What is Scala & FP?

- Scala combines object-oriented programming and functional programming
- Functional programming relies on the use of pure, immutable, and side-effect-free functions
- Goal: code more concise and feel more confident with it


Note:
- As you may know Scala is a multi-paradigm programming language 
- It combines elements of object-oriented programming and functional programming.
- Functional programming is a paradigm that relies on 
- the use of pure, immutable, and side-effect-free functions. 
- Let's say that everything is easier with things you can predict a substitute as we do in maths
- Why?
- I became interested in functional programming because I wanted to write code that was more concise, expressive and secure.




## `Future` & Asynchronous Calls

-  `Future` represents a value that may become available in the future.
-  `Future` allows asynchronous calls without blocking the main thread.

```scala
import scala.concurrent.Future

case class DepopProduct(id: Long)
def getPrice(product: DepopProduct): Future[Double] = ???

val priceOfTheProduct: Future[Double] = getPrice(DepopProduct(1L))
```

Note:
- One of the first things I started using here was `Future`, which is an abstraction that represents a value that may become available in the future.
- `Future` allows me to make asynchronous calls without blocking the main thread, and handle possible errors or results with callbacks or combinators.
- This is really useful to make or code run faster on the server. 
- `getPrice` is an async function (may calls to a db get the price, but we don't block the thread!!
- `priceOfTheProduct` is a value that may become in the future



### `EitherT` & `OptionT` with `Future`
- What if we want to deal with possible errors?
- Dealing with `Future[Either[A, B]]` or `Future[Option[A]]` can be tedious
- OptionT and EitherT can help!

Note:
- Both of them are data types from 
- OptionT: `OptionT[F[_], A]` wrapper of `F[Option[A]]`
- EitherT: `EitherT[F[_], A, B]` wrapper of `F[Either[A, B]]`




Example
```scala
import scala.concurrent.Future
import cats.data.EitherT
import cats.data.OptionT
import cats.syntax.all._

case class DepopProduct(id: Long)

case class ShippingInfo(carrier: String, price: Double)

case class CheckoutProduct(product: DepopProduct, price: Double, shipping: ShippingInfo)

def getPrice(product: DepopProduct): Future[Either[String, Double]] = ???
def getShippingInfo(product: DepopProduct): Future[Option[ShippingInfo]] = ???

val product: DepopProduct = DepopProduct(1L)
val checkoutProduct: EitherT[Future, String, CheckoutProduct] = for {
  price: Double <- EitherT(getPrice(product))
  shippingInfo: ShippingInfo <- EitherT.fromOptionF(getShippingInfo(product), "Not Shipping Info available")
} yield CheckoutProduct(product, price, shippingInfo)

val res: Future[Either[String, CheckoutProduct]] = checkoutProduct.value

```
Note:
- These data types from typelevel libraries can be really useful when dealing 
- Also they help with error handling situations where something can fails or there is no data 
- In the example we can see how they allow us to work with the proper data in the for-compression 




## Value Class / Opaque Types
- Same internal representation as another type,
- BUT they are distinct at compile-time.
- Allowing strong typing!
- So you don't get confused passing parameters :smile:


Note:

- Another thing I liked about Scala was the ability to use opaque types, which are types that have the same internal representation as another type, but are distinct at compile-time.
- Opaque types allow me to have strong typing with objects, which improves code safety, readability, and modularity.
- Here's an example of how I use opaque types to define a `SellerId` or `BuyerId` type that only accepts valid text strings.





Example Scala 2
```scala
case class SellerId(val userId: Int) extends AnyVal
case class BuyerId(val userId: Int) extends AnyVal

def pay(sellerId: SellerId): Unit = ???

def charge(buyerId: BuyerId): Unit = ???

```


Note:
- In Scala 3 it's easier
- There are also libraries to deal with this called `newtype`



Example Scala 3
```scala
opaque type SellerId = Int

opaque type BuyerId = Int

def pay(sellerId: SellerId): Unit = ???

def charge(buyerId: BuyerId): Unit = ???

```

Note:
- In this case is simpler to use them




## Using Effects

- What happens when we want to deal with outside world?
- Effects come to help us!
- For example avoid side effects when:
  - retrieve with a database 
  - making a http request
- How? For example Cats Effects IO library 



## IO
- Provides a nice way to deal with effects
- Encapsulates an action that will happen later
- Allow to compose different actions safely




## IO Example

```scala
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import cats.effect.{IO, IOApp}

object FutureIOCompositionExample extends IOApp.Simple {
  def getFutureNumber: Future[Int] =
    Future.successful(10)

  def getIONumber: IO[Int] =
    IO.pure(20)

  def run: IO[Unit] = {
    val futureResult: Future[Int] = getFutureNumber
    val ioResult: IO[Int] = getIONumber

    for {
      futureValue <- IO.fromFuture(IO(futureResult))
      ioValue <- ioResult
      _ <- IO(println(s"Suma de Future y IO: ${futureValue + ioValue}"))
    } yield ()
  }
}
```

Note:
- In Future there is no a explicit mechanism to deal with the effect
- When composing Futures we don't have control of when the effect is going to happen or not 
- Here, `IO.fromFuture` help us to deal with this.
- If was not there, future will run just when defining and we will not see nothing





## Scala 3 & Typelevel Libraries

- Scala 3 has simpler syntax, algebraic enums and so on
- The most common typelevel libraries like `cats-effect`, `http4s`, `fs2-kafka` are also in Scala 3
- Example [fulfilment-notification-ingest](https://github.com/depop/fulfilment-notification-ingest) service

Note:
- Finally, we had the chance to migrate to Scala 3 a project and even create a one from the scratch
- Scala 3 has a simpler and more consistent syntax, new features such as algebraic enums, union and intersection types, and much mire things
- At the beginning work with Scala 3 was harder because there was much more problems with the tooling
- An example of how I use Scala 3 with `cats-effect`, `http4s`, `fs2-kafka`and `skunk` to create a functional web application that consumes data from a kafka and database can be found on (https://github.com/depop/fulfilment-notification-ingest)



## Conclusion

- *Benefits* more concise, expressive, secure and confident codebase
- *Challenges* syntax and concepts of FP
  - [The Red Book](https://www.manning.com/books/functional-programming-in-scala) or `Functional Programming in Scala` 
- *Approach* Adding/Testing these concepts step by step 
- *Approach II* New project with [Scala 3 g8 project](https://github.com/depop/scala3-server-seed.g8)
- *Conclusion* IMO FP = robust & scalable applications


Note:
- I've seen the benefits of writing more concise, expressive, secure and confident codebase
- I've also encountered challenges with the syntax and concepts of functional programming.
    - Red Book is still helping
    - Or even Practical Functional Programming with scala can be a nice read.
- But I encourage to add the libraries to your projects or find some time to try thing with them
- Overall, I've found functional programming to be a powerful tool for building robust and scalable applications.
- Thank you for listening, and I encourage you to try or learn more about Scala and functional programming!



### Questions?

![Questions](imgs/questions.webp)



### Fuentes

| Título                                                                                  | Autor         |
|-----------------------------------------------------------------------------------------|---------------|
| EitherT and OptionT                                                                     |               |
| [Value classes](https://docs.scala-lang.org/overviews/core/value-classes.html)          | Scala         |
 | [Opaque types Scala 3](https://docs.scala-lang.org/scala3/book/types-opaque-types.html) | Scala         |
| [Practical FP in Scala](https://leanpub.com/pfp-scala)                                  | Gabriel Volpe |