
package poca

import scala.concurrent.Future
import slick.jdbc.PostgresProfile.api._
import java.util.UUID
import java.sql.Timestamp

case class Comment(id: Int, iduser: Int, idproduct: Int, comment: String, nbstars: Int, verify: Boolean)

//final case class NameAlreadyExistsException(private val message: String="", private val cause: Throwable=None.orNull)
  //  extends Exception(message, cause)

class Comments(orders: Orders, orderLines: OrderLines) {

type Comment_t = (Int, Int, Int, String, Int, Boolean)
type Order_t = (Int, Int, Timestamp)
type OrderLine_t = (Int, Int, Int, Int, Double, Int)
 
    implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global
    val db = MyDatabase.db
    val comments = TableQuery[CommentTable]

    def createComment(id: Int, iduser: Int, idproduct: Int, comment: String, nbstars: Int): Future[Unit] = {
                val orderListFuture : Future[Seq[Order]] = orders.getOrderByIdUser(iduser)

                orderListFuture.map((orderList: Seq[Order]) => {
                    orderList.map(o => 
                                        orderLines.getOrderLineById(o.id, idproduct, 1).map((ol: Option[OrderLine]) => {
                                            val verify = if (ol == None) false else true
                                            val newComment = Comment(id, iduser=iduser, idproduct=idproduct, comment=comment, nbstars=nbstars, verify=verify)
                                            val newCommentAsTuple: Comment_t = Comment.unapply(newComment).get

                                            val dbio: DBIO[Int] = comments += newCommentAsTuple
                                            var resultFuture: Future[Int] = db.run(dbio)

                                            // We do not care about the Int value
                                            resultFuture.map(_ => ())
                                        })
                    )

                })
                /*

                val existingCommentsFuture = getCommentsByIdComment(idproduct, comment)

                existingCommentsFuture.flatMap(existingComments => {
                    if (existingComments.isEmpty) {

                        val newComment = Comment(id, iduser=iduser, idproduct=idproduct, comment=comment, nbstars=nbstars, verify=false)
                        val newCommentAsTuple: Comment_t = Comment.unapply(newComment).get

                        val dbio: DBIO[Int] = comments += newCommentAsTuple
                        var resultFuture: Future[Int] = db.run(dbio)

                        // We do not care about the Int value
                        resultFuture.map(_ => ())
                    } else {
                        throw new NameAlreadyExistsException(s"A developer with name '$id' already exists.")
                    }
                })
                */


                
    }

    def getCommentsById(idproduct: Int): Future[Seq[Comment]] = {
        val query = comments.filter(_.idproduct === idproduct)

        val commentListFuture = db.run(query.result)

        commentListFuture.map((commentList: Seq[Comment_t]) => {
            commentList.map(Comment tupled _)
        })
    }

    def getCommentsByIdComment(idproduct: Int, comment: String): Future[Seq[Comment]] = {
        val query = comments.filter(x => (x.idproduct === idproduct && x.comment === comment))

        val commentListFuture = db.run(query.result)

        commentListFuture.map((commentList: Seq[Comment_t]) => {
            commentList.map(Comment tupled _)
        })
    }

    def getAllComments(): Future[Seq[Comment]] = {
        val commentListFuture = db.run(comments.result)

        commentListFuture.map((commentList: Seq[Comment_t]) => {
            commentList.map(Comment tupled _)
        })
    }
    
}
