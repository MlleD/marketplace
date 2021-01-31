package poca

import slick.jdbc.PostgresProfile.api._

class UserTable(tag: Tag) extends Table[(Int, String, String, String, String, String, String)](tag, "user") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def firstname = column[String]("firstname", O.Length(255))
    def lastname = column[String]("lastname", O.Length(255))
    def email = column[String]("email", O.Length(255))
    def password = column[String]("password", O.Length(255))
    def address = column[String]("address", O.Length(255))
    def telephone = column[String]("telephone", O.Length(255))
    def * = (id, firstname, lastname, email, password, address, telephone)
}
