
package poca

import scala.concurrent.Future
import slick.jdbc.PostgresProfile.api._
import java.util.UUID

case class User(id: Int, firstname: String, lastname: String, email: String, password: String, address: String, telephone: String)

final case class EmailAlreadyExistsException(private val message: String="", private val cause: Throwable=None.orNull)
    extends Exception(message, cause) 
final case class InconsistentStateException(private val message: String="", private val cause: Throwable=None.orNull)
    extends Exception(message, cause) 
final case class NotSamePasswordException(private val message: String="", private val cause: Throwable=None.orNull)
    extends Exception(message, cause) 


class Users {

type User_t = (Int, String, String, String, String, String, String)
    /*
    class UsersTable(tag: Tag) extends Table[(String, String)](tag, "users") {
        def userId = column[String]("userId", O.PrimaryKey)
        def username = column[String]("username")
        def * = (userId, username)
    }*/

    implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global
    val db = MyDatabase.db
    val users = TableQuery[UserTable]

    def createUser(id: Int, firstname: String, lastname: String, email: String, password: String, address: String, telephone: String, password_conf: String): Future[Unit] = {
        val existingUsersFuture = getUserByEmail(email)

        existingUsersFuture.flatMap(existingUsers => {
            if (existingUsers.isEmpty) {
                if(!password.equals(password_conf)) throw new NotSamePasswordException(s"Passwords are not the same.")
                val newUser = User(id, firstname=firstname, lastname=lastname, email=email, password=password, address=address, telephone=telephone)
                val newUserAsTuple: User_t = User.unapply(newUser).get

                val dbio: DBIO[Int] = users += newUserAsTuple
                var resultFuture: Future[Int] = db.run(dbio)

                // We do not care about the Int value
                resultFuture.map(_ => ())
            } else {
                throw new EmailAlreadyExistsException(s"A user with email '$email' already exists.")
            }
        })
    }

    def getUserByEmail(email: String): Future[Option[User]] = {
        val query = users.filter(_.email === email)

        val userListFuture = db.run(query.result)

        userListFuture.map((userList: Seq[User_t]) => {
            userList.length match {
                case 0 => None
                case 1 => Some(User tupled userList.head)
                case _ => throw new InconsistentStateException(s"Email $email is linked to several users in database!")
            }
        })
    }

    def getAllUsers(): Future[Seq[User]] = {
        val userListFuture = db.run(users.result)

        userListFuture.map((userList: Seq[User_t]) => {
            userList.map(User tupled _)
        })
    }
}
