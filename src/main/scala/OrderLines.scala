
package poca

import scala.concurrent.Future
import slick.jdbc.PostgresProfile.api._
import java.util.UUID

case class OrderLine(idorder: Int, idproduct: Int, idreseller: Int, idstatus: Int, price: Double, quantity: Int)

final case class IdAlreadyExistsException(private val message: String="", private val cause: Throwable=None.orNull)
    extends Exception(message, cause) 



class OrderLines {

type OrderLine_t = (Int, Int, Int, Int, Double, Int)

    implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global
    val db = MyDatabase.db
    val orderLines = TableQuery[OrderLineTable]

    def createOrderLine(idorder: Int, idproduct: Int, idreseller: Int, idstatus: Int, price: Double, quantity: Int): Future[Unit] = {
        val existingOrderLinesFuture = getOrderLineById(idorder, idproduct, idreseller)

        existingOrderLinesFuture.flatMap(existingOrderLines => {
            if (existingOrderLines.isEmpty) {
                
                val newOrderLine = OrderLine(idorder=idorder, idproduct=idproduct, idreseller=idreseller, idstatus=idstatus, price=price, quantity=quantity)
                val newOrderLineAsTuple: OrderLine_t = OrderLine.unapply(newOrderLine).get

                val dbio: DBIO[Int] = orderLines += newOrderLineAsTuple
                var resultFuture: Future[Int] = db.run(dbio)

                // We do not care about the Int value
                resultFuture.map(_ => ())
            } else {
                throw new IdAlreadyExistsException(s"A OrderLine with name '$idorder', '$idproduct', '$idreseller' already exists.")
            }
        })
    }


        def getOrderLineById(idorder: Int, idproduct: Int, idreseller: Int): Future[Option[OrderLine]] = {
        val query = orderLines.filter(x => (x.idorder === idorder && x.idproduct === idproduct && x.idreseller === idreseller))

        val orderLineListFuture = db.run(query.result)

        orderLineListFuture.map((orderLineList: Seq[OrderLine_t]) => {
            orderLineList.length match {
                case 0 => None
                case 1 => Some(OrderLine tupled orderLineList.head)
                case _ => throw new InconsistentStateException(s"OrderLine $idorder is linked to several Products in database!")
            }
        })
    }

    def getAllOrderLines(): Future[Seq[OrderLine]] = {
        val orderLineListFuture = db.run(orderLines.result)

        orderLineListFuture.map((orderLineList: Seq[OrderLine_t]) => {
            orderLineList.map(OrderLine tupled _)
        })
    }
}
