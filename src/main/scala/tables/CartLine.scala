package poca

import slick.jdbc.PostgresProfile.api._

class CartLineTable(tag: Tag) extends Table[(Int, Int, Int, Double, Int)](tag, "cartLine") {
    def idcart = column[Int]("idcart")
    def cart = foreignKey("cartLine_idcart_fkey", idcart, TableQuery[CartTable])(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def idproduct = column[Int]("idproduct")
    def idreseller = column[Int]("idreseller")    
    def productreseller = foreignKey("cartLine_idproductreseller_fkey", (idproduct, idreseller), TableQuery[ProductResellerTable])((t => (t.idproduct, t.idreseller)), onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def price = column[Double]("price")
    def quantity = column[Int]("quantity")
    def pk = primaryKey("cartLine_pkey", (idcart, idproduct, idreseller))
    def * = (idcart, idproduct, idreseller, price, quantity)
}
