
package poca

import scala.concurrent.Future
import slick.jdbc.PostgresProfile.api._
import java.util.UUID

case class Game(id: Int, name: String, basename: String, id_genre: Int, year: Double, plateform: String, ESRB: String, url_image: String, id_publisher: Int, id_developer: Int)

class Games {

type Game_t = (Int, String, String, Int, Double, String, String, String, Int, Int)

    implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global
    val db = MyDatabase.db
    val games = TableQuery[GameTable]

    def createGame(id: Int, name: String, basename: String, id_genre: Int, year: Double, plateform: String, ESRB: String, url_image: String, id_publisher: Int, id_developer: Int): Future[Unit] = {
        val existingGamesFuture = getGameByBaseName(basename)

        existingGamesFuture.flatMap(existingGames => {
            if (existingGames.isEmpty) {
                val newGame = Game(id, name=name, basename=basename, id_genre=id_genre,year=year, plateform=plateform, ESRB=ESRB, url_image=url_image, id_publisher=id_publisher, id_developer=id_developer)
                val newGameAsTuple: Game_t = Game.unapply(newGame).get

                val dbio: DBIO[Int] = games += newGameAsTuple
                var resultFuture: Future[Int] = db.run(dbio)
                //println(s"${basename}|${id_genre}")
                resultFuture.map(_ => ())
            } else {
                throw new NameAlreadyExistsException(s"A game with basename '$basename' already exists.")
            }
        })
    }

/*
    def fillGameFromCSV() = {
        //println("dans fill game from csv")
        val db = MyDatabase.db
        val bufferedSource = io.Source.fromFile("src/main/dataset/features/game.csv")
        
        for (line <- bufferedSource.getLines) {
            //println(cols(0), cols(1), cols(2))
            val cols = line.split(";").map(_.trim)
            //println(s"${cols(0)}|${cols(1)}|${cols(2)}|${cols(3)}|${cols(4)}|${cols(5)}|${cols(6)}|${cols(7)}|${cols(8)}|${cols(9)}")
            createGame(cols(0).toInt, cols(1), cols(2), cols(3).toInt, cols(4).toDouble, cols(5), cols(6), cols(7), cols(8).toInt, (cols(9).toDouble).toInt)
        }
        bufferedSource.close
    }
    */

    def getGameByBaseName(basename: String): Future[Option[Game]] = {
        val query = games.filter(_.basename === basename)

        val gameListFuture = db.run(query.result)

        gameListFuture.map((gameList: Seq[Game_t]) => {
            gameList.length match {
                case 0 => None
                case 1 => Some(Game tupled gameList.head)
                case _ => throw new InconsistentStateException(s"BaseName $basename is linked to several games in database!")
            }
        })
    }

    def getGameById(id: Int): Future[Option[Game]] = {
        val query = games.filter(_.id === id)

        val gameListFuture = db.run(query.result)

        gameListFuture.map((gameList: Seq[Game_t]) => {
            gameList.length match {
                case 0 => None
                case 1 => Some(Game tupled gameList.head)
                case _ => throw new InconsistentStateException(s"Game $id is linked to several Products in database!")
            }
        })
    }

    def getGamesFromPublisher(idPublisher: Int): Future[Seq[Game]] = {
        val query = games.filter(_.id_publisher === idPublisher)

        val gameListFuture = db.run(query.result)

        gameListFuture.map((gameList: Seq[Game_t]) => {
            gameList.map(Game tupled _)
        })
    }

    def getGamesFromGenre(idGenre: Int): Future[Seq[Game]] = {
        val query = games.filter(_.id_genre === idGenre)

        val gameListFuture = db.run(query.result)

        gameListFuture.map((gameList: Seq[Game_t]) => {
            gameList.map(Game tupled _)
        })
    }

    def getAllGames(): Future[Seq[Game]] = {
        val gameListFuture = db.run(games.result)

        gameListFuture.map((gameList: Seq[Game_t]) => {
            gameList.map(Game tupled _)
        })
    }
}
