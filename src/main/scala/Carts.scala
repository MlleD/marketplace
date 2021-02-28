
package poca

import scala.concurrent.Future
import slick.jdbc.PostgresProfile.api._
import java.util.UUID
import java.sql.Timestamp

case class Cart(id: Int, iduser: Int)

final case class IdUserAlreadyExistsException(private val message: String="", private val cause: Throwable=None.orNull)
   extends Exception(message, cause)

class Carts {
    type Cart_t = (Int, Int)

    implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global
    val db = MyDatabase.db
    val carts = TableQuery[CartTable]

    def createCart(id: Int, iduser: Int): Future[Unit] = {
        val existingCartsFuture = getCartById(id)

        existingCartsFuture.flatMap(existingCarts => {
            if (existingCarts.isEmpty) {
                val havingOtherCartFuture = getCartByIdUser(iduser)
                havingOtherCartFuture.flatMap(otherCart => 
                if (otherCart.isEmpty) {
                    val newCart = Cart(id=id, iduser=iduser)
                    val newCartAsTuple: Cart_t = Cart.unapply(newCart).get

                    val dbio: DBIO[Int] = carts += newCartAsTuple
                    var resultFuture: Future[Int] = db.run(dbio)

                    // We do not care about the Int value
                    resultFuture.map(_ => ())
                } else {
                    throw new IdUserAlreadyExistsException(s"A Cart with user id '$iduser' already exists.")
                })
            } else {
                throw new IdAlreadyExistsException(s"A Cart with id '$id' already exists.")
            }
        })
    }


    def getCartById(id: Int): Future[Option[Cart]] = {
        val query = carts.filter(_.id === id)

        val cartListFuture = db.run(query.result)

        cartListFuture.map((cartList: Seq[Cart_t]) => {
            cartList.length match {
                case 0 => None
                case 1 => Some(Cart tupled cartList.head)
                case _ => throw new InconsistentStateException(s"Cart $id is linked to several Carts in database!")
            }
        })
    }

    def getCartByIdUser(iduser: Int): Future[Option[Cart]] = {
        val query = carts.filter(_.iduser === iduser)

        val cartListFuture = db.run(query.result)

        cartListFuture.map((cartList: Seq[Cart_t]) => {
            cartList.length match {
                case 0 => None
                case 1 => Some(Cart tupled cartList.head)
                case _ => throw new InconsistentStateException(s"User $iduser is linked to several Carts in database!")
            }
        })
    }

    def getAllCarts(): Future[Seq[Cart]] = {
        val cartListFuture = db.run(carts.result)

        cartListFuture.map((cartList: Seq[Cart_t]) => {
            cartList.map(Cart tupled _)
        })
    }
}
