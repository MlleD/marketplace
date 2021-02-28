
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
        var orders = new Orders()
        var orderLines = new OrderLines()
        var comments = new Comments(orders, orderLines)
        var carts = new Carts()
        var cartlines = new CartLines()
        

        
        var insertdata = new InsertData(developers, genres, publishers, games, users, comments, orders, orderLines, carts, cartlines)
        logger.info("I got a request to clear the DB.")
        insertdata.ClearDB()
        logger.info("I got a request to fill the developer table.")
        insertdata.FillDevelopers()
        logger.info("I got a request to fill the publisher table.")
        insertdata.FillPublishers()
        logger.info("I got a request to fill the genre table.")
        insertdata.FillGenre()
        logger.info("I got a request to fill the game table.")
        insertdata.FillGame()
        logger.info("I got a request to fill the user table.")
        insertdata.FillUser()
        logger.info("I got a request to fill the order table.")
        insertdata.FillOrder()
        logger.info("I got a request to fill the orderLine table.")
        insertdata.FillOrderLine()
        logger.info("I got a request to fill the comment table.")
        insertdata.FillComment()
        logger.info("I got a request to fill the cart table.")
        insertdata.FillCart()
        logger.info("I got a request to fill the cartline table.")
        insertdata.FillCartLine()

       
        
        val routes = new Routes(users , developers , genres, publishers, games, comments, carts, cartlines) ; // , genres, publishers, games)

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
