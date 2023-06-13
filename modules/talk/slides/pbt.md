## Functional Programming in Scala by me :)
---
### Hi
![Alt Text](https://media.giphy.com/media/3o7budMRwZvNGJ3pyE/giphy.gif)

Note:
- Hello everyone
- thank you for connect to this talk
- I am gonna steal some minutes for your time but I hope you can learn something new
---
![Alt Text](https://media.giphy.com/media/NsaOcTrbIVH3lwdOXL/giphy-downsized-large.gif)
- Who? [Cristian Cantarero](https://www.linkedin.com/in/cristiancantarero/)
- Where? Based on Madrid, Spain
- Why? Last two years on Depop as tech contractor
- What? Share personal experience using Scala and FP on Depop.
- When? NOW!

Note:
- Hello everyone, my name is Cristian Cantarero 
- Based on Madrid, Spain
- I have been working on Depop for these last two years, in Sellers, Ads and now Fulfilment teams.
- I am Tech contractor from 47 Degrees (now Xebia Functional) 
- I'm going to talk to you about some of my experience using Scala and functional programming here.
- My idea is to share my path of approaches, challenges, and may some lessons I've learned along the way.



### What is Scala & FP?
![Alt Text](https://media.giphy.com/media/rI9O6UXkCjvTG/giphy.gif)
---
- Scala combines object-oriented programming and functional programming
- Functional programming relies on the use of pure, immutable, and side-effect-free functions
- Goal: code more concise and feel more confident with it

Note:
- For those who are not familiar with Scala is a multi-paradigm programming language 
- It combines elements of object-oriented programming and functional programming.
- Functional programming is a paradigm that relies on 
- the use of pure, immutable, and side-effect-free functions. 
- Let's say that everything is easier with things you can predict a substitute as we do in maths
- Moreover, we don't have so much boiler plate and we have less code, so we can make less errors.



### Value Class / Opaque Types
![Alt Text](https://media.giphy.com/media/l36kU80xPf0ojG0Erg/giphy.gif)

Note:
- First and I think one of the things I feel is more important
---
**What they are?**
- Same internal representation as another type
- they are distinct at compile-time.
- Allows strong type!
- No more passing parameters problems

![Alt Text](https://media.giphy.com/media/ej0Ay8fH6Y1Wg/giphy.gif)

Note:
- Another thing I liked about Scala was the ability to use opaque types
- There are types that have the same internal representation as another type
- but are distinct at compile-time.
- Opaque types allows to have strong typing with objects, 
- which improves code safety, readability, and modularity
- 
---
**Example Scala 2**
```scala
case class SellerId(val userId: Int) extends AnyVal
case class BuyerId(val userId: Int) extends AnyVal

def pay(sellerId: SellerId, amount: Double): Unit = ???
def charge(buyerId: BuyerId, amount: Double): Unit = ???
```

Note:
- This is a perfect example where we can define some actions (pay, charge) 
- to only affect some types(buyer, seller)
- There are also libraries to deal with this called `newtype`
---
**Example Scala 3**
```scala
opaque type SellerId = Int
opaque type BuyerId = Int
```
Note:
- In this case it looks even easiest to use these types



### Future & Asynchronous Calls
![Alt Text](https://media.giphy.com/media/ZZkCo8zKWtt2ZgozfX/giphy.gif)
---
 **Future**
- represents a value that may become available in the future
- allows asynchronous calls without blocking the main thread
- you can compose them and also deal with errors

Note:
- 
---
An example
```scala
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object FutureII extends App {
  case class SellerId(userId: Int) extends AnyVal

  case class DepopProduct(id: Int) extends AnyVal

  case class PaymentId(id: Long) extends AnyVal

  case class Amount(q: Double) extends AnyVal

  def getPrice(product: DepopProduct): Future[Amount] =
    Future.successful(Amount(20))

  def getSeller(product: DepopProduct): Future[SellerId] =
    Future.successful(SellerId(10))

  // def pay(sellerId: SellerId, amount: Amount): Future[PaymentId] = Future.successful(PaymentId(101L))
  def pay(sellerId: SellerId, amount: Amount): Future[PaymentId] =
    Future.failed(new Exception("No money"))

  def createRecordError(sellerId: SellerId, amount: Amount): Future[Unit] =
    Future.successful((println(s"created error record for $sellerId")))

  val product1: DepopProduct = DepopProduct(1)
  val result = for {
    seller <- getSeller(product1)
    price  <- getPrice(product1)
    res <- pay(seller, price).recover { case _: Exception =>
      createRecordError(seller, price)
    }
  } yield res

  val res = scala.concurrent.Await
          .result(result, scala.concurrent.duration.Duration.Inf)

  println(res)

}
```

Note:
- this example demonstrates how multiple asynchronous operations can be chained together using Future 
-  Each function call returns a Future, 
- and the syntax of the for comprehension allows for concise combination and transformation of results.
- Additionally, the use of recover allows for error handling and alternative actions in case of exceptions.



### EitherT & OptionT with Future

What if we want to deal with possible errors?
![Alt Text](https://media.giphy.com/media/wffqitstlBuXf7Po0y/giphy.gif)
---

- Dealing with `Future[Either[A, B]]` or `Future[Option[A]]` can be tedious
- OptionT and EitherT can help!

Note:
- Both of them are data types from 
- OptionT: `OptionT[F[_], A]` wrapper of `F[Option[A]]`
- EitherT: `EitherT[F[_], A, B]` wrapper of `F[Either[A, B]]`
---
```scala
import cats.data.EitherT

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object FutureEither extends App {
  case class DepopProduct(id: Long) extends AnyVal
  case class Amount(q: Double)      extends AnyVal
  case class Carrier(s: String)     extends AnyVal
  case class ShippingInfo(carrier: Carrier, price: Amount)
  case class CheckoutProduct(
                                    product: DepopProduct,
                                    price: Amount,
                                    shipping: ShippingInfo
                            )

  val product: DepopProduct = DepopProduct(1L)
  def getPrice(product: DepopProduct): Future[Either[String, Amount]] =
    Future.successful(Right(Amount(10)))
  def getShippingInfo(product: DepopProduct): Future[Option[ShippingInfo]] =
    Future.successful(None)

  /*def getShippingInfo(product: DepopProduct): Future[Option[ShippingInfo]] =
    Future.successful(Some(ShippingInfo(Carrier("USPS"), Amount(5))))*/


  val checkoutProduct: EitherT[Future, String, CheckoutProduct] = for {
    price <- EitherT(getPrice(product))
    shippingInfo <- EitherT.fromOptionF(
      getShippingInfo(product),
      "Not Shipping Info available"
    )
  } yield CheckoutProduct(product, price, shippingInfo)

  val res = scala.concurrent.Await
          .result(checkoutProduct.value, scala.concurrent.duration.Duration.Inf)

  println(res)

}
```
Note:
- These data types from typelevel libraries can be really useful when dealing 
- Also, they help with error handling situations where something can fails or there is no data 
- In the example we can see how they allow us to work with the proper data in the for-compression 



## Using Effects
![Alt Text](https://media.giphy.com/media/rne3oSd7DsBDa/giphy.gif)

What happens when we want to deal with outside world?

---
- Effects come to help us!
- Avoiding side effects when:
  - retrieve with a database 
  - making a http request
- How? For example Cats Effects IO library 
---
## IO
- Provides a nice way to deal with effects
- Encapsulates an action that will happen later
- Allow to compose different actions safely
- Allow to decide when run those actions
---
## IO Example

```scala
object FutureIOEffectExample extends IOApp.Simple {
  def printFutureMessage: Future[Unit] =
    Future(println("Future effect"))

  def printIOMessage: IO[Unit] =
    IO(println("IO effect"))

  def run: IO[Unit] = {
    val futureEffect: Future[Unit] = printFutureMessage
    val ioEffect: IO[Unit] = printIOMessage

    for {
      _ <- ioEffect
      _ <- IO(futureEffect)
      //_ <- IO.fromFuture(IO(printFutureMessage))
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
![Alt Text](https://media.giphy.com/media/AbF3tqSgMuoCzE6ulp/giphy.gif)
---
- Scala 3 has simpler syntax, algebraic enums, union and intersection types, etc
- Tooling is getting better
- The most common typelevel libraries like `cats-effect`, `http4s`, `fs2-kafka` are also in Scala 3
- A nice movement from Scala 2 & Play Framework

Note:
- Finally, we had the chance to migrate to Scala 3 a project and even create several from scratch
- Scala 3 has a simpler and more consistent syntax, new features such as algebraic enums, union and intersection types, and much mire things
- At the beginning work with Scala 3 was harder because there was much more problems with the tooling
- An example of how I use Scala 3 with `cats-effect`, `http4s`, `fs2-kafka`and `skunk` to create a functional web application
- that consumes data from a kafka and database can be found on (https://github.com/depop/fulfilment-notification-ingest)
---
**Scala 2 Play vs Scala 3 Typelevel** 
- Endpoints: `routes` -> `Endpoints.scala` file with `tapir` syntax
- PostgresDB: `slick`  -> `skunk`  
- Kafka: `depop-kafka` -> `fs2-kafka`
- *Complete* example [fulfilment-notification-ingest](https://github.com/depop/fulfilment-notification-ingest) service



### Conclusions
![Alt Text](https://media.giphy.com/media/ltBhl4BoHaMM/giphy.gif)
---
- **Benefits** more concise, expressive, secure and confident codebase
- **Challenges** syntax and concepts of FP
  - [The Red Book](https://www.manning.com/books/functional-programming-in-scala) or `Functional Programming in Scala` helps 
- **Approach** Adding/Testing these concepts step by step 
- **Approach II** New project with [Scala 3 g8 project](https://github.com/depop/scala3-server-seed.g8)
- **Conclusion** IMO 
```scala
val FP = robust && scalable_apps
```

Note:
- I've seen the benefits of writing more concise, expressive, secure and confident codebase
- I've also encountered challenges with the syntax and concepts of functional programming.
    - Red Book is still helping
    - Or even Practical Functional Programming with scala can be a nice read.
- But I encourage to add the libraries to your projects or find some time to try thing with them
- Overall, I've found functional programming to be a powerful tool for building robust and scalable applications.
- Thank you for listening, and I encourage you to try or learn more about Scala and functional programming!



## Acknowledgements

Alessandro, Alberto, John F, Luis, Hersiv, Aida, Russell, Joe, Chloe, Jacob,![Alt Text](https://media.giphy.com/media/mqd0IiCu4iBVK/giphy.gif)  Umar, Bego, Damien, Farid, Marcin, Tanya, Jack, Rhiannon, Dani, James, Ana, Charlie, Fifi

Note:
- Alessandro is the responsible for almost 75% of all my knowdledge related with FP and really doing stuff with that. I'm really thankful to you for that.
- But without the help of alberto this would be really difficult



## Questions?
![Alt Text](https://media.giphy.com/media/3o7btY5JUf60CZeHny/giphy.gif)
