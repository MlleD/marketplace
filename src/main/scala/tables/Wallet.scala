package poca

import slick.jdbc.PostgresProfile.api._

class WalletTable(tag: Tag) extends Table[(Int, Int)](tag, "Wallets") {
    def id = column[Int]("WalletId", O.PrimaryKey)
    def solde = column[Int]("WalletSolde")
    def * = (id, solde)
}
