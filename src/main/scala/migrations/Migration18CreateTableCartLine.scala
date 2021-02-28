package poca

import scala.concurrent.{Future, Await}
import com.typesafe.scalalogging.LazyLogging
import scala.concurrent.duration.Duration
import slick.jdbc.PostgresProfile.api._

class Migration18CreateTableCartLine(db: Database) extends Migration with LazyLogging {

    override def apply(): Unit = {
        implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global
        val dropFuture: Future[Unit] = db.run(TableQuery[CartLineTable].schema.dropIfExists)
        dropFuture.map(t => logger.info("Done dropping table CartLine"))

        val creationFuture: Future[Unit] = db.run(TableQuery[CartLineTable].schema.createIfNotExists)
        creationFuture.onComplete(t => logger.info("Done creating table CartLine"))
        Await.result(creationFuture, Duration.Inf)
    }
}
