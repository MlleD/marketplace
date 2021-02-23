
import scala.concurrent.Future
import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.ActorSystem
import akka.http.scaladsl.testkit.RouteTestTimeout
import akka.http.scaladsl.model.{HttpRequest, StatusCodes, ContentTypes, FormData, HttpMethods}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.Matchers

import org.scalatest.funsuite.AnyFunSuite
import org.scalamock.scalatest.MockFactory
import poca.{Game, Games, Developer, Developers, Genre, Genres, Users, User, Publishers, Publisher, Comments, Comment, Routes, EmailAlreadyExistsException, NotSamePasswordException}
//import poca.{MyDatabase, Users, User, UserAlreadyExistsException, Routes}

class RoutesTest extends AnyFunSuite with Matchers with MockFactory with ScalatestRouteTest {
    // the Akka HTTP route testkit does not yet support a typed actor system (https://github.com/akka/akka-http/issues/2036)
    // so we have to adapt for now
    lazy val testKit = ActorTestKit()
    implicit def typedSystem = testKit.system
    override def createActorSystem(): akka.actor.ActorSystem =
        testKit.system.classicSystem


    test("Route GET /home should return the home page  with all the games for now") {
        val mockGames = mock[Games]
        val mockGenres = mock[Genres]
        val mockDevelopers = mock[Developers]
        val mockPublishers = mock[Publishers]
        val mockUsers = mock[Users]
        val gameList = List(
            Game(id=1, name="game1", basename="basename_game1", id_genre=1, year=2005.0, plateform="plateform1", ESRB="E", url_image="url_image1.fr", id_publisher=1, id_developer=1),
            Game(id=2, name="game2", basename="basename_game2", id_genre=2, year=2006.0, plateform="plateform2", ESRB="E", url_image="url_image2.fr", id_publisher=2, id_developer=2),
            Game(id=3, name="game3", basename="basename_game3", id_genre=3, year=2007.0, plateform="plateform3", ESRB="E", url_image="url_image3.fr", id_publisher=3, id_developer=3)
        )
        val genreList = List(
            Genre(id=1, name="genre1"),
            Genre(id=2, name="genre2"),
            Genre(id=3, name="genre3")
        )
        val developerList = List(
            Developer(id=1, name="dev1"),
            Developer(id=2, name="dev2"),
            Developer(id=3, name="dev3")
        )
        val publisherList = List(
            Publisher(id=1, name="pub1"),
            Publisher(id=2, name="pub2"),
            Publisher(id=3, name="pub3")
        )
        (mockGenres.getAllGenres _).expects().returns(Future(genreList)).once()
        (mockGames.getAllGames _).expects().returns(Future(gameList)).once()
        (mockDevelopers.getAllDevelopers _).expects().returns(Future(developerList)).once()
        (mockPublishers.getAllPublishers _).expects().returns(Future(publisherList)).once()


        
        val routesUnderTest = new Routes(mockUsers , mockDevelopers , mockGenres, mockPublishers, mockGames, null).routes

        val request = HttpRequest(uri = "/home")
        request ~> routesUnderTest ~> check {
            status should ===(StatusCodes.OK)

            contentType should ===(ContentTypes.`text/html(UTF-8)`)
        }
  }

  
    test("Route POST /register should create a new user") {
        val mockUsers = mock[Users]
        (mockUsers.createUser _)
            .expects(0, "toto", "totolast", "test@test.fr", "pwd", "address test", "0666666666", "pwd")
            .returning(Future(()))
            .once()

        val routesUnderTest = new Routes(mockUsers , null , null, null, null, null).routes

        val request = HttpRequest(
            method = HttpMethods.POST,
            uri = "/register",
            entity = FormData(("firstname","toto"),
                              ("lastname", "totolast"),
                              ("email", "test@test.fr"), 
                              ("address", "address test"),
                              ("telephone", "0666666666"),
                              ("pwd", "pwd"),
                              ("pwd_conf", "pwd")
                              ).toEntity
        )
        request ~> routesUnderTest ~> check {
            status should ===(StatusCodes.OK)

            contentType should ===(ContentTypes.`text/plain(UTF-8)`)

            entityAs[String] should ===(
                "Welcome toto totolast! You've just been registered to our great marketplace.")
        }
    }

