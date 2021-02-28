package poca

import scala.concurrent.{Future, Await}
import com.typesafe.scalalogging.LazyLogging
import scala.concurrent.duration.Duration
import slick.jdbc.PostgresProfile.api._

class Migration19CreateTableWallet(db: Database) extends Migration with LazyLogging {
    class WalletsTable(tag: Tag) extends Table[(Int, Int)](tag, "Wallets") {
        def WalletId = column[Int]("WalletId", O.PrimaryKey)
        def WalletSolde = column[Int]("WalletSolde")
        def * = (WalletId, WalletSolde)
    }

    override def apply(): Unit = {
        implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global
        val dropFuture: Future[Unit] = db.run(TableQuery[WalletsTable].schema.dropIfExists)
        dropFuture.map(t => logger.info("Done dropping table Wallets"))

        val creationFuture: Future[Unit] = db.run(TableQuery[WalletsTable].schema.createIfNotExists)
        creationFuture.onComplete(t => logger.info("Done creating table Wallets"))
        Await.result(creationFuture, Duration.Inf)
    }
}
