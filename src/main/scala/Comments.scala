
package poca

import scala.concurrent.Future
import slick.jdbc.PostgresProfile.api._
import java.util.UUID

case class Comment(id: Int, iduser: Int, idproduct: Int, comment: String, nbstars: Int)

//final case class NameAlreadyExistsException(private val message: String="", private val cause: Throwable=None.orNull)
  //  extends Exception(message, cause)

class Comments {

type Comment_t = (Int, Int, Int, String, Int)
 
    implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global
    val db = MyDatabase.db
    val comments = TableQuery[CommentTable]

    def createComment(id: Int, iduser: Int, idproduct: Int, comment: String, nbstars: Int): Future[Unit] = {
       /* val existingCommentsFuture = getCommentById(id)
        
        existingCommentssFuture.flatMap(existingComments => {
            if (existingComments.isEmpty) {
               */
                //println("on ajoute un nouveau commentaire dans la bd")
                val newComment = Comment(id, iduser=iduser, idproduct=idproduct, comment=comment, nbstars=nbstars)
                val newCommentAsTuple: Comment_t = Comment.unapply(newComment).get

                val dbio: DBIO[Int] = comments += newCommentAsTuple
                var resultFuture: Future[Int] = db.run(dbio)

                // We do not care about the Int value
                resultFuture.map(_ => ())
            /*}else{
                throw new IdAlreadyExistsException(s"A comment with id '$id' already exists.")
            }*/
        //})
    }
/*
    def getCommentById(id: Int): Future[Option[Comment]] = {
        val query = comments.filter(_.id === id)

        val commentListFuture = db.run(query.result)

        commentListFuture.map((commentList: Seq[Comment_t]) => {
            commentList.length match {
                case 0 => None
                case 1 => Some(Comment tupled commentList.head)
                case _ => throw new InconsistentStateException(s"Comment $id is linked to several Products in database!")
            }
        })
    }
*/
    def getCommentsById(idproduct: Int): Future[Seq[Comment]] = {
        val query = comments.filter(_.idproduct === idproduct)

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
