package poca

import slick.jdbc.PostgresProfile.api._
import java.sql.Timestamp

class CartTable(tag: Tag) extends Table[(Int, Int)](tag, "cart") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def iduser = column[Int]("iduser")
    def user = foreignKey("cart_iduser_fkey", iduser, TableQuery[UserTable])(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def * = (id, iduser)
}
