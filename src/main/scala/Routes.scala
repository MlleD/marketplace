
package poca

import scala.concurrent.Future
import akka.http.scaladsl.server.Directives.{path, get, post, formFieldMap, complete, concat}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.{HttpEntity, HttpResponse, ContentTypes, StatusCodes}
import com.typesafe.scalalogging.LazyLogging
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import TwirlMarshaller._


class Routes(users: Users , developers: Developers , genres: Genres, publishers: Publishers, games : Games ) extends LazyLogging {
    implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global

    def getHome() = {
        logger.info("I got a request to greet.")
        val genreSeqFuture : Future[Seq[Genre]] = genres.getAllGenres()
        genreSeqFuture.map[ToResponseMarshallable] {
            case genreSeq =>
                val gameSeqFuture: Future[Seq[Game]] = games.getAllGames()
                gameSeqFuture.map(gameSeq => html.home(gameSeq, genreSeq))
        }
    }

    def getSignup() = {
        logger.info("I got a request for signup.")
        html.signup()
    }

    def getSignin() = {
        logger.info("I got a request for signin.")
        html.signin()
    }

    def register(fields: Map[String, String]): Future[HttpResponse] = {
        logger.info("I got a request to register.")
        fields.get("email") match {
            case Some(email) => {
                var firstname = fields.get("firstname").get
                var lastname = fields.get("lastname").get
                var address = fields.get("address").get
                var phone = fields.get("telephone").get
                var pwd = fields.get("pwd").get
                var pwd_conf = fields.get("pwd_conf").get
                val userCreation: Future[Unit] = users.createUser(0, firstname=firstname, lastname=lastname, email=email, password=pwd, address=address, telephone=phone, password_conf=pwd_conf)

                userCreation.map(_ => {
                    HttpResponse(
                        StatusCodes.OK,
                        entity=s"Welcome $firstname $lastname! You've just been registered to our great marketplace.",
                    )
                }).recover({
                    case exc: EmailAlreadyExistsException => {
                        HttpResponse(
                            StatusCodes.OK,
                            entity=s"The email '$email' is already taken. Please choose another email.",
                        )
                    }
                    case exc : NotSamePasswordException => {
                        HttpResponse(
                            StatusCodes.OK,
                            entity=s"The password are not the same.",
                        )
                    }
                })
            }
        }
    }
/*
    def checkUser(fields: Map[String, String]): Future[HttpResponse] = {
        logger.info("I got a request to checkUser.")

        fields.get("email") match {
            case Some(username) => {
                val pwd = fields.get("pwd").get
                val existingUsersFuture = getUserByEmail(email)

                userCreation.map(_ => {
                    HttpResponse(
                        StatusCodes.OK,
                        entity=s"Welcome '$username'! You've just been registered to our great marketplace.",
                    )
                }).recover({
                    case exc: UserAlreadyExistsException => {
                        HttpResponse(
                            StatusCodes.OK,
                            entity=s"The username '$username' is already taken. Please choose another username.",
                        )
                    }

                    case exc: WrongPasswordException => {
                        HttpResponse(
                            StatusCodes.OK,
                            entity=s"Wrong password.",
                        )
                    }
                })
            }
            /*
            case None => {
                Future(
                    HttpResponse(
                        StatusCodes.BadRequest,
                        entity="Field 'username' not found."
                    )
                )
            }
            */
        }
    }
    */

    def getUsers() = {
        logger.info("I got a request to get user list.")

        val userSeqFuture: Future[Seq[User]] = users.getAllUsers()

        userSeqFuture.map(userSeq => html.users(userSeq))
    }
    
    def getPublishers() = {
        logger.info("I got a request to get publisher list.")

        val publisherSeqFuture: Future[Seq[Publisher]] = publishers.getAllPublishers()

        publisherSeqFuture.map(publisherSeq => html.publishers(publisherSeq))
    }
    
    def getGames() = {
        logger.info("I got a request to get game list.")

        val gameSeqFuture: Future[Seq[Game]] = games.getAllGames()

        gameSeqFuture.map(gameSeq => html.games(gameSeq))
    }
    
    def getDevelopers() = {
        logger.info("I got a request to get developer list.")
        //developers.fillDeveloperFromCSV()
        val developerSeqFuture: Future[Seq[Developer]] = developers.getAllDevelopers()

        developerSeqFuture.map(developerSeq => html.developers(developerSeq))
    }

    
    def getGenres() = {
        logger.info("I got a request to get genre list.")

        val genreSeqFuture: Future[Seq[Genre]] = genres.getAllGenres()

        genreSeqFuture.map(genreSeq => html.genres(genreSeq))    
    }

     def getGame(id : Int) = {
        logger.info("I got a request to get informations of a game.")
        
        val game = games.getGameById(id)

        game.map[ToResponseMarshallable] {
            case Some(game) => val genre = genres.getGenreById(game.id_genre)
                genre.map[ToResponseMarshallable] {
                    case Some(genre) => val dev = developers.getDeveloperById(game.id_developer)
                        dev.map[ToResponseMarshallable] {
                                case Some(dev) => html.product(game, genre, dev)
                        }
                }
                               
        }

     }
    
    
    /*def getPublishers() = {
        logger.info("I got a request to get publisher list.")

        val publisherSeqFuture: Future[Seq[Publisher]] = publishers.getAllPublishers()

        publisherSeqFuture.map(publisherSeq => html.publishers(publisherSeq))
    }*/

    val routes: Route = 
        concat(
            path("home") {
                get {
                    complete(getHome)
                }
            },
            path("signup") {
                get {
                    complete(getSignup)
                }
            },
            path("signin") {
                get {
                    complete(getSignin)
                }
            },
            path("register") {
                (post & formFieldMap) { fields =>
                    complete(register(fields))
                }
            },
            /*
            path("checkUser") {
                (post & formFieldMap) { fields =>
                    complete(checkUser(fields))
                }
            },*/
            path("users") {
                get {
                    complete(getUsers)
                }
            },
            path("developer") {
                get {
                    complete(getDevelopers)
                }
            },
            path("genre") {
                get {
                    complete(getGenres)
                }
            },
            path("publisher") {
                get {
                    complete(getPublishers)
                }
            },
            path("game") {
                get {
                    complete(getGames)
                }
            },
             path("product"){
            	get {
            		parameter('id.as[Int]) { id =>
                   	 complete(getGame(id))
                	}
            	}
            }

        )

}