    test("Route POST /register should warn the user when email is already taken") {
        val mockUsers = mock[Users]
        (mockUsers.createUser _)
            .expects(0, "toto", "totolast", "test@test.fr", "pwd", "address test", "0666666666", "pwd")
            .returns(Future({
        throw new EmailAlreadyExistsException("")
      }))
            .once()

        val routesUnderTest = new Routes(mockUsers , null, null, null, null, null).routes

        val request = HttpRequest(
            method = HttpMethods.POST,
            uri = "/register",
            entity = FormData(("firstname","toto"),
                              ("lastname", "totolast"),
                              ("email", "test@test.fr"), 
                              ("address", "address test"),
                              ("telephone", "0666666666"),
                              ("pwd", "pwd"),
                              ("pwd_conf", "pwd")
                              ).toEntity
        )
        request ~> routesUnderTest ~> check {
            status should ===(StatusCodes.OK)

            contentType should ===(ContentTypes.`text/plain(UTF-8)`)

            entityAs[String] should ===(
                "The email 'test@test.fr' is already taken. Please choose another email.")
        }
    }

    test("Route POST /register should warn the user when passwords are not the same") {
        val mockUsers = mock[Users]
        (mockUsers.createUser _)
            .expects(0, "toto", "totolast", "test@test.fr", "pwd", "address test", "0666666666", "pwd1")
            .returns(Future({
        throw new NotSamePasswordException("")
      }))
            .once()

        val routesUnderTest = new Routes(mockUsers , null, null, null, null, null).routes

        val request = HttpRequest(
            method = HttpMethods.POST,
            uri = "/register",
            entity = FormData(("firstname","toto"),
                              ("lastname", "totolast"),
                              ("email", "test@test.fr"), 
                              ("address", "address test"),
                              ("telephone", "0666666666"),
                              ("pwd", "pwd"),
                              ("pwd_conf", "pwd1")
                              ).toEntity
        )
        request ~> routesUnderTest ~> check {
            status should ===(StatusCodes.OK)

            contentType should ===(ContentTypes.`text/plain(UTF-8)`)

            entityAs[String] should ===(
                "The password are not the same.")
        }
    }

    test("Route GET /signin should returns the signin page") {
        val routesUnderTest = new Routes(null, null, null, null, null, null).routes

        val request = HttpRequest(uri = "/signin")
        request ~> routesUnderTest ~> check {
        status should ===(StatusCodes.OK)

        contentType should ===(ContentTypes.`text/html(UTF-8)`)
        }
  }

  test("Route GET /signup should returns the signup page") {
        val routesUnderTest = new Routes(null, null, null, null, null, null).routes

        val request = HttpRequest(uri = "/signup")
        request ~> routesUnderTest ~> check {
        status should ===(StatusCodes.OK)

        contentType should ===(ContentTypes.`text/html(UTF-8)`)
        }
  }


    test("Route GET /users should display the list of users") {
        val mockUsers = mock[Users]
        val userList = List(
            User(id=1, firstname="firstname1", lastname="lastname1", email="email1@email.fr", password="pwd1", address="address1", telephone="0111111111"),
            User(id=2, firstname="firstname2", lastname="lastname2", email="email2@email.fr", password="pwd2", address="address2", telephone="0222222222"),
            User(id=3, firstname="firstname3", lastname="lastname3", email="email3@email.fr", password="pwd3", address="address3", telephone="0333333333")
        )
        (mockUsers.getAllUsers _).expects().returns(Future(userList)).once()

        val routesUnderTest = new Routes(mockUsers , null, null, null, null, null).routes

        val request = HttpRequest(uri = "/users")
        request ~> routesUnderTest ~> check {
            status should ===(StatusCodes.OK)

            contentType should ===(ContentTypes.`text/html(UTF-8)`)
        }
    }

    test("Route GET /genres should display the list of genres") {
        val mockGenres = mock[Genres]

        val genreList = List(
            Genre(id=1, name="genre1"),
            Genre(id=2, name="genre2"),
            Genre(id=3, name="genre3")
        )
        (mockGenres.getAllGenres _).expects().returns(Future(genreList)).once()

        val routesUnderTest = new Routes(null, null , mockGenres, null, null, null).routes

        val request = HttpRequest(uri = "/genres")
        request ~> routesUnderTest ~> check {
            status should ===(StatusCodes.OK)

            contentType should ===(ContentTypes.`text/html(UTF-8)`)
        }
    }

