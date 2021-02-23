
package poca

import scala.concurrent.Future
import slick.jdbc.PostgresProfile.api._
import java.util.UUID
import java.sql.Timestamp

case class Order(id: Int, iduser: Int, date: Timestamp)



class Orders {

type Order_t = (Int, Int, Timestamp)

    implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global
    val db = MyDatabase.db
    val orders = TableQuery[OrderTable]

    def createOrder(id: Int, iduser: Int, date: Timestamp): Future[Unit] = {
        val existingOrdersFuture = getOrderById(id)

        existingOrdersFuture.flatMap(existingOrders => {
            if (existingOrders.isEmpty) {
                
                val newOrder = Order(id=id, iduser=iduser, date=date)
                val newOrderAsTuple: Order_t = Order.unapply(newOrder).get

                val dbio: DBIO[Int] = orders += newOrderAsTuple
                var resultFuture: Future[Int] = db.run(dbio)

                // We do not care about the Int value
                resultFuture.map(_ => ())
            } else {
                throw new IdAlreadyExistsException(s"A Order with name '$id' already exists.")
            }
        })
    }


        def getOrderById(id: Int): Future[Option[Order]] = {
        val query = orders.filter(_.id === id)

        val devListFuture = db.run(query.result)

        devListFuture.map((devList: Seq[Order_t]) => {
            devList.length match {
                case 0 => None
                case 1 => Some(Order tupled devList.head)
                case _ => throw new InconsistentStateException(s"Order $id is linked to several Products in database!")
            }
        })
    }

    def getAllOrders(): Future[Seq[Order]] = {
        val orderListFuture = db.run(orders.result)

        orderListFuture.map((orderList: Seq[Order_t]) => {
            orderList.map(Order tupled _)
        })
    }
}
