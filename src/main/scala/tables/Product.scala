package poca

import slick.jdbc.PostgresProfile.api._
import java.sql.Timestamp

class ProductTable(tag: Tag) extends Table[(Int, String, String, Int, Timestamp)](tag, "product") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name", O.Length(255))
    def description = column[String]("description", O.Length(255))
    def idmanufacturer = column[Int]("idmanufacturer")
    def manufacturer = foreignKey("product_idmanufacturer_fkey", idmanufacturer, TableQuery[ManufacturerTable])(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def date = column[Timestamp]("date", O.SqlType("timestamp default now()"))
    def * = (id, name, description, idmanufacturer, date)
}
