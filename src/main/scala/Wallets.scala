
package poca

import scala.concurrent.Future
import slick.jdbc.PostgresProfile.api._
import java.util.UUID

case class Wallet(id: Int, solde: Int)

//final case class NameAlreadyExistsException(private val message: String="", private val cause: Throwable=None.orNull)
  //  extends Exception(message, cause)

class Wallets {

type Wallet_t = (Int, Int)
 
    implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global
    val db = MyDatabase.db
    val Wallets = TableQuery[WalletTable]

    def createWallets(id: Int, solde: Int): Future[Unit] = {
        val existingWalletsFuture = getSoldeById(id)
        
        existingWalletsFuture.flatMap(existingWallets => {
            if (existingWallets.isEmpty) {
               
                val newWallet = Wallet(id=id, solde=solde)
                val newWalletAsTuple: Wallet_t = Wallet.unapply(newWallet).get

                val dbio: DBIO[Int] = Wallets += newWalletAsTuple
                var resultFuture: Future[Int] = db.run(dbio)

                // We do not care about the Int value
                resultFuture.map(_ => ())
            }else{
                throw new NameAlreadyExistsException(s"A Wallet with id '$id' already exists. ( with solde  = $solde )")
            }
        })
    }

    def getSoldeById(id: Int): Future[Option[Wallet]] = {
        val query = Wallets.filter(_.id === id)

        val walletListFuture = db.run(query.result)

        walletListFuture.map((WalletList: Seq[Wallet_t]) => {
            WalletList.length match {
                case 0 => None
                case 1 => Some(Wallet tupled WalletList.head)
                case _ => throw new InconsistentStateException(s"Wallet $id is linked to several user in database!")
            }
        })
    }
    def getAllWallets(): Future[Seq[Wallet]] = {
        val walletListFuture = db.run(Wallets.result)

        walletListFuture.map((walletList: Seq[Wallet_t]) => {
            walletList.map(Wallet tupled _)
        })
    }

}
