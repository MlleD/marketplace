package poca

import scala.concurrent.Future
import com.typesafe.scalalogging.LazyLogging
import slick.jdbc.PostgresProfile.api._

class Migration13CreateTableDeveloper(db: Database) extends Migration with LazyLogging {
    class DevelopersTable(tag: Tag) extends Table[(Int, String)](tag, "developers") {
        def developerId = column[Int]("id", O.PrimaryKey)
        def developername = column[String]("name",O.Length(255))
        def * = (developerId, developername)
    }

    override def apply(): Unit = {
        implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global
        val dropFuture: Future[Unit] = db.run(TableQuery[DevelopersTable].schema.dropIfExists)
        dropFuture.map(t => logger.info("Done dropping table Developers"))

        val creationFuture: Future[Unit] = db.run(TableQuery[DeveloperTable].schema.createIfNotExists)
        creationFuture.onComplete(t => logger.info("Done creating table Developer"))
    }
}
