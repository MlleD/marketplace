package poca

import slick.jdbc.PostgresProfile.api._

class ProductCategoryTable(tag: Tag) extends Table[(Int, Int)](tag, "product_category") {
    def idproduct = column[Int]("idproduct")
    def idcategory = column[Int]("idcategory")
    def product = foreignKey("product_category_idproduct_fkey", idproduct, TableQuery[ProductTable])(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def category = foreignKey("product_category_idcategory_fkey", idcategory, TableQuery[CategoryTable])(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def pk = primaryKey("product_category_pkey", (idproduct, idcategory))
    def * = (idproduct, idcategory)
}
