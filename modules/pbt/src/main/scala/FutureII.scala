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
