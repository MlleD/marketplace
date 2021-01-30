package poca

import slick.jdbc.PostgresProfile.api._

class ResellerTable(tag: Tag) extends Table[(Int, String, String, String)](tag, "reseller") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name", O.Length(255))
    def email = column[String]("email", O.Length(255))
    def address = column[String]("address", O.Length(255))
    def * = (id, name, email, address)
}
