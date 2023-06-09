import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object Future extends App {

  case class SellerId(userId: Int) extends AnyVal
  case class DepopProduct(id: Int) extends AnyVal
  case class PaymentId(id: Long) extends AnyVal
  case class Amount(q: Double) extends AnyVal

  def getPrice(product: DepopProduct): Future[Amount] = ???
  def getSeller(product: DepopProduct): Future[SellerId] = ???
  def pay(sellerId: SellerId, amount: Amount): Future[PaymentId] = ???
  def createRecordError(sellerId: SellerId, amount: Amount): Future[Unit] = ???

  val product1: DepopProduct = DepopProduct(1)
  for {
    seller <- getSeller(product1)
    price  <- getPrice(product1)
    res <- pay(seller, price).recover { case _: Exception =>
      createRecordError(seller, price)
    }
  } yield res
}
