package poca

import scala.concurrent.{Future, Await}
import com.typesafe.scalalogging.LazyLogging
import scala.concurrent.duration.Duration
import slick.jdbc.PostgresProfile.api._

class Migration10CreateTableComment(db: Database) extends Migration with LazyLogging {
    class CommentTable(tag: Tag) extends Table[(Int, Int, Int, String, Int)](tag, "comment") {
        def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
        def iduser = column[Int]("iduser")
        def user = foreignKey("comment_iduser_fkey", iduser, TableQuery[UserTable])(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
        def idproduct = column[Int]("idproduct")
        def product = foreignKey("comment_idproduct_fkey", idproduct, TableQuery[ProductTable])(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
        def comment = column[String]("comment", O.Length(255))
        def nbstars = column[Int]("nbstars")
        def * = (id, iduser, idproduct, comment, nbstars)
    }
    override def apply(): Unit = {
        implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global
        val dropFuture: Future[Unit] = db.run(TableQuery[CommentTable].schema.dropIfExists)
        dropFuture.map(t => logger.info("Done dropping table Comment"))

        val creationFuture: Future[Unit] = db.run(TableQuery[CommentTable].schema.createIfNotExists)
        creationFuture.onComplete(t => logger.info("Done creating table Comment"))
        Await.result(creationFuture, Duration.Inf)
    }
}
