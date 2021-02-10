
package poca

import scala.concurrent.Future
import slick.jdbc.PostgresProfile.api._
import java.util.UUID

case class Publisher(id: Int, name: String)

class Publishers {

type Publisher_t = (Int, String)

    implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global
    val db = MyDatabase.db
    val publishers = TableQuery[PublisherTable]

    def createPublisher(id: Int, name: String): Future[Unit] = {
        val existingPublishersFuture = getPublisherByName(name)
        /*
        val newPublisher = Publisher(id, name=name)
        val newPublisherAsTuple: Publisher_t = Publisher.unapply(newPublisher).get

        val dbio: DBIO[Int] = publishers += newPublisherAsTuple
        var resultFuture: Future[Int] = db.run(dbio)
        println("ajout d'un publisher")
        resultFuture.map(_ => ())*/
        existingPublishersFuture.flatMap(existingPublishers => {
            if (existingPublishers.isEmpty) {
                //if(!password.equals(password_conf)) throw new NotSamePasswordException(s"Passwords are not the same.")
                val newPublisher = Publisher(id, name=name)
                val newPublisherAsTuple: Publisher_t = Publisher.unapply(newPublisher).get

                val dbio: DBIO[Int] = publishers += newPublisherAsTuple
                var resultFuture: Future[Int] = db.run(dbio)

                // We do not care about the Int value
                resultFuture.map(_ => ())
            } else {
                throw new NameAlreadyExistsException(s"A publisher with name '$name' already exists.")
            }
        })
    }

/*
     def fillPublisherFromCSV() = {
        //println("dans fill publisher from csv")
        val db = MyDatabase.db
        val bufferedSource = io.Source.fromFile("src/main/dataset/features/publisher.csv")
        
        for (line <- bufferedSource.getLines) {
            val cols = line.split(";").map(_.trim)
            // 0 for the id due auto-Incrementation
            createPublisher(0, cols(1))
            //println(s"${cols(0)}|${cols(1)}")
        }
        bufferedSource.close
    }
    */

    def getPublisherByName(name: String): Future[Option[Publisher]] = {
        val query = publishers.filter(_.name === name)

        val publisherListFuture = db.run(query.result)

        publisherListFuture.map((publisherList: Seq[Publisher_t]) => {
            publisherList.length match {
                case 0 => None
                case 1 => Some(Publisher tupled publisherList.head)
                case _ => throw new InconsistentStateException(s"Name $name is linked to several publishers in database!")
            }
        })
    }

    def getAllPublishers(): Future[Seq[Publisher]] = {
        val publisherListFuture = db.run(publishers.result)

        publisherListFuture.map((publisherList: Seq[Publisher_t]) => {
            publisherList.map(Publisher tupled _)
        })
    }
}
