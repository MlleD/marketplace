package poca

import scala.concurrent.Future
import com.typesafe.scalalogging.LazyLogging
import slick.jdbc.PostgresProfile.api._

class Migration09CreateTableUser(db: Database) extends Migration with LazyLogging {
    class UserTable(tag: Tag) extends Table[(String, String)](tag, "user") {
        def userId = column[String]("userId", O.PrimaryKey)
        def username = column[String]("username")
        def * = (userId, username)
    }

    override def apply(): Unit = {
        implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global
        val dropFuture: Future[Unit] = db.run(TableQuery[UserTable].schema.dropIfExists)
        dropFuture.map(t => logger.info("Done dropping table User"))

        val creationFuture: Future[Unit] = db.run(TableQuery[UserTable].schema.createIfNotExists)
        creationFuture.onComplete(t => logger.info("Done creating table User"))
    }
}