    test("Route GET /genre when unknown genre should display the message 'This genre doesn't exist.'") {
        val mockGenres = mock[Genres]
        val expectedValue: Option[Genre] = None
        val unknownGenre: Int = 99
        (mockGenres.getGenreById _).expects(unknownGenre).returns(Future(expectedValue)).once()

        val routesUnderTest = new Routes(null, null, mockGenres, null, null, null).routes

        val request = HttpRequest(uri = "/genre?id=99")
        request ~> routesUnderTest ~> check {
            status should ===(StatusCodes.OK)
            contentType should ===(ContentTypes.`text/html(UTF-8)`)
            responseAs[String].contains("This genre doesn't exist.") should ===(true)
        }
    }


    test("Route GET /all-developers should display the list of developers") {
        val mockDevelopers = mock[Developers]
        val developerList = List(
            Developer(id=1, name="developer1"),
            Developer(id=2, name="developer2"),
            Developer(id=3, name="developer3")
        )
        (mockDevelopers.getAllDevelopers _).expects().returns(Future(developerList)).once()

        val routesUnderTest = new Routes(null , mockDevelopers , null, null, null, null).routes

        val devRoute: String = "/developer?id="
        val request = HttpRequest(uri = "/all-developers")
        request ~> routesUnderTest ~> check {
            status should ===(StatusCodes.OK)

            contentType should ===(ContentTypes.`text/html(UTF-8)`)
            responseAs[String].contains(devRoute + developerList.head.id) should ===(true)
            responseAs[String].contains(developerList.head.name) should ===(true)
            responseAs[String].contains(devRoute + developerList.tail.head.id) should ===(true)
            responseAs[String].contains(developerList.tail.head.name) should ===(true)
            responseAs[String].contains(devRoute + developerList.tail.tail.head.id) should ===(true)
            responseAs[String].contains(developerList.tail.tail.head.name) should ===(true)
        }
    }

    test("Route GET /developer when unknown developer should display the message 'This developer doesn't exist.'") {
        val mockDevelopers = mock[Developers]
        val expectedValue: Option[Developer] = None
        val unknownId: Int = 9876

        (mockDevelopers.getDeveloperById _).expects(unknownId).returns(Future(expectedValue)).once()

        val routesUnderTest = new Routes(null, mockDevelopers, null, null, null, null).routes

        val request = HttpRequest(uri = "/developer?id=" + unknownId.toString())
        request ~> routesUnderTest ~> check {
            status should ===(StatusCodes.OK)
            contentType should ===(ContentTypes.`text/html(UTF-8)`)
            responseAs[String].contains("This developer doesn't exist.") should ===(true)
        }
    }

    test("Route GET /developer when developer without game should display the message 'No game for the moment.' ") {
        val mockDevelopers = mock[Developers]
        val mockGames = mock[Games]
        val inputId: Int = 1
        val devName: String = "Bullet Proof Software"
        val developer: Developer = Developer(inputId, devName)
        val expectedValue: Option[Developer] = Some(developer)

        (mockDevelopers.getDeveloperById _).expects(inputId).returns(Future(expectedValue)).once()
        (mockGames.getGamesFromDeveloper _).expects(inputId).returns(Future(Seq())).once()

        val routesUnderTest = new Routes(null, mockDevelopers, null, null, mockGames, null).routes

        val request = HttpRequest(uri = "/developer?id=" + inputId.toString())
        request ~> routesUnderTest ~> check {
            status should ===(StatusCodes.OK)
            contentType should ===(ContentTypes.`text/html(UTF-8)`)
            responseAs[String].contains(devName) should ===(true)
            responseAs[String].contains("No game for the moment.") should ===(true)
        }
    }

