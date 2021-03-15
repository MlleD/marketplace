import scala.util.{Success, Failure}
import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.meta._
import org.scalatest.{Matchers, BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatest.funsuite.AnyFunSuite
import com.typesafe.scalalogging.LazyLogging
import com.typesafe.config.{ConfigFactory}
import ch.qos.logback.classic.{Level, Logger}
import org.slf4j.LoggerFactory
import java.sql.Timestamp
import java.time.format.DateTimeFormatter
import java.time.ZoneId
import java.time.LocalDateTime
import poca.{
    MyDatabase,
    Users, User, Games, Game, Developers, Developer, Genres, Genre, Publishers, Publisher,
    Comments, Comment, Order, Orders, OrderLine, OrderLines, InsertData, Wallet , Wallets ,
    Cart, Carts, CartLine, CartLines,
    NotSamePasswordException, EmailAlreadyExistsException, NameAlreadyExistsException,
    IdAlreadyExistsException, IdUserAlreadyExistsException,
    RunMigrations}

class DatabaseTest extends AnyFunSuite 
                   with Matchers with BeforeAndAfterAll with BeforeAndAfterEach with LazyLogging {
    val rootLogger: Logger = LoggerFactory.getLogger("com").asInstanceOf[Logger]
    rootLogger.setLevel(Level.INFO)
    val slickLogger: Logger = LoggerFactory.getLogger("slick").asInstanceOf[Logger]
    slickLogger.setLevel(Level.INFO)

    // In principle, mutable objets should not be shared between tests, because tests should be 
    // independent from each other. However for performance the connection to the database should 
    // not be recreated for each test. Here we prefer to share the database.
    override def beforeAll() {
        val isRunningOnCI = sys.env.getOrElse("CI", "") != ""
        val configName = if (isRunningOnCI) "myTestDBforCI" else "myTestDB"
        val config = ConfigFactory.load().getConfig(configName)
        MyDatabase.initialize(config)
    }
    override def afterAll() {
        MyDatabase.db.close
    }

    override def beforeEach() {
        val resetSchema = sqlu"drop schema public cascade; create schema public;"
        val resetFuture: Future[Int] = MyDatabase.db.run(resetSchema)
        Await.result(resetFuture, Duration.Inf)
        new RunMigrations(MyDatabase.db)()
    }

    test("Users.createUser should create a new user") {
        val users: Users = new Users()
        val marie: User = new User(
            1, "marie", "dupont", "dupont@gmail.fr", "md", "3 rue des tulipes, 75011, France", "0134765980"
        )
        val createUserFuture: Future[Unit] = users.createUser(
            marie.id, marie.firstname, marie.lastname, marie.email, marie.password, 
            marie.address, marie.telephone, marie.password
        )
        Await.ready(createUserFuture, Duration.Inf)

        // Check that the future succeeds
        createUserFuture.value should be(Some(Success(())))

        val getUsersFuture: Future[Seq[User]] = users.getAllUsers()
        var allUsers: Seq[User] = Await.result(getUsersFuture, Duration.Inf)

        allUsers.length should be(1)
        allUsers.head should be(marie)
    }

    test("Users.createUser returned future should fail if the email already exists") {
        val users: Users = new Users()
        val marie: User = new User(
            1, "marie", "dupont", "dupont@gmail.fr", "md", "3 rue des tulipes, 75011, France", "0134765980"
        )
        val createUserFuture: Future[Unit] = users.createUser(
            marie.id, marie.firstname, marie.lastname, marie.email, marie.password, 
            marie.address, marie.telephone, marie.password
        )
        Await.ready(createUserFuture, Duration.Inf)

        val theo: User = new User(
            2,"theo", marie.lastname, marie.email, "td", marie.address, "0134765786"
        )
        val createDuplicateUserEmailFuture: Future[Unit] = users.createUser(
            theo.id, theo.firstname, theo.lastname, theo.email, theo.password, 
            theo.address, theo.telephone, theo.password
        )
        Await.ready(createDuplicateUserEmailFuture, Duration.Inf)

        createDuplicateUserEmailFuture.value match {
            case Some(Failure(exc: EmailAlreadyExistsException)) => {
                exc.getMessage should equal ("A user with email '" + marie.email + "' already exists.")
            }
            case _ => fail("The future should fail.")
        }
    }

    test("Users.createUser returned future should fail if password confirmation not equals to password") {
        val users: Users = new Users()
        val marie: User = new User(
            1,"marie", "dupont", "dupont@gmail.fr", "md", "3 rue des tulipes, 75011, France", "0134765980"
        )
        val createUserFuture: Future[Unit] = users.createUser(
            marie.id, marie.firstname, marie.lastname, marie.email, marie.password, 
            marie.address, marie.telephone, marie.password + "d"
        )
        Await.ready(createUserFuture, Duration.Inf)


        createUserFuture.value match {
            case Some(Failure(exc: NotSamePasswordException)) => {
                exc.getMessage should equal ("Passwords are not the same.")
            }
            case _ => fail("The future should fail.")
        }
    }

    test("Users.getUserByUsername should return no user if it does not exist") {
        val users: Users = new Users()

        val marie: User = new User(
            1,"marie", "dupont", "dupont@gmail.fr", "md", "3 rue des tulipes, 75011, France", "0134765980"
        )
        val createUserFuture: Future[Unit] = users.createUser(
            marie.id, marie.firstname, marie.lastname, marie.email, marie.password, 
            marie.address, marie.telephone, marie.password
        )
        Await.ready(createUserFuture, Duration.Inf)

        val returnedUserFuture: Future[Option[User]] = users.getUserByEmail("durand@gmail.fr")
        val returnedUser: Option[User] = Await.result(returnedUserFuture, Duration.Inf)

        returnedUser should be(None)
    }

    test("Users.getUserByUsername should return a user") {
        val users: Users = new Users()

        val marie: User = new User(
            1,"marie", "dupont", "dupont@gmail.fr", "md", "3 rue des tulipes, 75011, France", "0134765980"
        )
        val createUserFuture: Future[Unit] = users.createUser(
            marie.id, marie.firstname, marie.lastname, marie.email, marie.password, 
            marie.address, marie.telephone, marie.password
        )
        Await.ready(createUserFuture, Duration.Inf)

        val returnedUserFuture: Future[Option[User]] = users.getUserByEmail(marie.email)
        val returnedUser: Option[User] = Await.result(returnedUserFuture, Duration.Inf)

        returnedUser match {
            case Some(user) => user should be(marie)
            case None => fail("Should return a user.")
        }
    }

    test("Users.getAllUsers should return a list of users") {
        val users: Users = new Users()

        val marie: User = new User(
            1,"marie", "dupont", "m.dupont@gmail.fr", "md", "3 rue des tulipes, 75011, France", "0134765980"
        )
        val createUserFuture: Future[Unit] = users.createUser(
            marie.id, marie.firstname, marie.lastname, marie.email, marie.password, 
            marie.address, marie.telephone, marie.password
        )
        Await.ready(createUserFuture, Duration.Inf)

        val theo: User = new User(
            2,"theo", marie.lastname, "t.dupont@gmail.fr", "td", marie.address, "0134765786"
        )
        val createAnotherUserFuture: Future[Unit] = users.createUser(
            theo.id, theo.firstname, theo.lastname, theo.email, theo.password, 
            theo.address, theo.telephone, theo.password
        )
        Await.ready(createAnotherUserFuture, Duration.Inf)

        val returnedUserSeqFuture: Future[Seq[User]] = users.getAllUsers()
        val returnedUserSeq: Seq[User] = Await.result(returnedUserSeqFuture, Duration.Inf)

        returnedUserSeq.length should be(2)
        returnedUserSeq(0) should be(marie)
        returnedUserSeq(1) should be(theo)
    }

    test("Users.getEmailFromUser should return None if the user doesn't exist") {
        val users: Users = new Users()
        val noemail: Option[String] = Await.result(users.getEmailFromUser(5), Duration.Inf)
        noemail should be(None)
    }

    test("Users.getEmailFromUser should the email if the user exists") {
        val users: Users = new Users()
        val id: Int = 1
        val email: String = "dr@gmail.com"
        val createFuture: Future[Unit] = users.createUser(
            id, "Dana", "Reyes", email, "drpw",
            "5 rue du soleil", "0294570644", "drpw")
        Await.ready(createFuture, Duration.Inf)

        val result_email: Option[String] = Await.result(users.getEmailFromUser(id), Duration.Inf)
        result_email should be(Some(email))
    }


    // -------------------------- GAME ---------------------------------

    test("Games.createGame should create a new game") {
        val games: Games = new Games()
        val fake: Game = new Game(
            1, "fake_product_1", "basename_fake_product_1", 1, 2005.0, "fake_plateforme", "E", "fake_url.fr", 1, 1
        )
        val createGameFuture: Future[Unit] = games.createGame(
            fake.id, fake.name, fake.basename, fake.id_genre, fake.year, 
            fake.plateform, fake.ESRB, fake.url_image, fake.id_publisher, fake.id_developer
        )
        Await.ready(createGameFuture, Duration.Inf)

        // Check that the future succeeds
        createGameFuture.value should be(Some(Success(())))

        val getGamesFuture: Future[Seq[Game]] = games.getAllGames()
        var allGames: Seq[Game] = Await.result(getGamesFuture, Duration.Inf)

        allGames.length should be(1)
        allGames.head should be(fake)
    }

    test("Games.createGame returned future should fail if game basename already exists") {
        val games: Games = new Games()
        val fake1: Game = new Game(
            1, "fake_product_1", "basename_fake_product_1", 1, 2005.0, "fake_plateforme",
            "E", "fake_url.fr", 1, 1
        )
        val createGame1Future: Future[Unit] = games.createGame(
            fake1.id, fake1.name, fake1.basename, fake1.id_genre, fake1.year, 
            fake1.plateform, fake1.ESRB, fake1.url_image, fake1.id_publisher, fake1.id_developer
        )
        
        Await.ready(createGame1Future, Duration.Inf)
        createGame1Future.value should be(Some(Success(())))
        
        val fake2: Game = new Game(
            fake1.id + 1, fake1.name+ "0", fake1.basename, fake1.id_genre + 1, fake1.year + 8.0,
            fake1.plateform, fake1.ESRB, "2" + fake1.url_image, fake1.id_publisher, fake1.id_developer
        )
        val createGame2Future: Future[Unit] = games.createGame(
            fake2.id, fake2.name, fake2.basename, fake2.id_genre, fake2.year, 
            fake2.plateform, fake2.ESRB, fake2.url_image, fake2.id_publisher, fake2.id_developer
        )
        Await.ready(createGame2Future, Duration.Inf)

        createGame2Future.value match {
            case Some(Failure(exc: NameAlreadyExistsException)) => {
                exc.getMessage should equal("A game with basename '" + fake1.basename + "' already exists.")
            }
            case _ => fail("The future should fail.")
        }
    }

    test("Games.getGameById should return no id if it does not exist") {
        val games: Games = new Games()

        val fake_game: Game = new Game(
            1, "fake_product_1", "basename_fake_product_1", 1, 2005.0, "fake_plateforme", "E", "fake_url.fr", 1, 1
        )
        val createGameFuture: Future[Unit] = games.createGame(
            fake_game.id, fake_game.name, fake_game.basename, fake_game.id_genre, fake_game.year, 
            fake_game.plateform, fake_game.ESRB, fake_game.url_image, fake_game.id_publisher, fake_game.id_developer
        )
        Await.ready(createGameFuture, Duration.Inf)

        val returnedGameFuture: Future[Option[Game]] = games.getGameById(0)
        val returnedGame: Option[Game] = Await.result(returnedGameFuture, Duration.Inf)

        returnedGame should be(None)
    }

    test("Games.getGameById should return a game") {
        val games: Games = new Games()

        val fake_game: Game = new Game(
            1, "fake_product_1", "basename_fake_product_1", 1, 2005.0, "fake_plateforme", "E", "fake_url.fr", 1, 1
        )
        val createGameFuture: Future[Unit] = games.createGame(
            fake_game.id, fake_game.name, fake_game.basename, fake_game.id_genre, fake_game.year, 
            fake_game.plateform, fake_game.ESRB, fake_game.url_image, fake_game.id_publisher, fake_game.id_developer
        )
        Await.ready(createGameFuture, Duration.Inf)

        val returnedGameFuture: Future[Option[Game]] = games.getGameById(1)
        val returnedGame: Option[Game] = Await.result(returnedGameFuture, Duration.Inf)

        returnedGame match {
            case Some(game) => game should be(fake_game)
            case None => fail("Should return a game.")
        }
    }

    test("Games.getGameByBaseName should return no id if it does not exist") {
        val games: Games = new Games()

        val fake_game: Game = new Game(
            1, "fake_product_1", "basename_fake_product_1", 1, 2005.0, "fake_plateforme", "E", "fake_url.fr", 1, 1
        )
        val createGameFuture: Future[Unit] = games.createGame(
            fake_game.id, fake_game.name, fake_game.basename, fake_game.id_genre, fake_game.year, 
            fake_game.plateform, fake_game.ESRB, fake_game.url_image, fake_game.id_publisher, fake_game.id_developer
        )
        Await.ready(createGameFuture, Duration.Inf)

        val returnedGameFuture: Future[Option[Game]] = games.getGameByBaseName("basename_fake_product_0")
        val returnedGame: Option[Game] = Await.result(returnedGameFuture, Duration.Inf)

        returnedGame should be(None)
    }

    test("Games.getGameByBaseName should return a game") {
        val games: Games = new Games()

        val fake_game: Game = new Game(
            1, "fake_product_1", "basename_fake_product_1", 1, 2005.0, "fake_plateforme", "E", "fake_url.fr", 1, 1
        )
        val createGameFuture: Future[Unit] = games.createGame(
            fake_game.id, fake_game.name, fake_game.basename, fake_game.id_genre, fake_game.year, 
            fake_game.plateform, fake_game.ESRB, fake_game.url_image, fake_game.id_publisher, fake_game.id_developer
        )
        Await.ready(createGameFuture, Duration.Inf)

        val returnedGameFuture: Future[Option[Game]] = games.getGameByBaseName("basename_fake_product_1")
        val returnedGame: Option[Game] = Await.result(returnedGameFuture, Duration.Inf)

        returnedGame match {
            case Some(game) => game should be(fake_game)
            case None => fail("Should return a game.")
        }
    }

    test("Games.getAllGames should return a list of games") {
        val games: Games = new Games()

        val fake_game: Game = new Game(
            1, "fake_product_1", "basename_fake_product_1", 1, 2005.0, "fake_plateforme", "E", "fake_url.fr", 1, 1
        )
        val createGameFuture: Future[Unit] = games.createGame(
            fake_game.id, fake_game.name, fake_game.basename, fake_game.id_genre, fake_game.year, 
            fake_game.plateform, fake_game.ESRB, fake_game.url_image, fake_game.id_publisher, fake_game.id_developer
        )
        Await.ready(createGameFuture, Duration.Inf)

        val fake_game2: Game = new Game(
            2, "fake_product_2", "basename_fake_product_2", 1, 2005.0, "fake2_plateforme", "E", "fake2_url.fr", 1, 1
        )
        val createAnotherGameFuture: Future[Unit] = games.createGame(
            fake_game2.id, fake_game2.name, fake_game2.basename, fake_game2.id_genre, fake_game2.year, 
            fake_game2.plateform, fake_game2.ESRB, fake_game2.url_image, fake_game2.id_publisher, fake_game2.id_developer
        )
        Await.ready(createAnotherGameFuture, Duration.Inf)

        val returnedGameSeqFuture: Future[Seq[Game]] = games.getAllGames()
        val returnedGameSeq: Seq[Game] = Await.result(returnedGameSeqFuture, Duration.Inf)

        returnedGameSeq.length should be(2)
        returnedGameSeq(0) should be(fake_game)
        returnedGameSeq(1) should be(fake_game2)
    }



    // -------------------------- Developer ---------------------------------

    test("Developers.createDeveloper should create a new developer") {
        val developers: Developers = new Developers()
        val fake_dev: Developer = new Developer(
            1, "fake_dev_name"
        )
        val createDeveloperFuture: Future[Unit] = developers.createDeveloper(
            fake_dev.id, fake_dev.name
        )
        Await.ready(createDeveloperFuture, Duration.Inf)

        // Check that the future succeeds
        createDeveloperFuture.value should be(Some(Success(())))

        val getDevelopersFuture: Future[Seq[Developer]] = developers.getAllDevelopers()
        var allDevelopers: Seq[Developer] = Await.result(getDevelopersFuture, Duration.Inf)

        allDevelopers.length should be(1)
        allDevelopers.head should be(fake_dev)
    }

    test("Developers.createDeveloper returned future should fail if name already exists") {
        val developers: Developers = new Developers()

        val fake_dev: Developer = new Developer(
            1, "fake_dev_name"
        )
        val createDeveloperFuture: Future[Unit] = developers.createDeveloper(
            fake_dev.id, fake_dev.name
        )
        Await.ready(createDeveloperFuture, Duration.Inf)

        // Check that the future succeeds
        createDeveloperFuture.value should be(Some(Success(())))

        val fake_dev2: Developer = new Developer(
            fake_dev.id + 1, fake_dev.name
        )
        val createDoubleDeveloperFuture: Future[Unit] = developers.createDeveloper(
            fake_dev2.id, fake_dev2.name
        )
        Await.ready(createDoubleDeveloperFuture, Duration.Inf)

        createDoubleDeveloperFuture.value match {
            case Some(Failure(exc: NameAlreadyExistsException)) => {
                exc.getMessage should equal("A developer with name '" + fake_dev2.name + "' already exists.")
            }
            case _ => fail("The future should fail.")
        }

    }

    test("Developers.getDeveloperById should return no id if it does not exist") {
        val developers: Developers = new Developers()

        val fake_dev: Developer = new Developer(
            1, "fake_dev_name"
        )
        val createDeveloperFuture: Future[Unit] = developers.createDeveloper(
            fake_dev.id, fake_dev.name
        )
        Await.ready(createDeveloperFuture, Duration.Inf)

        // Check that the future succeeds
        createDeveloperFuture.value should be(Some(Success(())))

        val returnedDevelopersFuture: Future[Option[Developer]] = developers.getDeveloperById(0)
        var returnedDeveloper: Option[Developer] = Await.result(returnedDevelopersFuture, Duration.Inf)

        returnedDeveloper should be(None)
    }

    test("Developers.getDeveloperById should return a developer") {
        val developers: Developers = new Developers()

        val fake_dev: Developer = new Developer(
            1, "fake_dev_name"
        )
        val createDeveloperFuture: Future[Unit] = developers.createDeveloper(
            fake_dev.id, fake_dev.name
        )
        Await.ready(createDeveloperFuture, Duration.Inf)

        // Check that the future succeeds
        createDeveloperFuture.value should be(Some(Success(())))

        val returnedDevelopersFuture: Future[Option[Developer]] = developers.getDeveloperById(1)
        var returnedDeveloper: Option[Developer] = Await.result(returnedDevelopersFuture, Duration.Inf)

        returnedDeveloper match {
            case Some(developer) => developer should be(fake_dev)
            case None => fail("Should return a developer.")
        }
    }

    test("Developers.getDeveloperByName should return no id if it does not exist") {
        val developers: Developers = new Developers()

        val fake_dev: Developer = new Developer(
            1, "fake_dev_name"
        )
        val createDeveloperFuture: Future[Unit] = developers.createDeveloper(
            fake_dev.id, fake_dev.name
        )
        Await.ready(createDeveloperFuture, Duration.Inf)

        // Check that the future succeeds
        createDeveloperFuture.value should be(Some(Success(())))

        val returnedDevelopersFuture: Future[Option[Developer]] = developers.getDeveloperByName("fake_dev_name0")
        var returnedDeveloper: Option[Developer] = Await.result(returnedDevelopersFuture, Duration.Inf)

        returnedDeveloper should be(None)
    }

    test("Developers.getDeveloperByName should return a developer") {
        val developers: Developers = new Developers()

        val fake_dev: Developer = new Developer(
            1, "fake_dev_name"
        )
        val createDeveloperFuture: Future[Unit] = developers.createDeveloper(
            fake_dev.id, fake_dev.name
        )
        Await.ready(createDeveloperFuture, Duration.Inf)

        // Check that the future succeeds
        createDeveloperFuture.value should be(Some(Success(())))

        val returnedDevelopersFuture: Future[Option[Developer]] = developers.getDeveloperByName("fake_dev_name")
        var returnedDeveloper: Option[Developer] = Await.result(returnedDevelopersFuture, Duration.Inf)

        returnedDeveloper match {
            case Some(developer) => developer should be(fake_dev)
            case None => fail("Should return a developer.")
        }
    }

    test("Games.getAllDevelopers should return a list of developers") {
        val developers: Developers = new Developers()

        val fake_dev: Developer = new Developer(
            1, "fake_dev_name"
        )
        val createDeveloperFuture: Future[Unit] = developers.createDeveloper(
            fake_dev.id, fake_dev.name
        )
        Await.ready(createDeveloperFuture, Duration.Inf)

        val fake_dev2: Developer = new Developer(
            2, "fake_dev_name2"
        )
        val createAnotherDeveloperFuture: Future[Unit] = developers.createDeveloper(
            fake_dev2.id, fake_dev2.name
        )
        Await.ready(createAnotherDeveloperFuture, Duration.Inf)


        val returnedDeveloperSeqFuture: Future[Seq[Developer]] = developers.getAllDevelopers()
        val returnedDeveloperSeq: Seq[Developer] = Await.result(returnedDeveloperSeqFuture, Duration.Inf)

        returnedDeveloperSeq.length should be(2)
        returnedDeveloperSeq(0) should be(fake_dev)
        returnedDeveloperSeq(1) should be(fake_dev2)
    }


    // -------------------------- GENRE ---------------------------------

    test("Genres.createGenre should create a new genre") {
        val genres: Genres = new Genres()
        val fake_genre: Genre = new Genre(
            1, "fake_genre_name"
        )
        val createGenreFuture: Future[Unit] = genres.createGenre(
            fake_genre.id, fake_genre.name
        )
        Await.ready(createGenreFuture, Duration.Inf)

        // Check that the future succeeds
        createGenreFuture.value should be(Some(Success(())))

        val getGenresFuture: Future[Seq[Genre]] = genres.getAllGenres()
        var allGenres: Seq[Genre] = Await.result(getGenresFuture, Duration.Inf)

        allGenres.length should be(1)
        allGenres.head should be(fake_genre)
    }

    test("Genres.createGenre returned future should fail if name already exists") {
        val genres: Genres = new Genres()
        val mygenre: Genre = new Genre(
            1, "Action"
        )
        val createGenreFuture: Future[Unit] = genres.createGenre(
            mygenre.id, mygenre.name
        )
        Await.ready(createGenreFuture, Duration.Inf)

        // Check that the future succeeds
        createGenreFuture.value should be(Some(Success(())))

        val double: Genre = new Genre(
            mygenre.id + 1, mygenre.name
        )
        val createGenreDoubleFuture: Future[Unit] = genres.createGenre(
            double.id, double.name
        )
        Await.ready(createGenreDoubleFuture, Duration.Inf)

        createGenreDoubleFuture.value match {
            case Some(Failure(exc: NameAlreadyExistsException)) => {
                exc.getMessage should equal("A genre with name '" + mygenre.name + "' already exists.")
            }
            case _ => fail("The future should fail.")
        }

        val getGenresFuture: Future[Seq[Genre]] = genres.getAllGenres()
        var allGenres: Seq[Genre] = Await.result(getGenresFuture, Duration.Inf)

        allGenres.length should be(1)
        allGenres.head should be(mygenre)
    }

    test("Genres.getGenreById should return no id if it does not exist") {
        val genres: Genres = new Genres()

        val fake_genre: Genre = new Genre(
            1, "fake_genre_name"
        )
        val createGenreFuture: Future[Unit] = genres.createGenre(
            fake_genre.id, fake_genre.name
        )
        Await.ready(createGenreFuture, Duration.Inf)

        // Check that the future succeeds
        createGenreFuture.value should be(Some(Success(())))

        val returnedGenresFuture: Future[Option[Genre]] = genres.getGenreById(0)
        var returnedGenre: Option[Genre] = Await.result(returnedGenresFuture, Duration.Inf)

        returnedGenre should be(None)
    }

    test("Genres.getGenreById should return a genre") {
        val genres: Genres = new Genres()

        val fake_genre: Genre = new Genre(
            1, "fake_genre_name"
        )
        val createGenreFuture: Future[Unit] = genres.createGenre(
            fake_genre.id, fake_genre.name
        )
        Await.ready(createGenreFuture, Duration.Inf)

        // Check that the future succeeds
        createGenreFuture.value should be(Some(Success(())))

        val returnedGenresFuture: Future[Option[Genre]] = genres.getGenreById(1)
        var returnedGenre: Option[Genre] = Await.result(returnedGenresFuture, Duration.Inf)

        returnedGenre match {
            case Some(genre) => genre should be(fake_genre)
            case None => fail("Should return a genre.")
        }
    }

    test("Genres.getGenreByName should return no id if it does not exist") {
        val genres: Genres = new Genres()

        val fake_genre: Genre = new Genre(
            1, "fake_genre_name"
        )
        val createGenreFuture: Future[Unit] = genres.createGenre(
            fake_genre.id, fake_genre.name
        )
        Await.ready(createGenreFuture, Duration.Inf)

        // Check that the future succeeds
        createGenreFuture.value should be(Some(Success(())))

        val returnedGenresFuture: Future[Option[Genre]] = genres.getGenreByName("fake_genre_name0")
        var returnedGenre: Option[Genre] = Await.result(returnedGenresFuture, Duration.Inf)

        returnedGenre should be(None)
    }

    test("Genres.getGenreByName should return a genre") {
        val genres: Genres = new Genres()

        val fake_genre: Genre = new Genre(
            1, "fake_genre_name"
        )
        val createGenreFuture: Future[Unit] = genres.createGenre(
            fake_genre.id, fake_genre.name
        )
        Await.ready(createGenreFuture, Duration.Inf)

        // Check that the future succeeds
        createGenreFuture.value should be(Some(Success(())))

        val returnedGenresFuture: Future[Option[Genre]] = genres.getGenreByName("fake_genre_name")
        var returnedGenre: Option[Genre] = Await.result(returnedGenresFuture, Duration.Inf)

        returnedGenre match {
            case Some(genre) => genre should be(fake_genre)
            case None => fail("Should return a genre.")
        }
    }

    test("Genres.getAllGenres should return a list of genres") {
        val genres: Genres = new Genres()

        val fake_genre: Genre = new Genre(
            1, "fake_genre_name"
        )
        val createGenreFuture: Future[Unit] = genres.createGenre(
            fake_genre.id, fake_genre.name
        )
        Await.ready(createGenreFuture, Duration.Inf)

        val fake_genre2: Genre = new Genre(
            2, "fake_genre_name2"
        )
        val createAnotherGenreFuture: Future[Unit] = genres.createGenre(
            fake_genre2.id, fake_genre2.name
        )
        Await.ready(createAnotherGenreFuture, Duration.Inf)


        val returnedGenreSeqFuture: Future[Seq[Genre]] = genres.getAllGenres()
        val returnedGenreSeq: Seq[Genre] = Await.result(returnedGenreSeqFuture, Duration.Inf)

        returnedGenreSeq.length should be(2)
        returnedGenreSeq(0) should be(fake_genre)
        returnedGenreSeq(1) should be(fake_genre2)
    }


    // -------------------------- PUBLISHER ---------------------------------

    test("Publishers.createPublisher should create a new publisher") {
        val publishers: Publishers = new Publishers()
        val fake_publisher: Publisher = new Publisher(
            1, "fake_publisher_name"
        )
        val createPublisherFuture: Future[Unit] = publishers.createPublisher(
            fake_publisher.id, fake_publisher.name
        )
        Await.ready(createPublisherFuture, Duration.Inf)

        // Check that the future succeeds
        createPublisherFuture.value should be(Some(Success(())))

        val getPublishersFuture: Future[Seq[Publisher]] = publishers.getAllPublishers()
        var allPublishers: Seq[Publisher] = Await.result(getPublishersFuture, Duration.Inf)

        allPublishers.length should be(1)
        allPublishers.head should be(fake_publisher)
    }

    test("Publishers.createPublisher returned future should fail if name already exists") {
        val publishers: Publishers = new Publishers()
        val mypublisher: Publisher = new Publisher(
            1, "Action"
        )
        val createPublisherFuture: Future[Unit] = publishers.createPublisher(
            mypublisher.id, mypublisher.name
        )
        Await.ready(createPublisherFuture, Duration.Inf)

        // Check that the future succeeds
        createPublisherFuture.value should be(Some(Success(())))

        val double: Publisher = new Publisher(
            mypublisher.id + 1, mypublisher.name
        )
        val createPublisherDoubleFuture: Future[Unit] = publishers.createPublisher(
            double.id, double.name
        )
        Await.ready(createPublisherDoubleFuture, Duration.Inf)

        createPublisherDoubleFuture.value match {
            case Some(Failure(exc: NameAlreadyExistsException)) => {
                exc.getMessage should equal("A publisher with name '" + mypublisher.name + "' already exists.")
            }
            case _ => fail("The future should fail.")
        }

        val getPublishersFuture: Future[Seq[Publisher]] = publishers.getAllPublishers()
        var allPublishers: Seq[Publisher] = Await.result(getPublishersFuture, Duration.Inf)

        allPublishers.length should be(1)
        allPublishers.head should be(mypublisher)
    }

    test("Publishers.getPublisherByName should return no id if it does not exist") {
        val publishers: Publishers = new Publishers()

        val fake_publisher: Publisher = new Publisher(
            1, "fake_publisher_name"
        )
        val createPublisherFuture: Future[Unit] = publishers.createPublisher(
            fake_publisher.id, fake_publisher.name
        )
        Await.ready(createPublisherFuture, Duration.Inf)

        // Check that the future succeeds
        createPublisherFuture.value should be(Some(Success(())))

        val returnedPublishersFuture: Future[Option[Publisher]] = publishers.getPublisherByName("fake_publisher_name0")
        var returnedPublisher: Option[Publisher] = Await.result(returnedPublishersFuture, Duration.Inf)

        returnedPublisher should be(None)
    }

    test("Publishers.getPublisherByName should return a publisher") {
        val publishers: Publishers = new Publishers()

        val fake_publisher: Publisher = new Publisher(
            1, "fake_publisher_name"
        )
        val createPublisherFuture: Future[Unit] = publishers.createPublisher(
            fake_publisher.id, fake_publisher.name
        )
        Await.ready(createPublisherFuture, Duration.Inf)

        // Check that the future succeeds
        createPublisherFuture.value should be(Some(Success(())))

        val returnedPublishersFuture: Future[Option[Publisher]] = publishers.getPublisherByName("fake_publisher_name")
        var returnedPublisher: Option[Publisher] = Await.result(returnedPublishersFuture, Duration.Inf)

        returnedPublisher match {
            case Some(publisher) => publisher should be(fake_publisher)
            case None => fail("Should return a publisher.")
        }
    }

    test("Publishers.getPublisherById should return no id if it does not exist") {
        val publishers: Publishers = new Publishers()

        val fake_publisher: Publisher = new Publisher(
            1, "fake_publisher_name"
        )
        val createPublisherFuture: Future[Unit] = publishers.createPublisher(
            fake_publisher.id, fake_publisher.name
        )
        Await.ready(createPublisherFuture, Duration.Inf)

        // Check that the future succeeds
        createPublisherFuture.value should be(Some(Success(())))

        val returnedPublishersFuture: Future[Option[Publisher]] = publishers.getPublisherById(0)
        var returnedPublisher: Option[Publisher] = Await.result(returnedPublishersFuture, Duration.Inf)

        returnedPublisher should be(None)
    }

    test("Publishers.getPublisherById should return a publisher") {
        val publishers: Publishers = new Publishers()

        val fake_publisher: Publisher = new Publisher(
            1, "fake_publisher_name"
        )
        val createPublisherFuture: Future[Unit] = publishers.createPublisher(
            fake_publisher.id, fake_publisher.name
        )
        Await.ready(createPublisherFuture, Duration.Inf)

        // Check that the future succeeds
        createPublisherFuture.value should be(Some(Success(())))

        val returnedPublishersFuture: Future[Option[Publisher]] = publishers.getPublisherById(1)
        var returnedPublisher: Option[Publisher] = Await.result(returnedPublishersFuture, Duration.Inf)

        returnedPublisher match {
            case Some(publisher) => publisher should be(fake_publisher)
            case None => fail("Should return a publisher.")
        }
    }


    test("Publishers.getAllPublishers should return a list of publishers") {
        val publishers: Publishers = new Publishers()

        val fake_publisher: Publisher = new Publisher(
            1, "fake_publisher_name"
        )
        val createPublisherFuture: Future[Unit] = publishers.createPublisher(
            fake_publisher.id, fake_publisher.name
        )
        Await.ready(createPublisherFuture, Duration.Inf)

        val fake_publisher2: Publisher = new Publisher(
            2, "fake_publisher_name2"
        )
        val createAnotherPublisherFuture: Future[Unit] = publishers.createPublisher(
            fake_publisher2.id, fake_publisher2.name
        )
        Await.ready(createAnotherPublisherFuture, Duration.Inf)


        val returnedPublisherSeqFuture: Future[Seq[Publisher]] = publishers.getAllPublishers()
        val returnedPublisherSeq: Seq[Publisher] = Await.result(returnedPublisherSeqFuture, Duration.Inf)

        returnedPublisherSeq.length should be(2)
        returnedPublisherSeq(0) should be(fake_publisher)
        returnedPublisherSeq(1) should be(fake_publisher2)
    }

    // -------------------------- ORDER ---------------------------------

    test("Orders.createOrder should create a new order") {
        val fmt = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")
		val time = LocalDateTime.now(ZoneId.of("America/New_York")).format(fmt)
        val orders: Orders = new Orders()
        val fake_order: Order = new Order(
            1, 1, Timestamp.valueOf(time)
        )
        val createOrderFuture: Future[Unit] = orders.createOrder(
            fake_order.id, fake_order.iduser, fake_order.date
        )
        Await.ready(createOrderFuture, Duration.Inf)

        // Check that the future succeeds
        createOrderFuture.value should be(Some(Success(())))

        val getOrdersFuture: Future[Seq[Order]] = orders.getAllOrders()
        var allOrders: Seq[Order] = Await.result(getOrdersFuture, Duration.Inf)

        allOrders.length should be(1)
        allOrders.head should be(fake_order)
    }


    test("Orders.getOrderById should return no id if it does not exist") {
        val fmt = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")
		val time = LocalDateTime.now(ZoneId.of("America/New_York")).format(fmt)
        val orders: Orders = new Orders()
        val fake_order: Order = new Order(
            1, 1, Timestamp.valueOf(time)
        )
        val createOrderFuture: Future[Unit] = orders.createOrder(
            fake_order.id, fake_order.iduser, fake_order.date
        )
        Await.ready(createOrderFuture, Duration.Inf)

        // Check that the future succeeds
        createOrderFuture.value should be(Some(Success(())))

        val returnedOrdersFuture: Future[Option[Order]] = orders.getOrderById(0)
        var returnedOrder: Option[Order] = Await.result(returnedOrdersFuture, Duration.Inf)

        returnedOrder should be(None)
    }

    test("Orders.getOrderById should return a order") {
        val fmt = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")
		val time = LocalDateTime.now(ZoneId.of("America/New_York")).format(fmt)
        val orders: Orders = new Orders()
        val fake_order: Order = new Order(
            1, 1, Timestamp.valueOf(time)
        )
        val createOrderFuture: Future[Unit] = orders.createOrder(
            fake_order.id, fake_order.iduser, fake_order.date
        )
        Await.ready(createOrderFuture, Duration.Inf)

        // Check that the future succeeds
        createOrderFuture.value should be(Some(Success(())))

        val returnedOrdersFuture: Future[Option[Order]] = orders.getOrderById(1)
        var returnedOrder: Option[Order] = Await.result(returnedOrdersFuture, Duration.Inf)

        returnedOrder match {
            case Some(order) => order should be(fake_order)
            case None => fail("Should return a order.")
        }
    }


    test("Orders.getAllOrders should return a list of orders") {
        val fmt = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")
		val time = LocalDateTime.now(ZoneId.of("America/New_York")).format(fmt)
        val orders: Orders = new Orders()
        val fake_order: Order = new Order(
            1, 1, Timestamp.valueOf(time)
        )
        val createOrderFuture: Future[Unit] = orders.createOrder(
            fake_order.id, fake_order.iduser, fake_order.date
        )
        Await.ready(createOrderFuture, Duration.Inf)

        val fake_order2: Order = new Order(
            2, 1, Timestamp.valueOf(time)
        )
        val createAnotherOrderFuture: Future[Unit] = orders.createOrder(
            fake_order2.id, fake_order2.iduser, fake_order2.date
        )
        Await.ready(createAnotherOrderFuture, Duration.Inf)


        val returnedOrderSeqFuture: Future[Seq[Order]] = orders.getAllOrders()
        val returnedOrderSeq: Seq[Order] = Await.result(returnedOrderSeqFuture, Duration.Inf)

        returnedOrderSeq.length should be(2)
        returnedOrderSeq(0) should be(fake_order)
        returnedOrderSeq(1) should be(fake_order2)
    }

    test("Orders.getLastOrderFromUser should return None if no order from the user")
    {
        var orders: Orders = new Orders()
        val getOrderFuture: Future[Option[Order]] = orders.getLastOrderFromUser(1)

        val order: Option[Order] = Await.result(getOrderFuture, Duration.Inf);

        order should be(None)
    }

    test("Orders.getLastOrderFromUser should return the last order of the user")
    {
        var orders: Orders = new Orders()

        val fmt = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")
		var time = LocalDateTime.now(ZoneId.of("America/New_York")).format(fmt)

        val iduser: Int = 2
        var createOrderFuture: Future[Unit] = orders.createOrder(0, iduser, Timestamp.valueOf(time))
        Await.ready(createOrderFuture, Duration.Inf)
        time = LocalDateTime.now(ZoneId.of("America/New_York")).format(fmt)
        val order: Order = new Order(1, iduser, Timestamp.valueOf(time))
        createOrderFuture = orders.createOrder(order.id, iduser, order.date)
        Await.ready(createOrderFuture, Duration.Inf)

        val getOrderFuture: Future[Option[Order]] = orders.getLastOrderFromUser(iduser)

        val lastOrder: Option[Order] = Await.result(getOrderFuture, Duration.Inf);

        lastOrder should be(Some(order))
    }
        // -------------------------- ORDERLINE ---------------------------------

    test("OrderLines.createOrderLine should create a new orderLine") {
        val orderLines: OrderLines = new OrderLines()
        val fake_orderLine: OrderLine = new OrderLine(
            1, 0, 1, 1, 10.0, 1
        )
        val createOrderLineFuture: Future[Unit] = orderLines.createOrderLine(
            fake_orderLine.idorder, fake_orderLine.idproduct, fake_orderLine.idreseller, fake_orderLine.idstatus, fake_orderLine.price, fake_orderLine.quantity
        )
        Await.ready(createOrderLineFuture, Duration.Inf)

        // Check that the future succeeds
        createOrderLineFuture.value should be(Some(Success(())))

        val getOrderLinesFuture: Future[Seq[OrderLine]] = orderLines.getAllOrderLines()
        var allOrderLines: Seq[OrderLine] = Await.result(getOrderLinesFuture, Duration.Inf)

        allOrderLines.length should be(1)
        allOrderLines.head should be(fake_orderLine)
    }


    test("OrderLines.getOrderLineById should return no id if it does not exist") {
        val orderLines: OrderLines = new OrderLines()
        val fake_orderLine: OrderLine = new OrderLine(
            1, 0, 1, 1, 10.0, 1
        )
        val createOrderLineFuture: Future[Unit] = orderLines.createOrderLine(
            fake_orderLine.idorder, fake_orderLine.idproduct, fake_orderLine.idreseller, fake_orderLine.idstatus, fake_orderLine.price, fake_orderLine.quantity
        )
        Await.ready(createOrderLineFuture, Duration.Inf)

        // Check that the future succeeds
        createOrderLineFuture.value should be(Some(Success(())))

        val returnedOrderLinesFuture: Future[Option[OrderLine]] = orderLines.getOrderLineById(0, 0, 1)
        var returnedOrderLine: Option[OrderLine] = Await.result(returnedOrderLinesFuture, Duration.Inf)

        returnedOrderLine should be(None)
    }

    test("OrderLines.getOrderLineById should return a orderLine") {
        val orderLines: OrderLines = new OrderLines()
        val fake_orderLine: OrderLine = new OrderLine(
            1, 0, 1, 1, 10.0, 1
        )
        val createOrderLineFuture: Future[Unit] = orderLines.createOrderLine(
            fake_orderLine.idorder, fake_orderLine.idproduct, fake_orderLine.idreseller, fake_orderLine.idstatus, fake_orderLine.price, fake_orderLine.quantity
        )
        Await.ready(createOrderLineFuture, Duration.Inf)

        // Check that the future succeeds
        createOrderLineFuture.value should be(Some(Success(())))

        val returnedOrderLinesFuture: Future[Option[OrderLine]] = orderLines.getOrderLineById(1, 0, 1)
        var returnedOrderLine: Option[OrderLine] = Await.result(returnedOrderLinesFuture, Duration.Inf)

        returnedOrderLine match {
            case Some(orderLine) => orderLine should be(fake_orderLine)
            case None => fail("Should return a orderLine.")
        }
    }


    test("OrderLines.getAllOrderLines should return a list of orderLines") {
        val orderLines: OrderLines = new OrderLines()
        val fake_orderLine: OrderLine = new OrderLine(
            1, 0, 1, 1, 10.0, 1
        )
        val createOrderLineFuture: Future[Unit] = orderLines.createOrderLine(
            fake_orderLine.idorder, fake_orderLine.idproduct, fake_orderLine.idreseller, fake_orderLine.idstatus, fake_orderLine.price, fake_orderLine.quantity
        )
        Await.ready(createOrderLineFuture, Duration.Inf)

        val fake_orderLine2: OrderLine = new OrderLine(
            1, 1, 1, 1, 10.0, 1
        )
        val createAnotherOrderLineFuture: Future[Unit] = orderLines.createOrderLine(
            fake_orderLine2.idorder, fake_orderLine2.idproduct, fake_orderLine2.idreseller, fake_orderLine2.idstatus, fake_orderLine2.price, fake_orderLine2.quantity
        )
        Await.ready(createAnotherOrderLineFuture, Duration.Inf)


        val returnedOrderLineSeqFuture: Future[Seq[OrderLine]] = orderLines.getAllOrderLines()
        val returnedOrderLineSeq: Seq[OrderLine] = Await.result(returnedOrderLineSeqFuture, Duration.Inf)

        returnedOrderLineSeq.length should be(2)
        returnedOrderLineSeq(0) should be(fake_orderLine)
        returnedOrderLineSeq(1) should be(fake_orderLine2)
    }

    // -------------------------- CART ---------------------------------

    test("Carts.createCart should create a new cart") {
        val carts: Carts = new Carts()
        val mycart: Cart = new Cart(1, 1)

        val createCartFuture: Future[Unit] = carts.createCart(mycart.id, mycart.iduser)
        Await.ready(createCartFuture, Duration.Inf)

        // Check that the future succeeds
        createCartFuture.value should be(Some(Success(())))

        val getCartsFuture: Future[Seq[Cart]] = carts.getAllCarts()
        var allcarts: Seq[Cart] = Await.result(getCartsFuture, Duration.Inf)

        allcarts.length should be(1)
        allcarts.head should be (mycart)
    }

    test("Carts.createCart returned future should fail if the id of the cart already exists") {
        val carts: Carts = new Carts()
        val cart1: Cart = new Cart(1, 1)

        val createCart1Future: Future[Unit] = carts.createCart(cart1.id, cart1.iduser)
        Await.ready(createCart1Future, Duration.Inf)

        val createCart2Future: Future[Unit] = carts.createCart(cart1.id, cart1.iduser + 1)
        Await.ready(createCart2Future, Duration.Inf)

        createCart2Future.value match {
            case Some(Failure(exc: IdAlreadyExistsException)) => {
                exc.getMessage should equal ("A Cart with id '" + cart1.id + "' already exists.")
            }
            case _ => fail("The future should fail.")
        }
    }

    test("Carts.createCart returned future should fail if the iduser of the cart already exists") {
        val carts: Carts = new Carts()
        val cart1: Cart = new Cart(1, 1)

        val createCart1Future: Future[Unit] = carts.createCart(cart1.id, cart1.iduser)
        Await.ready(createCart1Future, Duration.Inf)

        val createCart2Future: Future[Unit] = carts.createCart(cart1.id + 1, cart1.iduser)
        Await.ready(createCart2Future, Duration.Inf)

        createCart2Future.value match {
            case Some(Failure(exc: IdUserAlreadyExistsException)) => {
                exc.getMessage should equal ("A Cart with user id '" + cart1.iduser + "' already exists.")
            }
            case _ => fail("The future should fail.")
        }
    }

    test("Carts.getCartById should return a cart") {
        val carts: Carts = new Carts()
        val cart: Cart = new Cart(1, 1)

        val createCartFuture: Future[Unit] = carts.createCart(cart.id, cart.iduser)
        Await.ready(createCartFuture, Duration.Inf)

        val getCartFuture: Future[Option[Cart]] = carts.getCartById(cart.id)
        val allCarts: Option[Cart] = Await.result(getCartFuture, Duration.Inf)

        allCarts should be (Some(cart))
    }

    test("Carts.getCartById should return none if id doesn't exist") {
        val carts: Carts = new Carts()
        val cart: Cart = new Cart(1, 1)

        val createCartFuture: Future[Unit] = carts.createCart(cart.id, cart.iduser)
        Await.ready(createCartFuture, Duration.Inf)

        val getCartFuture: Future[Option[Cart]] = carts.getCartById(cart.id + 2)
        val allCarts: Option[Cart] = Await.result(getCartFuture, Duration.Inf)

        allCarts should be (None)
    }

    test("Carts.getCartByIdUser should return a cart") {
        val carts: Carts = new Carts()
        val cart: Cart = new Cart(1, 1)

        val createCartFuture: Future[Unit] = carts.createCart(cart.id, cart.iduser)
        Await.ready(createCartFuture, Duration.Inf)

        val getCartFuture: Future[Option[Cart]] = carts.getCartByIdUser(cart.iduser)
        val allCarts: Option[Cart] = Await.result(getCartFuture, Duration.Inf)

        allCarts should be (Some(cart))
    }

    test("Carts.getCartByIdUser should return none if id doesn't exist") {
        val carts: Carts = new Carts()
        val cart: Cart = new Cart(1, 1)

        val createCartFuture: Future[Unit] = carts.createCart(cart.id, cart.iduser)
        Await.ready(createCartFuture, Duration.Inf)

        val getCartFuture: Future[Option[Cart]] = carts.getCartByIdUser(cart.iduser + 2)
        val allCarts: Option[Cart] = Await.result(getCartFuture, Duration.Inf)

        allCarts should be (None)
    }

    test("Carts.getAllCarts should return a list of carts") {
        val carts: Carts = new Carts()
        val cart1: Cart = new Cart(1, 1)

        val createCart1Future: Future[Unit] = carts.createCart(cart1.id, cart1.iduser)
        Await.ready(createCart1Future, Duration.Inf)

        val cart2: Cart = new Cart(cart1.id + 1, cart1.iduser + 1)
        val createCart2Future: Future[Unit] = carts.createCart(cart2.id, cart2.iduser)
        Await.ready(createCart2Future, Duration.Inf)

        val getAllCartsFuture: Future[Seq[Cart]] = carts.getAllCarts()
        val allCarts: Seq[Cart] = Await.result(getAllCartsFuture, Duration.Inf)

        allCarts.length should be(2)
        allCarts.head should be (cart1)
        allCarts(0) should be (cart1)
        allCarts(1) should be (cart2)
    }

    // -------------------------- CARTLINE ---------------------------------

    test("CartLines.createCartLine should create a new cartline") {
        val cartlines: CartLines = new CartLines()
        val mycartline: CartLine = new CartLine(1, 311, 1, 19.99, 1)

        val createCartlineFuture: Future[Unit] = cartlines.createCartLine(
            mycartline.idcart, mycartline.idproduct, mycartline.idreseller,
            mycartline.price, mycartline.quantity
        )
        Await.ready(createCartlineFuture, Duration.Inf)

        // Check that the future succeeds
        createCartlineFuture.value should be(Some(Success(())))

        val getCartlinesFuture: Future[Seq[CartLine]] = cartlines.getAllCartLines()
        var allcartlines: Seq[CartLine] = Await.result(getCartlinesFuture, Duration.Inf)

        allcartlines.length should be(1)
        allcartlines.head should be (mycartline)
    }

    test("CartLines.createCartLine returned future should fail if the id of the cartline already exists") {
        val cartlines: CartLines = new CartLines()
        val cartline1: CartLine = new CartLine(1, 311, 1, 19.99, 1)

        val createCartline1Future: Future[Unit] = cartlines.createCartLine(
            cartline1.idcart, cartline1.idproduct, cartline1.idreseller,
            cartline1.price, cartline1.quantity
        )
        Await.ready(createCartline1Future, Duration.Inf)

        val cartline2: CartLine = new CartLine(
            cartline1.idcart, cartline1.idproduct, cartline1.idreseller, 19.99, 2
        )

        val createCartline2Future: Future[Unit] = cartlines.createCartLine(
            cartline1.idcart, cartline1.idproduct, cartline1.idreseller,
            cartline2.price, cartline2.quantity
        )
        Await.ready(createCartline2Future, Duration.Inf)

        createCartline2Future.value match {
            case Some(Failure(exc: IdAlreadyExistsException)) => {
                exc.getMessage should equal ("A CartLine with name '" + cartline1.idcart 
                + "', '" + cartline1.idproduct + "', '" + cartline1.idreseller + "' already exists.")
            }
            case _ => fail("The future should fail.")
        }
    }

    test("CartLines.getCartLineById should return a cartline") {
        val cartlines: CartLines = new CartLines()
        val mycartline: CartLine = new CartLine(1, 311, 1, 19.99, 1)

        val createCartlineFuture: Future[Unit] = cartlines.createCartLine(
            mycartline.idcart, mycartline.idproduct, mycartline.idreseller,
            mycartline.price, mycartline.quantity
        )
        Await.ready(createCartlineFuture, Duration.Inf)

        val getCartlineFuture: Future[Option[CartLine]] = cartlines.getCartLineById(
            mycartline.idcart, mycartline.idproduct, mycartline.idreseller
        )
        val allCartlines: Option[CartLine] = Await.result(getCartlineFuture, Duration.Inf)

        allCartlines should be (Some(mycartline))
    }

    test("CartLines.getCartLineById should return None if id doesn't exist") {
        val cartlines: CartLines = new CartLines()
        val mycartline: CartLine = new CartLine(1, 311, 1, 19.99, 1)

        val createCartlineFuture: Future[Unit] = cartlines.createCartLine(
            mycartline.idcart, mycartline.idproduct, mycartline.idreseller,
            mycartline.price, mycartline.quantity
        )
        Await.ready(createCartlineFuture, Duration.Inf)

        val getCartlineFuture: Future[Option[CartLine]] = cartlines.getCartLineById(
            mycartline.idcart + 2, mycartline.idproduct, mycartline.idreseller
        )
        val allCartlines: Option[CartLine] = Await.result(getCartlineFuture, Duration.Inf)

        allCartlines should be (None)
    }

    test("CartLines.getCartLinesByIdCart should return a list of cartlines") {
        val cartlines: CartLines = new CartLines()
        val idcart: Int = 1
        val cartline1: CartLine = new CartLine(idcart, 311, 1, 19.99, 1)

        val createCartline1Future: Future[Unit] = cartlines.createCartLine(
            cartline1.idcart, cartline1.idproduct, cartline1.idreseller,
            cartline1.price, cartline1.quantity
        )
        Await.ready(createCartline1Future, Duration.Inf)

        val cartline2: CartLine = new CartLine(idcart, 35, 1, 59.99, 1)

        val createCartline2Future: Future[Unit] = cartlines.createCartLine(
            cartline2.idcart, cartline2.idproduct, cartline2.idreseller,
            cartline2.price, cartline2.quantity
        )
        Await.ready(createCartline2Future, Duration.Inf)

        val getCartlinesFuture: Future[Seq[CartLine]] = cartlines.getCartLinesByIdCart(idcart)
        val allCartlines: Seq[CartLine] = Await.result(getCartlinesFuture, Duration.Inf)

        allCartlines.length should be(2)
        allCartlines.head should be (cartline1)
        allCartlines(0) should be (cartline1)
        allCartlines(1) should be (cartline2)
    }

    test("CartLines.getAllCartLines should return a list of cartlines") {
        val cartlines: CartLines = new CartLines()
        val cartline1: CartLine = new CartLine(1, 311, 1, 19.99, 1)

        val createCartline1Future: Future[Unit] = cartlines.createCartLine(
            cartline1.idcart, cartline1.idproduct, cartline1.idreseller,
            cartline1.price, cartline1.quantity
        )
        Await.ready(createCartline1Future, Duration.Inf)

        val cartline2: CartLine = new CartLine(2, 35, 1, 59.99, 1)

        val createCartline2Future: Future[Unit] = cartlines.createCartLine(
            cartline2.idcart, cartline2.idproduct, cartline2.idreseller,
            cartline2.price, cartline2.quantity
        )
        Await.ready(createCartline2Future, Duration.Inf)

        val getCartlinesFuture: Future[Seq[CartLine]] = cartlines.getAllCartLines()
        val allCartlines: Seq[CartLine] = Await.result(getCartlinesFuture, Duration.Inf)

        allCartlines.length should be(2)
        allCartlines.head should be (cartline1)
        allCartlines(0) should be (cartline1)
        allCartlines(1) should be (cartline2)        
    }

    test("CartLines.updateCartlineQuantity should update quantity") {
        val cartlines: CartLines = new CartLines()
        val cartline1: CartLine = new CartLine(1, 311, 1, 19.99, 1)

        val createCartline1Future: Future[Unit] = cartlines.createCartLine(
            cartline1.idcart, cartline1.idproduct, cartline1.idreseller,
            cartline1.price, cartline1.quantity
        )
        Await.ready(createCartline1Future, Duration.Inf)

        val newQuantity: Int = cartline1.quantity + 1
        val updateCartlineFuture: Future[Unit] = cartlines.updateCartlineQuantity(
            cartline1.idcart, cartline1.idproduct,
            cartline1.idreseller, newQuantity
        )
        Await.ready(updateCartlineFuture, Duration.Inf)

        val getCartlinesFuture: Future[Seq[CartLine]] = cartlines.getAllCartLines()
        var allcartlines: Seq[CartLine] = Await.result(getCartlinesFuture, Duration.Inf)

        allcartlines.length should be(1)
        allcartlines.head.idcart should be (cartline1.idcart)
        allcartlines.head.idproduct should be(cartline1.idproduct)
        allcartlines.head.idreseller should be(cartline1.idreseller)
        allcartlines.head.quantity should be(newQuantity)
    }

    test("CartLines.deleteCartline should delete the cartline") {
        val cartlines: CartLines = new CartLines()
        val cartline1: CartLine = new CartLine(1, 117, 1, 59.99, 1)

        val createFuture: Future[Unit] = cartlines.createCartLine(
            cartline1.idcart, cartline1.idproduct, cartline1.idreseller,
            cartline1.price, cartline1.quantity
        )
        Await.ready(createFuture, Duration.Inf)

        // Check that the cartline is really here, otherwise the test is useless
        val getCartlineFuture: Future[Option[CartLine]] = cartlines.getCartLineById(
            cartline1.idcart, cartline1.idproduct, cartline1.idreseller
        )
        val cartline: Option[CartLine] = Await.result(getCartlineFuture, Duration.Inf)
        cartline should be(Some(cartline1))

        // Delete the cartline
        val deleteFuture: Future[Unit] = cartlines.deleteCartline(
            cartline1.idcart, cartline1.idproduct, cartline1.idreseller
        )
        Await.ready(deleteFuture, Duration.Inf)

        // Check that the cartline is deleted
        val getCartlineFuture2: Future[Option[CartLine]] = cartlines.getCartLineById(
            cartline1.idcart, cartline1.idproduct, cartline1.idreseller
        )
        val cartline2: Option[CartLine] = Await.result(getCartlineFuture2, Duration.Inf)
        cartline2 should be(None)
    }

    // --------- Wallets --------------


    test("Wallets.createWallets should create a new Wallet") {
        val wallets : Wallets = new Wallets()
        val fake: Wallet = new Wallet(
            1, 100
        )
        val createWalletsFuture: Future[Unit] = wallets.createWallets(
            fake.id, fake.solde
        )
        Await.ready(createWalletsFuture, Duration.Inf)

        // Check that the future succeeds
        createWalletsFuture.value should be(Some(Success(())))

        val getWalletsFuture: Future[Seq[Wallet]] = wallets.getAllWallets()
        var allwallets: Seq[Wallet] = Await.result(getWalletsFuture, Duration.Inf)

        allwallets.length should be(1)
        allwallets.head should be (fake)
    }

    test("Wallets.getAllWallets should return a list of Wallets") {
        val wallets : Wallets = new Wallets()
        val fake: Wallet = new Wallet(
            1, 100
        )
        val createWalletsFuture: Future[Unit] = wallets.createWallets(
            fake.id, fake.solde
        )
        Await.ready(createWalletsFuture, Duration.Inf)

        val fake2: Wallet = new Wallet(
            2, 200
        )
        val createWalletsFuture2: Future[Unit] = wallets.createWallets(
            fake2.id, fake2.solde
        )
        Await.ready(createWalletsFuture2, Duration.Inf)


        val getWalletsFuture: Future[Seq[Wallet]] = wallets.getAllWallets()
        var allwallets: Seq[Wallet] = Await.result(getWalletsFuture, Duration.Inf)

        allwallets.length should be(2)
        allwallets.head should be (fake)
        allwallets(0) should be(fake)
        allwallets(1) should be(fake2)
    }
    test("Wallets.getWalletById should return none if id does not exist") {
        val wallets : Wallets = new Wallets()
        val fake: Wallet = new Wallet(
            1, 100
        )
        val createWalletsFuture: Future[Unit] = wallets.createWallets(
            fake.id, fake.solde
        )
        Await.ready(createWalletsFuture, Duration.Inf)

        val fake2: Wallet = new Wallet(
            2, 200
        )
        val createWalletsFuture2: Future[Unit] = wallets.createWallets(
            fake2.id, fake2.solde
        )
        Await.ready(createWalletsFuture2, Duration.Inf)

        val getWalletsFuture: Future[Option[Wallet]] = wallets.getSoldeById(3)
        var allwallets: Option[Wallet] = Await.result(getWalletsFuture, Duration.Inf)

        allwallets should be(None)
    }
    test("Wallets.creditWalletFuture should return none if id does not exist") {
        val wallets : Wallets = new Wallets()
        
        val fake2: Wallet = new Wallet(
            2, 200
        )
        val createWalletsFuture2: Future[Unit] = wallets.createWallets(
            fake2.id, fake2.solde
        )
        Await.ready(createWalletsFuture2, Duration.Inf)
        val creditWalletFuture: Future[Option[Wallet]] = wallets.creditWallet(2,200)
        var allwallets: Option[Wallet] = Await.result(creditWalletFuture, Duration.Inf)
        allwallets should be(Some(Wallet(2,400)))
    }
    test("Wallets.debitWalletFuture should return none if id does not exist") {
        val wallets : Wallets = new Wallets()
        
        val fake2: Wallet = new Wallet(
            2, 200
        )
        val createWalletsFuture2: Future[Unit] = wallets.createWallets(
            fake2.id, fake2.solde
        )
        Await.ready(createWalletsFuture2, Duration.Inf)
        val debitWalletFuture: Future[Option[Wallet]] = wallets.debitWallet(2,200)
        var allwallets: Option[Wallet] = Await.result(debitWalletFuture, Duration.Inf)
        allwallets should be(Some(Wallet(2,0)))
    }



    // -------------------------- DATABASE ---------------------------------

    test("InsertData.ClearDB should Clear the DB") {
        val genres: Genres = new Genres()
        val publishers: Publishers = new Publishers()
        val games: Games = new Games()
        val developers: Developers = new Developers()
        val users: Users = new Users()
        val orders: Orders = new Orders()
        val orderLines: OrderLines = new OrderLines()
        val comments: Comments = new Comments(orders, orderLines)
        val wallets : Wallets = new Wallets()
        val carts: Carts = new Carts()
        val cartlines: CartLines = new CartLines()
        

        val insertdata : InsertData = new InsertData(developers,genres,publishers,games, users, comments, orders, orderLines, carts, cartlines ,null)
        insertdata.ClearDB()

        val returnedPublisherSeqFuture: Future[Seq[Publisher]] = publishers.getAllPublishers()
        val returnedPublisherSeq: Seq[Publisher] = Await.result(returnedPublisherSeqFuture, Duration.Inf)

        val getGenresFuture: Future[Seq[Genre]] = genres.getAllGenres()
        var allGenres: Seq[Genre] = Await.result(getGenresFuture, Duration.Inf)

        val getGamesFuture: Future[Seq[Game]] = games.getAllGames()
        var allGames: Seq[Game] = Await.result(getGamesFuture, Duration.Inf)

        val returnedDeveloperSeqFuture: Future[Seq[Developer]] = developers.getAllDevelopers()
        val returnedDeveloperSeq: Seq[Developer] = Await.result(returnedDeveloperSeqFuture, Duration.Inf)

        val returnedCommentSeqFuture: Future[Seq[Comment]] = comments.getAllComments()
        val returnedCommentSeq: Seq[Comment] = Await.result(returnedCommentSeqFuture, Duration.Inf)

        val getUsersFuture: Future[Seq[User]] = users.getAllUsers()
        var allUsers: Seq[User] = Await.result(getUsersFuture, Duration.Inf)

        val getOrdersFuture: Future[Seq[Order]] = orders.getAllOrders()
        var allOrders: Seq[Order] = Await.result(getOrdersFuture, Duration.Inf)

        val getOrderLinesFuture: Future[Seq[OrderLine]] = orderLines.getAllOrderLines()
        var allOrderLines: Seq[OrderLine] = Await.result(getOrderLinesFuture, Duration.Inf)

        val getCartsFuture: Future[Seq[Cart]] = carts.getAllCarts()
        var allcarts: Seq[Cart] = Await.result(getCartsFuture, Duration.Inf)

        val getCartlinesFuture: Future[Seq[CartLine]] = cartlines.getAllCartLines()
        var allcartlines: Seq[CartLine] = Await.result(getCartlinesFuture, Duration.Inf)

        val getWalletsFuture: Future[Seq[Wallet]] = wallets.getAllWallets()
        var allwallets: Seq[Wallet] = Await.result(getWalletsFuture, Duration.Inf)

        returnedDeveloperSeq.length should be(0)
        allGames.length should be(0)
        returnedPublisherSeq.length should be(0)
        allGenres.length should be(0)
        returnedCommentSeq.length should be(0)
        allUsers.length should be(0)
        allOrders.length should be(0)
        allOrderLines.length should be(0)
        allcarts.length should be(0)
        allcartlines.length should be(0)
        allwallets.length should be(0)


    }

    test("InsertData.FillDevelopers should add 19 developpers"){
        val developers: Developers = new Developers()
        val genres: Genres = new Genres()
        val publishers: Publishers = new Publishers()
        val games: Games = new Games()
        val users: Users = new Users()
        val orders: Orders = new Orders()
        val orderLines: OrderLines = new OrderLines()
        val comments: Comments = new Comments(orders, orderLines)


        val insertdata : InsertData = new InsertData(developers,genres,publishers,games,users,comments, orders, orderLines, null, null,null)
        insertdata.ClearDB()
        insertdata.FillDevelopers()

        val returnedDeveloperSeqFuture: Future[Seq[Developer]] = developers.getAllDevelopers()
        val returnedDeveloperSeq: Seq[Developer] = Await.result(returnedDeveloperSeqFuture, Duration.Inf)

        returnedDeveloperSeq.length should be(20)
    }

    test("InsertData.FillPublishers should add 15 Publishers")
    {
        val developers: Developers = new Developers()
        val genres: Genres = new Genres()
        val publishers: Publishers = new Publishers()
        val games: Games = new Games()
        val users: Users = new Users()
        val orders: Orders = new Orders()
        val orderLines: OrderLines = new OrderLines()
        val comments: Comments = new Comments(orders, orderLines)


        val insertdata : InsertData = new InsertData(developers,genres,publishers,games,users,comments, orders, orderLines, null, null,null)
        insertdata.ClearDB()
        insertdata.FillPublishers()

        val returnedPublisherSeqFuture: Future[Seq[Publisher]] = publishers.getAllPublishers()
        val returnedPublisherSeq: Seq[Publisher] = Await.result(returnedPublisherSeqFuture, Duration.Inf)

        returnedPublisherSeq.length should be(15)
    }
    test("InsertData.FillGenre should add 19 Genres")
    {
        val developers: Developers = new Developers()
        val genres: Genres = new Genres()
        val publishers: Publishers = new Publishers()
        val games: Games = new Games()
        val users: Users = new Users()
        val orders: Orders = new Orders()
        val orderLines: OrderLines = new OrderLines()
        val comments: Comments = new Comments(orders, orderLines)


        val insertdata : InsertData = new InsertData(developers,genres,publishers,games,users,comments, orders, orderLines, null, null,null)
        insertdata.ClearDB()
        insertdata.FillGenre()

        val getGenresFuture: Future[Seq[Genre]] = genres.getAllGenres()
        var allGenres: Seq[Genre] = Await.result(getGenresFuture, Duration.Inf)

        allGenres.length should be(20)
    }

    test("InsertData.FillGame should add 44 Games")
    {
        val developers: Developers = new Developers()
        val genres: Genres = new Genres()
        val publishers: Publishers = new Publishers()
        val games: Games = new Games()
        val users: Users = new Users()
        val orders: Orders = new Orders()
        val orderLines: OrderLines = new OrderLines()
        val comments: Comments = new Comments(orders, orderLines)


        val insertdata : InsertData = new InsertData(developers,genres,publishers,games,users,comments, orders, orderLines, null, null,null)
        insertdata.ClearDB()
        insertdata.FillGame()

        val getGamesFuture: Future[Seq[Game]] = games.getAllGames()
        var allGames: Seq[Game] = Await.result(getGamesFuture, Duration.Inf)

        allGames.length should be(44)
    }

    test("InsertData.FillUser should add 5 Users")
    {
        val developers: Developers = new Developers()
        val genres: Genres = new Genres()
        val publishers: Publishers = new Publishers()
        val games: Games = new Games()
        val users: Users = new Users()
        val orders: Orders = new Orders()
        val orderLines: OrderLines = new OrderLines()
        val comments: Comments = new Comments(orders, orderLines)


        val insertdata : InsertData = new InsertData(developers,genres,publishers,games,users,comments, orders, orderLines, null, null,null)
        insertdata.ClearDB()
        insertdata.FillUser()

        val getUsersFuture: Future[Seq[User]] = users.getAllUsers()
        var allUsers: Seq[User] = Await.result(getUsersFuture, Duration.Inf)

        allUsers.length should be(5)
    }

    test("InsertData.FillComment should add 20 Comments")
    {
        val developers: Developers = new Developers()
        val genres: Genres = new Genres()
        val publishers: Publishers = new Publishers()
        val games: Games = new Games()
        val users: Users = new Users()
        val orders: Orders = new Orders()
        val orderLines: OrderLines = new OrderLines()
        val comments: Comments = new Comments(orders, orderLines)


        val insertdata : InsertData = new InsertData(developers,genres,publishers,games,users,comments, orders, orderLines, null, null,null)
        insertdata.ClearDB()
        insertdata.FillComment()

        val getCommentsFuture: Future[Seq[Comment]] = comments.getAllComments()
        var allComments: Seq[Comment] = Await.result(getCommentsFuture, Duration.Inf)

        allComments.length should be(20)
    }

    test("InsertData.FillOrder should add 2 Orders")
    {
        val developers: Developers = new Developers()
        val genres: Genres = new Genres()
        val publishers: Publishers = new Publishers()
        val games: Games = new Games()
        val users: Users = new Users()
        val orders: Orders = new Orders()
        val orderLines: OrderLines = new OrderLines()
        val comments: Comments = new Comments(orders, orderLines)


        val insertdata : InsertData = new InsertData(developers,genres,publishers,games,users,comments, orders, orderLines, null, null,null)
        insertdata.ClearDB()
        insertdata.FillOrder()

        val getOrdersFuture: Future[Seq[Order]] = orders.getAllOrders()
        var allOrders: Seq[Order] = Await.result(getOrdersFuture, Duration.Inf)

        allOrders.length should be(2)
    }

    test("InsertData.FillOrderLine should add 4 OrderLine")
    {
        val developers: Developers = new Developers()
        val genres: Genres = new Genres()
        val publishers: Publishers = new Publishers()
        val games: Games = new Games()
        val users: Users = new Users()
        val orders: Orders = new Orders()
        val orderLines: OrderLines = new OrderLines()
        val comments: Comments = new Comments(orders, orderLines)


        val insertdata : InsertData = new InsertData(developers,genres,publishers,games,users,comments, orders, orderLines, null, null,null)
        insertdata.ClearDB()
        insertdata.FillOrderLine()

        val getOrderLinesFuture: Future[Seq[OrderLine]] = orderLines.getAllOrderLines()
        var allOrderLines: Seq[OrderLine] = Await.result(getOrderLinesFuture, Duration.Inf)

        allOrderLines.length should be(4)
    }

    test("InsertData.FillCarts should add 1 cart")
    {
        val carts: Carts = new Carts()
        
        val insertdata : InsertData = new InsertData(null, null, null, null, null, null, null, null, carts, null, null)
        insertdata.ClearDB()
        insertdata.FillCart()

        val getCartsFuture: Future[Seq[Cart]] = carts.getAllCarts()
        var allcarts: Seq[Cart] = Await.result(getCartsFuture, Duration.Inf)

        allcarts.length should be(1)
    }

    test("InsertData.FillCartLines should add 3 cartlines")
    {
        val cartlines: CartLines = new CartLines()
        
        val insertdata : InsertData = new InsertData(null, null, null, null, null, null, null, null, null, cartlines, null)
        insertdata.ClearDB()
        insertdata.FillCartLine()

        val getCartlinesFuture: Future[Seq[CartLine]] = cartlines.getAllCartLines()
        var allcartlines: Seq[CartLine] = Await.result(getCartlinesFuture, Duration.Inf)

        allcartlines.length should be(3)
    }

    test("InsertData.FillWallets should add 5 wallets")
    {
        val users: Users = new Users()
        val wallets : Wallets = new Wallets()

        val insertdata : InsertData = new InsertData(null,null,null,null,users,null, null, null, null, null,wallets)
        insertdata.ClearDB()
        insertdata.FillWallets()

        val getWalletsFuture: Future[Seq[Wallet]] = wallets.getAllWallets()
        var allwallets: Seq[Wallet] = Await.result(getWalletsFuture, Duration.Inf)

        allwallets.length should be(5)
    }


}
