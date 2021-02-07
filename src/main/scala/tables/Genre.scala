package poca

import slick.jdbc.PostgresProfile.api._

class GenreTable(tag: Tag) extends Table[(Int, String)](tag, "genre") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name", O.Length(255))
    def * = (id, name)
}
