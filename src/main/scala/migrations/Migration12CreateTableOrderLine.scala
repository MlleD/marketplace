package poca

import scala.concurrent.{Future, Await}
import com.typesafe.scalalogging.LazyLogging
import scala.concurrent.duration.Duration
import slick.jdbc.PostgresProfile.api._

class Migration12CreateTableOrderLine(db: Database) extends Migration with LazyLogging {
    class OrderLineTable(tag: Tag) extends Table[(Int, Int, Int, Int, Double, Int)](tag, "orderLine") {
    def orderLineidorder = column[Int]("idorder")
    def orderLineorder = foreignKey("orderLine_idorder_fkey", orderLineidorder, TableQuery[OrderTable])(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def orderLineidproduct = column[Int]("idproduct")
    def orderLineidreseller = column[Int]("idreseller")    
    def productreseller = foreignKey("orderLine_idproductreseller_fkey", (orderLineidproduct, orderLineidreseller), TableQuery[ProductResellerTable])((t => (t.idproduct, t.idreseller)), onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def orderLineidstatus = column[Int]("idstatus")
    def orderLinestatus = foreignKey("orderLine_idstatus_fkey", orderLineidstatus, TableQuery[OrderStatusTable])(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def orderLineprice = column[Double]("price")
    def orderLinequantity = column[Int]("quantity")
    def pk = primaryKey("orderLine_pkey", (orderLineidorder, orderLineidproduct, orderLineidreseller))
    def * = (orderLineidorder, orderLineidproduct, orderLineidreseller, orderLineidstatus, orderLineprice, orderLinequantity)
}
    override def apply(): Unit = {
        implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global
        val dropFuture: Future[Unit] = db.run(TableQuery[OrderLineTable].schema.dropIfExists)
        dropFuture.map(t => logger.info("Done dropping table OrderLine"))

        val creationFuture: Future[Unit] = db.run(TableQuery[OrderLineTable].schema.createIfNotExists)
        creationFuture.onComplete(t => logger.info("Done creating table OrderLine"))
        Await.result(creationFuture, Duration.Inf)
    }
}
