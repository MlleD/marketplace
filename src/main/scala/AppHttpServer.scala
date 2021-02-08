
package poca

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration
import com.typesafe.scalalogging.LazyLogging
import ch.qos.logback.classic.{Level, Logger}
import org.slf4j.LoggerFactory
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import slick.jdbc.PostgresProfile.api._
import org.postgresql.util.PSQLException


object AppHttpServer extends LazyLogging {
    val rootLogger: Logger = LoggerFactory.getLogger("com").asInstanceOf[Logger]
    rootLogger.setLevel(Level.INFO)
    val slickLogger: Logger = LoggerFactory.getLogger("slick").asInstanceOf[Logger]
    slickLogger.setLevel(Level.INFO)

    def initDatabase() = {
        val isRunningOnCloud = sys.env.getOrElse("DB_HOST", "") != ""
        var rootConfig = ConfigFactory.load()
        val dbConfig = if (isRunningOnCloud) {
            val dbHost = sys.env.getOrElse("DB_HOST", "")
            val dbPassword = sys.env.getOrElse("DB_PASSWORD", "")

            val originalConfig = rootConfig.getConfig("cloudDB")
            originalConfig.
                withValue("properties.serverName", ConfigValueFactory.fromAnyRef(dbHost)).
                withValue("properties.password", ConfigValueFactory.fromAnyRef(dbPassword))
        } else {
            rootConfig.getConfig("localDB")
        }
        MyDatabase.initialize(dbConfig)
    }

    def main(args: Array[String]): Unit = {
        implicit val actorsSystem = ActorSystem(guardianBehavior=Behaviors.empty, name="my-system")
        implicit val actorsExecutionContext = actorsSystem.executionContext

        initDatabase
        val db = MyDatabase.db

        new RunMigrations(db)()

        var users = new Users()
        var genres = new Genres()
        var publishers = new Publishers()
        var developers = new Developers()
        var games = new Games()

        /* insert data here :*/
        
        //developers.fillDeveloperFromCSV()
        developers.createDeveloper(0, "Leo-Paul")
        developers.createDeveloper(0, "Paul")
        developers.createDeveloper(0, "Laure")
        developers.createDeveloper(0, "Dorothee")

        genres.createGenre(0 ,"ActionFake")
        genres.createGenre(0 ,"AventureFake")
        genres.createGenre(0 ,"RPGFake")
        genres.createGenre(0 ,"ReflexionFake")

        publishers.createPublisher( 0 , "PubliFake-Leo")
        publishers.createPublisher( 0 , "PubliFake-Paul")
        publishers.createPublisher( 0 , "PubliFake-Laure")
        publishers.createPublisher( 0 , "PubliFake-Dorothee")

        games.createGame(0 , "Name_Fake_theWitcher3" , "basename_fake_TW3", 2 , 2005 , "Switch" , "E" , "http://www.vgchartz.com/games/boxart/full_2258645AmericaFrontccc.jpg" , 0 ,0 )
        games.createGame(1 , "Name_Fake_theWitcher2" , "basename_fake_TW2", 2 , 2008 , "PC" , "E" , "http://www.vgchartz.com/games/boxart/full_2258645AmericaFrontccc.jpg" , 1 ,1 )
        games.createGame(2 , "Name_Fake_Mario" , "basename_fake_Mario", 1 , 2004 , "Switch" , "E" , "http://www.vgchartz.com/games/boxart/full_2258645AmericaFrontccc.jpg" , 0 ,0 )
        games.createGame(3 , "Name_Fake_Wii_Sport" , "basename_fake_Wii_Sport", 1 , 2000 , "PC" , "E" , "http://www.vgchartz.com/games/boxart/full_2258645AmericaFrontccc.jpg" , 1 ,1 )
        games.createGame(4 , "Name_Fake_Age_Of_EmpireII" , "basename_fake_Age_Of_EmpireII", 3 , 2009 , "Switch" , "E" , "http://www.vgchartz.com/games/boxart/full_2258645AmericaFrontccc.jpg" , 0 ,0 )
        games.createGame(5 , "Name_Fake_Skyrim" , "basename_fake_Skyrim", 3 , 1999 , "PC" , "E" , "http://www.vgchartz.com/games/boxart/full_2258645AmericaFrontccc.jpg" , 1 ,1 )

        /*
        genres.fillGenreFromCSV()
        publishers.fillPublisherFromCSV()
        games.fillGameFromCSV()
        */
        
        
        val routes = new Routes(users , developers , genres, publishers, games ) ; // , genres, publishers, games)

        val bindingFuture = Http().newServerAt("0.0.0.0", 8080).bind(routes.routes)

        val serverStartedFuture = bindingFuture.map(binding => {
            val address = binding.localAddress
            logger.info(s"Server online at http://${address.getHostString}:${address.getPort}/")
        })

        val waitOnFuture = serverStartedFuture.flatMap(unit => Future.never)
        
        scala.sys.addShutdownHook { 
            actorsSystem.terminate
            db.close
        }

        Await.ready(waitOnFuture, Duration.Inf)
    }
}
