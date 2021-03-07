
package poca

import scala.concurrent.Future
import slick.jdbc.PostgresProfile.api._
import java.util.UUID
import poca.Cart

case class CartLine(idcart: Int, idproduct: Int, idreseller: Int, price: Double, quantity: Int)

class CartLines {

    type CartLine_t = (Int, Int, Int, Double, Int)

    implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global
    val db = MyDatabase.db
    val cartLines = TableQuery[CartLineTable]
    val cart = TableQuery[CartTable]

    def createCartLine(idcart: Int, idproduct: Int, idreseller: Int, price: Double, quantity: Int): Future[Unit] = {
        val existingCartLinesFuture = getCartLineById(idcart, idproduct, idreseller)

        existingCartLinesFuture.flatMap(existingCartLines => {
            if (existingCartLines.isEmpty) {
                
                val newCartLine = CartLine(idcart=idcart, idproduct=idproduct, idreseller=idreseller, price=price, quantity=quantity)
                val newCartLineAsTuple: CartLine_t = CartLine.unapply(newCartLine).get

                val dbio: DBIO[Int] = cartLines += newCartLineAsTuple
                var resultFuture: Future[Int] = db.run(dbio)

                // We do not care about the Int value
                resultFuture.map(_ => ())
            } else {
                throw new IdAlreadyExistsException(s"A CartLine with name '$idcart', '$idproduct', '$idreseller' already exists.")
            }
        })
    }


    def getCartLineById(idcart: Int, idproduct: Int, idreseller: Int): Future[Option[CartLine]] = {
        val query = cartLines.filter(x => x.idcart === idcart && x.idproduct === idproduct && x.idreseller === idreseller)

        val cartLineListFuture = db.run(query.result)

        cartLineListFuture.map((cartLineList: Seq[CartLine_t]) => {
            cartLineList.length match {
                case 0 => None
                case 1 => Some(CartLine tupled cartLineList.head)
                case _ => throw new InconsistentStateException(s"CartLine ($idcart,$idproduct,$idreseller) is linked to several CartLines in database!")
            }
        })
    }

    def getCartLinesByIdCart(idcart: Int): Future[Seq[CartLine]] = {
        val query = cartLines.filter(_.idcart === idcart)
        val cartLineListFuture = db.run(query.result)
        cartLineListFuture.map(seq => seq.map(CartLine tupled _))
    }

    def getAllCartLines(): Future[Seq[CartLine]] = {
        val cartLineListFuture = db.run(cartLines.result)

        cartLineListFuture.map((cartLineList: Seq[CartLine_t]) => {
            cartLineList.map(CartLine tupled _)
        })
    }

    def updateCartlineQuantity(idcart: Int, idproduct: Int, idreseller: Int, quantity: Int): Future[Unit] = {
        val query = for {c <- cartLines if c.idcart === idcart && c.idproduct === idproduct && c.idreseller === idreseller} yield c.quantity
        val updateAction = query.update(quantity)
        db.run(updateAction).map(_ => ())
    }

    def deleteCartline(idcart: Int, idproduct: Int, idreseller: Int): Future[Unit] = {
        val query = cartLines.filter(x => x.idcart === idcart && x.idproduct === idproduct && x.idreseller === idreseller)
        val deleteAction = query.delete
        val affectedRowsCount: Future[Int] = db.run(deleteAction)

        // We do not care about the Int value
        affectedRowsCount.map(_ => ())
    }
}
