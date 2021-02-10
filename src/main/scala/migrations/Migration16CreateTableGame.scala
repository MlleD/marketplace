package poca

import scala.concurrent.{Future, Await}
import com.typesafe.scalalogging.LazyLogging
import scala.concurrent.duration.Duration
import slick.jdbc.PostgresProfile.api._

class Migration16CreateTableGame(db: Database) extends Migration with LazyLogging {
// ,name,basename,id_genre,year,plateform,ESRB,url_image,id_publisher,id_developer
    class GamesTable(tag: Tag) extends Table[(Int, String, String, Int, Double, String, String, String, Int, Int)](tag, "games") {
        def id = column[Int]("id", O.PrimaryKey)
        def name = column[String]("name",O.Length(255))
        def basename = column[String]("basename",O.Length(255))
        def id_genre = column[Int]("id_genre",O.Length(10))
        def year = column[Double]("year",O.Length(10))
        def plateform = column[String]("plateform",O.Length(255))
        def ESRB = column[String]("ESRB",O.Length(255))
        def url_image = column[String]("url_image",O.Length(255))
        def id_publisher = column[Int]("id_publisher",O.Length(10))
        def id_developer = column[Int]("id_developer",O.Length(10))
        def * = (id,name,basename,id_genre,year,plateform,ESRB,url_image,id_publisher,id_developer)
    }

    override def apply(): Unit = {
        implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global
        val dropFuture: Future[Unit] = db.run(TableQuery[GamesTable].schema.dropIfExists)
        dropFuture.map(t => logger.info("Done dropping table games"))

        val creationFuture: Future[Unit] = db.run(TableQuery[GameTable].schema.createIfNotExists)
        creationFuture.onComplete(t => logger.info("Done creating table Game"))
        Await.result(creationFuture, Duration.Inf)
    }
}
