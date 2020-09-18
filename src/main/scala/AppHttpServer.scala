
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration
import com.typesafe.scalalogging.LazyLogging

import scala.io.StdIn

object AppHttpServer extends LazyLogging {

    def main(args: Array[String]): Unit = {
        implicit val system = ActorSystem(guardianBehavior=Behaviors.empty, name="my-system")
        implicit val executionContext = system.executionContext

        val bindingFuture = Http().newServerAt("0.0.0.0", 8080).bind(Routes.routes)

        val serverStartedFuture = bindingFuture.map(binding => {
            val address = binding.localAddress
            logger.info(s"Server online at http://${address.getHostString}:${address.getPort}/")
        })

        val waitOnFuture = serverStartedFuture.flatMap(unit => Future.never)
        
        sys.addShutdownHook { 
            // cleanup logic
            // nothing to do for now
        }

        Await.ready(waitOnFuture, Duration.Inf)
    }
}