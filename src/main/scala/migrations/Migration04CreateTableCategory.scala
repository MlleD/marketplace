package poca

import scala.concurrent.Future
import com.typesafe.scalalogging.LazyLogging
import slick.jdbc.PostgresProfile.api._

class Migration04CreateTableCategory(db: Database) extends Migration with LazyLogging {
    override def apply(): Unit = {
        implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global
        val table = TableQuery[CategoryTable]
        val creationFuture: Future[Unit] = db.run(table.schema.createIfNotExists)
        creationFuture.map(t => logger.info("Done creating table Category"))
    }
}
