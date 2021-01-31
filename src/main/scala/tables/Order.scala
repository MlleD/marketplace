package poca

import slick.jdbc.PostgresProfile.api._
import java.sql.Timestamp

class OrderTable(tag: Tag) extends Table[(Int, Int, Timestamp)](tag, "order") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def iduser = column[Int]("iduser")
    def user = foreignKey("order_iduser_fkey", iduser, TableQuery[UserTable])(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def date = column[Timestamp]("date", O.SqlType("timestamp default now()"))
    def * = (id, iduser, date)
}