    test("Route GET /developer when developer with games should display the list of its games ") {
        val mockDevelopers = mock[Developers]
        val mockGames = mock[Games]
        val inputId: Int = 14
        val devName: String = "Valve Software"
        val developer: Developer = Developer(inputId, devName)
        val expectedValue: Option[Developer] = Some(developer)
        val gid1: Int = 36
        val gname1: String = "Counter-Strike: Source"
        val gid2: Int = 55
        val gname2: String = "Portal 2"
        val gid3: Int = 103
        val gname3: String = "Half-Life 2"
        val gamesList = Seq(
            Game(gid1, gname1, "counter-strike-source", 3, 2004, "PC", "M", "http://www.vgchartz.com/games/boxart/full_9030886AmericaFrontccc.jpg", 7, 14),
            Game(gid2, gname2,"portal-2",	3, 2011, "PC", "E10", "http://www.vgchartz.com/games/boxart/full_portal-2_617AmericaFront.jpg", 10, 14),
            Game(gid3, gname3, "half-life-2", 3, 2004, "PC", "M", "http://www.vgchartz.com/games/boxart/6354662ccc.jpg", 7, 14)
        )

        (mockDevelopers.getDeveloperById _).expects(inputId).returns(Future(expectedValue)).once()
        (mockGames.getGamesFromDeveloper _).expects(inputId).returns(Future(gamesList)).once()

        val routesUnderTest = new Routes(null, mockDevelopers, null, null, mockGames, null).routes

        val productRoute: String = "/product?id="

        val request = HttpRequest(uri = "/developer?id=" + inputId.toString())
        request ~> routesUnderTest ~> check {
            status should ===(StatusCodes.OK)
            contentType should ===(ContentTypes.`text/html(UTF-8)`)
            responseAs[String].contains(devName) should ===(true)
            responseAs[String].contains(productRoute + gid1) should ===(true)
            responseAs[String].contains(gname1) should ===(true)
            responseAs[String].contains(productRoute + gid2) should ===(true)
            responseAs[String].contains(gname2) should ===(true)
            responseAs[String].contains(productRoute + gid3) should ===(true)
            responseAs[String].contains(gname3) should ===(true)
        }
    }

    test("Route GET /all-publishers should display the list of publishers") {
        val mockGames = mock[Games]
        val mockGenres = mock[Genres]
        val mockDevelopers = mock[Developers]
        val mockPublishers = mock[Publishers]
        val mockUsers = mock[Users]
        val publisherList = List(
            Publisher(id=1, name="publisher1"),
            Publisher(id=2, name="publisher2"),
            Publisher(id=3, name="publisher3")
        )
        (mockPublishers.getAllPublishers _).expects().returns(Future(publisherList)).once()

        val routesUnderTest = new Routes(mockUsers , mockDevelopers , mockGenres, mockPublishers, mockGames, null).routes

        val request = HttpRequest(uri = "/all-publishers")
        request ~> routesUnderTest ~> check {
            status should ===(StatusCodes.OK)

            contentType should ===(ContentTypes.`text/html(UTF-8)`)
        }
    }

    test("Route GET /publisher when unknown publisher should display the message 'This publisher doesn't exist.'") {
        val mockPublishers = mock[Publishers]
        val expectedValue: Option[Publisher] = None
        val unknownPublisher: String = "MM"
        (mockPublishers.getPublisherByName _).expects(unknownPublisher).returns(Future(expectedValue)).once()

        val routesUnderTest = new Routes(null, null, null, mockPublishers, null, null).routes

        val request = HttpRequest(uri = "/publisher?name=MM")
        request ~> routesUnderTest ~> check {
            status should ===(StatusCodes.OK)
            contentType should ===(ContentTypes.`text/html(UTF-8)`)
            responseAs[String].contains("This publisher doesn't exist.") should ===(true)
        }
    }

    test("Route GET /publisher when publisher without game should display the message 'No game for the moment.' ") {
        val mockPublishers = mock[Publishers]
        val mockGames = mock[Games]
        val inputName: String = "Mojang"
        val publisher: Publisher = Publisher(2, inputName)
        val expectedValue: Option[Publisher] = Some(publisher)

        (mockPublishers.getPublisherByName _).expects(inputName).returns(Future(expectedValue)).once()
        (mockGames.getGamesFromPublisher _).expects(publisher.id).returns(Future(Seq())).once()

        val routesUnderTest = new Routes(null, null, null, mockPublishers, mockGames, null).routes

        val request = HttpRequest(uri = "/publisher?name=" + inputName)
        request ~> routesUnderTest ~> check {
            status should ===(StatusCodes.OK)
            contentType should ===(ContentTypes.`text/html(UTF-8)`)
            responseAs[String].contains("No game for the moment.") should ===(true)
        }
    }

