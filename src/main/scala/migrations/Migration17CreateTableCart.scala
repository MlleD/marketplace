package poca

import scala.concurrent.{Future, Await}
import com.typesafe.scalalogging.LazyLogging
import scala.concurrent.duration.Duration
import slick.jdbc.PostgresProfile.api._
import java.sql.Timestamp

class Migration17CreateTableCart(db: Database) extends Migration with LazyLogging {

    override def apply(): Unit = {
        implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global
        val dropFuture: Future[Unit] = db.run(TableQuery[CartTable].schema.dropIfExists)
        dropFuture.map(t => logger.info("Done dropping table Cart"))

        val creationFuture: Future[Unit] = db.run(TableQuery[CartTable].schema.createIfNotExists)
        creationFuture.onComplete(t => logger.info("Done creating table Cart"))
        Await.result(creationFuture, Duration.Inf)
    }
}
