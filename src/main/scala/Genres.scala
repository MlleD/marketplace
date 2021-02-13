
package poca

import scala.concurrent.Future
import slick.jdbc.PostgresProfile.api._
import java.util.UUID

case class Genre(id: Int, name: String)

//final case class NameAlreadyExistsException(private val message: String="", private val cause: Throwable=None.orNull)
  //  extends Exception(message, cause)

class Genres {

type Genre_t = (Int, String)
 
    implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global
    val db = MyDatabase.db
    val genres = TableQuery[GenreTable]

    def createGenre(id: Int, name: String): Future[Unit] = {
        val existingGenresFuture = getGenreByName(name)
        
        existingGenresFuture.flatMap(existingGenres => {
            if (existingGenres.isEmpty) {
               
                //println("on ajoute un nouveau genre dans la bd")
                val newGenre = Genre(id, name=name)
                val newGenreAsTuple: Genre_t = Genre.unapply(newGenre).get

                val dbio: DBIO[Int] = genres += newGenreAsTuple
                var resultFuture: Future[Int] = db.run(dbio)

                // We do not care about the Int value
                resultFuture.map(_ => ())
            }else{
                throw new NameAlreadyExistsException(s"A genre with name '$name' already exists.")
            }
        })
    }

/*
    def fillGenreFromCSV() = {
        //println("dans fill genre from csv")
        val db = MyDatabase.db
        val bufferedSource = io.Source.fromFile("src/main/dataset/features/genre.csv")
        
        for (line <- bufferedSource.getLines) {
            val cols = line.split(";").map(_.trim)
            
            createGenre(0, cols(1))
        }
        bufferedSource.close
    }
    */


    def getGenreByName(name: String): Future[Option[Genre]] = {
        val query = genres.filter(_.name === name)

        val genreListFuture = db.run(query.result)

        genreListFuture.map((genreList: Seq[Genre_t]) => {
            genreList.length match {
                case 0 => None
                case 1 => Some(Genre tupled genreList.head)
                case _ => throw new InconsistentStateException(s"Genre $name is linked to several Products in database!")
            }
        })
    }

    def getGenreById(id: Int): Future[Option[Genre]] = {
        val query = genres.filter(_.id === id)

        val genreListFuture = db.run(query.result)

        genreListFuture.map((genreList: Seq[Genre_t]) => {
            genreList.length match {
                case 0 => None
                case 1 => Some(Genre tupled genreList.head)
                case _ => throw new InconsistentStateException(s"Genre $id is linked to several Products in database!")
            }
        })
    }

    def getAllGenres(): Future[Seq[Genre]] = {
        val genreListFuture = db.run(genres.result)

        genreListFuture.map((genreList: Seq[Genre_t]) => {
            genreList.map(Genre tupled _)
        })
    }
}
