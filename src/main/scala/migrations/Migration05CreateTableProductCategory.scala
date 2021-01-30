package poca

import scala.concurrent.Future
import com.typesafe.scalalogging.LazyLogging
import slick.jdbc.PostgresProfile.api._

class Migration05CreateTableProductCategory(db: Database) extends Migration with LazyLogging {
    override def apply(): Unit = {
        implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global
        val table = TableQuery[ProductCategoryTable]
        val creationFuture: Future[Unit] = db.run(table.schema.create)
        creationFuture.map(t => logger.info("Done creating table Product_Category"))
    }
}
