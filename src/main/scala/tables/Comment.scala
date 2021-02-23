package poca

import slick.jdbc.PostgresProfile.api._

class CommentTable(tag: Tag) extends Table[(Int, Int, Int, String, Int, Boolean)](tag, "comment") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def iduser = column[Int]("iduser")
    def user = foreignKey("comment_iduser_fkey", iduser, TableQuery[UserTable])(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def idproduct = column[Int]("idproduct")
    def product = foreignKey("comment_idproduct_fkey", idproduct, TableQuery[ProductTable])(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def comment = column[String]("comment", O.Length(255))
    def nbstars = column[Int]("nbstars")
    def verify = column[Boolean]("verify")
    def * = (id, iduser, idproduct, comment, nbstars, verify)
}
