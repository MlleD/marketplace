package poca

import slick.jdbc.PostgresProfile.api._

class CategoryTable(tag: Tag) extends Table[(Int, String)](tag, "category") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name", O.Length(255))
    def * = (id, name)
}
