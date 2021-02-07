package poca

import slick.jdbc.PostgresProfile.api._

class PublisherTable(tag: Tag) extends Table[(Int, String)](tag, "publisher") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name", O.Length(255))
    def * = (id, name)
}
