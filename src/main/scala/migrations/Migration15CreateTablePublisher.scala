package poca

import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration
import com.typesafe.scalalogging.LazyLogging
import slick.jdbc.PostgresProfile.api._

class Migration15CreateTablePublisher(db: Database) extends Migration with LazyLogging {
    class PublishersTable(tag: Tag) extends Table[(Int, String)](tag, "publishers") {
        def publisherId = column[Int]("publisherId", O.PrimaryKey)
        def publishername = column[String]("publishername")
        def * = (publisherId, publishername)
    }

    override def apply(): Unit = {
        implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global
        val dropFuture: Future[Unit] = db.run(TableQuery[PublishersTable].schema.dropIfExists)
        dropFuture.map(t => logger.info("Done dropping table Publishers"))

        val creationFuture: Future[Unit] = db.run(TableQuery[PublisherTable].schema.createIfNotExists)
        creationFuture.onComplete(t => logger.info("Done creating table Publisher"))
    }
}
