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
