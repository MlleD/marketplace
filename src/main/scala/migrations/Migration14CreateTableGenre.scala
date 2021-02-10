package poca

import scala.concurrent.{Future, Await}
import com.typesafe.scalalogging.LazyLogging
import scala.concurrent.duration.Duration
import slick.jdbc.PostgresProfile.api._

class Migration14CreateTableGenre(db: Database) extends Migration with LazyLogging {
    class GenresTable(tag: Tag) extends Table[(String, String)](tag, "genres") {
        def genreId = column[String]("genreId", O.PrimaryKey)
        def genrename = column[String]("genrename")
        def * = (genreId, genrename)
    }

    override def apply(): Unit = {
        implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global
        val dropFuture: Future[Unit] = db.run(TableQuery[GenresTable].schema.dropIfExists)
        dropFuture.map(t => logger.info("Done dropping table Genres"))

        val creationFuture: Future[Unit] = db.run(TableQuery[GenreTable].schema.createIfNotExists)
        creationFuture.onComplete(t => logger.info("Done creating table Genre"))
        Await.result(creationFuture, Duration.Inf)
    }
}
