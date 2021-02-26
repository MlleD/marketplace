
package poca

import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration
import com.typesafe.scalalogging.LazyLogging
import slick.jdbc.PostgresProfile.api._
import org.postgresql.util.PSQLException

trait Migration {
    def apply(): Unit
}

class RunMigrations(db: Database) extends LazyLogging {

    /*val ResetVersion = sqlu"delete from database_version;"

    val resetFuture: Future[Int] = db.run(ResetVersion)

    val v = Await.result(resetFuture, Duration.Inf)

    val InsertVersion = sqlu"insert into database_version values (0);"

    val insertFuture: Future[Int] = db.run(InsertVersion)
    
    val v2 = Await.result(insertFuture, Duration.Inf)*/

    implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global

    val migrationList: List[Migration] = List(
        new Migration00AddVersionNumber(db),
        new Migration09CreateTableUser(db),
        //new Migration01CreateTables(db),
        new Migration15CreateTablePublisher(db),
        //new Migration02CreateTableManufacturer(db),
        //new Migration03CreateTableProduct(db),
        //new Migration04CreateTableCategory(db),
        //new Migration05CreateTableProductCategory(db),
        //new Migration06CreateTableReseller(db),
        //new Migration07CreateTableProductReseller(db),
        //new Migration08CreateTableOrderStatus(db),
        new Migration10CreateTableComment(db),
        new Migration11CreateTableOrder(db),
        new Migration12CreateTableOrderLine(db),
        new Migration13CreateTableDeveloper(db),
        new Migration14CreateTableGenre(db),
        new Migration16CreateTableGame(db),
        //new Migration01CreateTables(db),
        new Migration17CreateTableCart(db),
    )

    def getCurrentDatabaseVersion(): Int = {
        val getVersionRequest: DBIO[Seq[Int]] = sql"select * from database_version;".as[Int]
        val responseFuture: Future[Seq[Int]] = db.run(getVersionRequest)

        val versionFuture = responseFuture.
            map(versionSeq => versionSeq(0)).
            recover{
                case exc: PSQLException => {
                    if (exc.toString.contains("ERROR: relation \"database_version\" does not exist")) {
                        0
                    } else {
                        throw exc
                    }
                }
            }
        
        val version = Await.result(versionFuture, Duration.Inf)
        logger.info(s"Database version is $version")
        version
    }

    def incrementDatabaseVersion(): Unit = {
        val oldVersion = getCurrentDatabaseVersion()
        val newVersion = oldVersion + 1

        val updateVersionRequest: DBIO[Int] = sqlu"update database_version set number = ${newVersion};"

        val updateVersionFuture: Future[Int] = db.run(updateVersionRequest)

        Await.result(updateVersionFuture, Duration.Inf)
        logger.info(s"Database version incremented from $oldVersion to $newVersion")
    }

    def apply(): Unit = {
        val version = getCurrentDatabaseVersion()

        migrationList.slice(version, migrationList.length).foreach(migration => {
            migration()
            incrementDatabaseVersion()
        })
    }
}
