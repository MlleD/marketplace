package poca

import scala.concurrent.{Future, Await}
import com.typesafe.scalalogging.LazyLogging
import scala.concurrent.duration.Duration
import slick.jdbc.PostgresProfile.api._
import java.sql.Timestamp

class Migration11CreateTableOrder(db: Database) extends Migration with LazyLogging {
    class OrderTable(tag: Tag) extends Table[(Int, Int, Timestamp)](tag, "order") {
    def orderid = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def orderiduser = column[Int]("iduser")
    def orderuser = foreignKey("order_iduser_fkey", orderiduser, TableQuery[UserTable])(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def orderdate = column[Timestamp]("date", O.SqlType("timestamp default now()"))
    def * = (orderid, orderiduser, orderdate)
}
    override def apply(): Unit = {
        implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global
        val dropFuture: Future[Unit] = db.run(TableQuery[OrderTable].schema.dropIfExists)
        dropFuture.map(t => logger.info("Done dropping table Order"))

        val creationFuture: Future[Unit] = db.run(TableQuery[OrderTable].schema.createIfNotExists)
        creationFuture.onComplete(t => logger.info("Done creating table Order"))
        Await.result(creationFuture, Duration.Inf)
    }
}