    test("Route GET /publisher when publisher with games should display the list of its games ") {
        val mockPublishers = mock[Publishers]
        val mockGames = mock[Games]
        val inputName: String = "VU Games"
        val publisher: Publisher = Publisher(7, inputName)
        val expectedValue: Option[Publisher] = Some(publisher)
        val gname1: String = "Counter-Strike: Source"
        val gid1: Int = 36
        val gid2: Int = 103
        val gname2: String = "Half-Life 2"
        val gamesList = Seq(
            Game(gid1, gname1, "counter-strike-source", 3, 2004, "PC", "M", "http://www.vgchartz.com/games/boxart/full_9030886AmericaFrontccc.jpg", 7, 14),
            Game(gid2, gname2, "half-life-2", 3, 2004, "PC", "M", "http://www.vgchartz.com/games/boxart/6354662ccc.jpg", 7, 14)
        )

        (mockPublishers.getPublisherByName _).expects(inputName).returns(Future(expectedValue)).once()
        (mockGames.getGamesFromPublisher _).expects(publisher.id).returns(Future(gamesList)).once()
        val routesUnderTest = new Routes(null, null, null, mockPublishers, mockGames, null).routes

        val request = HttpRequest(uri = "/publisher?name=" + inputName.replace(" ", "%20"))
        request ~> routesUnderTest ~> check {
            status should ===(StatusCodes.OK)
            contentType should ===(ContentTypes.`text/html(UTF-8)`)
            responseAs[String].contains(gname1) should ===(true)
            responseAs[String].contains(gname2) should ===(true)
            responseAs[String].contains("/product?id=" + gid1.toString()) should ===(true)
            responseAs[String].contains("/product?id=" + gid2.toString()) should ===(true)
        }
    }

    test("Route GET /game should display the list of games") {
        val mockGames = mock[Games]
        val mockGenres = mock[Genres]
        val mockDevelopers = mock[Developers]
        val mockPublishers = mock[Publishers]
        val mockUsers = mock[Users]
        val gameList = List(
            Game(id=1, name="game1", basename="basename_game1", id_genre=1, year=2005.0, plateform="plateform1", ESRB="E", url_image="url_image1.fr", id_publisher=1, id_developer=1),
            Game(id=2, name="game2", basename="basename_game2", id_genre=2, year=2006.0, plateform="plateform2", ESRB="E", url_image="url_image2.fr", id_publisher=2, id_developer=2),
            Game(id=3, name="game3", basename="basename_game3", id_genre=3, year=2007.0, plateform="plateform3", ESRB="E", url_image="url_image3.fr", id_publisher=3, id_developer=3)
        )
        (mockGames.getAllGames _).expects().returns(Future(gameList)).once()

        val routesUnderTest = new Routes(mockUsers , mockDevelopers , mockGenres, mockPublishers, mockGames, null).routes

        val request = HttpRequest(uri = "/game")
        request ~> routesUnderTest ~> check {
            status should ===(StatusCodes.OK)

            contentType should ===(ContentTypes.`text/html(UTF-8)`)
        }
    }
    
    test("Route GET /product?id=id should display the product") {
    val mockUsers = mock[Users]
    val mockGames = mock[Games]
    val mockGenres = mock[Genres]
    val mockDevelopers = mock[Developers]
    val mockPublishers = mock[Publishers]
    val mockComments = mock[Comments]

    val user = User(
        id=1, 
        firstname="firstname1", 
        lastname="lastname1", 
        email="email1@email.fr", 
        password="pwd1", 
        address="address1", 
        telephone="0111111111"
    )

    val genre = Genre(
        id = 1,
        name = "fake_genre_1"
    )

    val publisher = Publisher(
        id = 1,
        name = "fake_publisher_1"
    )

    val developer = Developer(
        id = 1,
        name = "fake_developer_1"
    )

    val game = Game(
        id = 1,
        name = "fake_product_1",
        basename = "basename_fake_product_1",
        id_genre = 1,
        year = 2005.0,
        plateform = "fake_plateforme",
        ESRB = "E",
        url_image = "fake_url.fr",
        id_publisher = 1,
        id_developer = 1
    )

    val commentList = List (
        Comment(
            id = 1,
            iduser = 1,
            idproduct = 1,
            comment = "fake comment",
            nbstars = 1,
            verify = false
        )
    )

    val routesUnderTest = new Routes(mockUsers, mockDevelopers , mockGenres, mockPublishers, mockGames, mockComments).routes
    (mockGames.getGameById _).expects(1).returns(Future(Some(game))).once()

    (mockDevelopers.getDeveloperById _).expects(1).returns(Future(Some(developer))).once()
    (mockPublishers.getPublisherById _).expects(1).returns(Future(Some(publisher))).once()
    (mockGenres.getGenreById _).expects(1).returns(Future(Some(genre))).once()
    (mockComments.getCommentsById _).expects(1).returns(Future(commentList)).once()

    Get("/product?id=1") ~> routesUnderTest ~> check {
      status should ===(StatusCodes.OK)
      contentType should ===(ContentTypes.`text/html(UTF-8)`)
    }
  }

}
