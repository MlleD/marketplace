package poca

import slick.jdbc.PostgresProfile.api._

class OrderLineTable(tag: Tag) extends Table[(Int, Int, Int, Int, Double, Int)](tag, "orderLine") {
    def idorder = column[Int]("idorder")
    def order = foreignKey("ordeLine_idorder_fkey", idorder, TableQuery[OrderTable])(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def idproduct = column[Int]("idproduct")
    def idreseller = column[Int]("idreseller")    
    def productreseller = foreignKey("orderLine_idproductreseller_fkey", (idproduct, idreseller), TableQuery[ProductResellerTable])((t => (t.idproduct, t.idreseller)), onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def idstatus = column[Int]("idstatus")
    def status = foreignKey("orderLine_idstatus_fkey", idstatus, TableQuery[OrderStatusTable])(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def price = column[Double]("price")
    def quantity = column[Int]("quantity")
    def pk = primaryKey("orderLine_pkey", (idorder, idproduct, idreseller))
    def * = (idorder, idproduct, idreseller, idstatus, price, quantity)
}
