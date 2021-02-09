
package poca

import scala.concurrent.Future
import slick.jdbc.PostgresProfile.api._
import java.util.UUID


case class Developer(id: Int, name: String)

final case class NameAlreadyExistsException(private val message: String="", private val cause: Throwable=None.orNull)
    extends Exception(message, cause) 



class Developers {

type Developer_t = (Int, String)
    /*
    class UsersTable(tag: Tag) extends Table[(String, String)](tag, "users") {
        def userId = column[String]("userId", O.PrimaryKey)
        def username = column[String]("username")
        def * = (userId, username)
    }*/

    implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global
    val db = MyDatabase.db
    val developers = TableQuery[DeveloperTable]

    def createDeveloper(id: Int, name: String): Future[Unit] = {
        val existingDevelopersFuture = getDeveloperByName(name)

        existingDevelopersFuture.flatMap(existingDevelopers => {
            if (existingDevelopers.isEmpty) {
                
                val newDeveloper = Developer(id, name=name)
                val newDeveloperAsTuple: Developer_t = Developer.unapply(newDeveloper).get

                val dbio: DBIO[Int] = developers += newDeveloperAsTuple
                var resultFuture: Future[Int] = db.run(dbio)

                // We do not care about the Int value
                resultFuture.map(_ => ())
            } else {
                throw new NameAlreadyExistsException(s"A developer with name '$name' already exists.")
            }
        })
    }
    
    def fillDeveloperFromCSV() = {
        //println("dans fill dev from csv")
        val db = MyDatabase.db
        val bufferedSource = io.Source.fromFile("src/main/dataset/features/developer.csv")
        
        for (line <- bufferedSource.getLines) {
            val cols = line.split(";").map(_.trim)
            // 0 for the id due to auto incrementation
            createDeveloper(0, cols(1))
            ///println(s"${cols(0)}|${cols(1)}")
        }
        bufferedSource.close
        
    }

    def getDeveloperByName(name: String): Future[Option[Developer]] = {
        val query = developers.filter(_.name === name)

        val developerListFuture = db.run(query.result)

        developerListFuture.map((developerList: Seq[Developer_t]) => {
            developerList.length match {
                case 0 => None
                case 1 => Some(Developer tupled developerList.head)
               // case _ => throw new InconsistentStateException(s"Name $name is linked to several developers in database!")
            }
        })
    }

        def getDeveloperById(id: Int): Future[Option[Developer]] = {
        val query = developers.filter(_.id === id)

        val devListFuture = db.run(query.result)

        devListFuture.map((devList: Seq[Developer_t]) => {
            devList.length match {
                case 0 => None
                case 1 => Some(Developer tupled devList.head)
                case _ => throw new InconsistentStateException(s"Developer $id is linked to several Products in database!")
            }
        })
    }

    def getAllDevelopers(): Future[Seq[Developer]] = {
        val developerListFuture = db.run(developers.result)

        developerListFuture.map((developerList: Seq[Developer_t]) => {
            developerList.map(Developer tupled _)
        })
    }
}
