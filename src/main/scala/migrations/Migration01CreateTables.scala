
package poca

import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration
import com.typesafe.scalalogging.LazyLogging
import slick.jdbc.PostgresProfile.api._


class Migration01CreateTables(db: Database) extends Migration with LazyLogging {
    class CurrentUsersTable(tag: Tag) extends Table[(String, String)](tag, "users") {
        def userId = column[String]("userId", O.PrimaryKey)
        def username = column[String]("username")
        def * = (userId, username)
    }
/*
    class CurrentGenresTable(tag: Tag) extends Table[(String, String)](tag, "genres") {
        def genreId = column[String]("genreId", O.PrimaryKey)
        def genrename = column[String]("genrename",O.Length(255))
        def * = (genreId, genrename)
    }

    class CurrentDevelopersTable(tag: Tag) extends Table[(String, String)](tag, "developers") {
        def developerId = column[String]("Id", O.PrimaryKey)
        def developername = column[String]("name",O.Length(255))
        def * = (developerId, developername)
    }
*/
    override def apply(): Unit = {
        implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global
        
        val users = TableQuery[CurrentUsersTable]
        val dbio: DBIO[Unit] = users.schema.create
        val creationFuture: Future[Unit] = db.run(dbio)
        Await.result(creationFuture, Duration.Inf)
        logger.info("Done creating table Users")
/*
        val genres = TableQuery[CurrentGenresTable]
        val dbio_1: DBIO[Unit] = genres.schema.create
        val creationFuture_1: Future[Unit] = db.run(dbio_1)
        Await.result(creationFuture_1, Duration.Inf)
        logger.info("Done creating table Genres")

        val developers = TableQuery[CurrentDevelopersTable]
        val dbio_2: DBIO[Unit] = developers.schema.create
        val creationFuture_2: Future[Unit] = db.run(dbio_2)
        Await.result(creationFuture_2, Duration.Inf)
        logger.info("Done creating table Developers")
*/    }
}
