package poca

import slick.jdbc.PostgresProfile.api._

class ProductResellerTable(tag: Tag) extends Table[(Int, Int, Float, Float, Int)](tag, "product_reseller") {
    def idproduct = column[Int]("idproduct")
    def idreseller = column[Int]("idreseller")
    def product = foreignKey("product_reseller_idproduct_fkey", idproduct, TableQuery[ProductTable])(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def reseller = foreignKey("product_reseller_idreseller_fkey", idreseller, TableQuery[ResellerTable])(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def pk = primaryKey("product_reseller_pkey", (idproduct, idreseller))
    def price = column[Float]("price")
    def discount = column[Float]("discount", O.Default(0f))
    def quantity = column[Int]("quantity")
    def * = (idproduct, idreseller, price, discount, quantity)
}
