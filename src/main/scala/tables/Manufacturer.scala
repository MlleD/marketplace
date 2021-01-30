package poca

import slick.jdbc.PostgresProfile.api._

class ManufacturerTable(tag: Tag) extends Table[(Int, String, String)](tag, "manufacturer") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name", O.Length(255))
    def description = column[String]("description", O.Length(255))
    def * = (id, name, description)
}
