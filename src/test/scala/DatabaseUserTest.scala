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
import poca.{MyDatabase, Users, User, Games, Game, Developers, Developer, Genres, Genre, NotSamePasswordException, EmailAlreadyExistsException, RunMigrations}

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



    test("Games.getGameById should return no id if it does not exist") {
        val games: Games = new Games()

        val fake: Game = new Game(
            1, "fake_product_1", "basename_fake_product_1", 1, 2005.0, "fake_plateforme", "E", "fake_url.fr", 1, 1
        )
        val createGameFuture: Future[Unit] = games.createGame(
            fake.id, fake.name, fake.basename, fake.id_genre, fake.year, 
            fake.plateform, fake.ESRB, fake.url_image, fake.id_publisher, fake.id_developer
        )
        Await.ready(createGameFuture, Duration.Inf)

        val returnedGameFuture: Future[Option[Game]] = games.getGameById(fake.id)
        val returnedGame: Option[Game] = Await.result(returnedGameFuture, Duration.Inf)

        returnedGame match {
            case Some(game) => game should be(fake)
            case None => fail("Should return a game.")
        }
    }

    test("Games.getGameByBaseName should return a game") {
        val games: Games = new Games()

        val fake: Game = new Game(
            1, "fake_product_1", "basename_fake_product_1", 1, 2005.0, "fake_plateforme", "E", "fake_url.fr", 1, 1
        )
        val createGameFuture: Future[Unit] = games.createGame(
            fake.id, fake.name, fake.basename, fake.id_genre, fake.year, 
            fake.plateform, fake.ESRB, fake.url_image, fake.id_publisher, fake.id_developer
        )
        Await.ready(createGameFuture, Duration.Inf)

        val returnedGameFuture: Future[Option[Game]] = games.getGameByBaseName(fake.basename)
        val returnedGame: Option[Game] = Await.result(returnedGameFuture, Duration.Inf)

        returnedGame match {
            case Some(game) => game should be(fake)
            case None => fail("Should return a game.")
        }
    }

    test("Games.getAllGames should return a list of games") {
        val games: Games = new Games()

        val fake: Game = new Game(
            1, "fake_product_1", "basename_fake_product_1", 1, 2005.0, "fake_plateforme", "E", "fake_url.fr", 1, 1
        )
        val createGameFuture: Future[Unit] = games.createGame(
            fake.id, fake.name, fake.basename, fake.id_genre, fake.year, 
            fake.plateform, fake.ESRB, fake.url_image, fake.id_publisher, fake.id_developer
        )
        Await.ready(createGameFuture, Duration.Inf)

        val fake2: Game = new Game(
            2, "fake_product_2", "basename_fake_product_2", 1, 2005.0, "fake2_plateforme", "E", "fake2_url.fr", 1, 1
        )
        val createAnotherGameFuture: Future[Unit] = games.createGame(
            fake2.id, fake2.name, fake2.basename, fake2.id_genre, fake2.year, 
            fake2.plateform, fake2.ESRB, fake2.url_image, fake2.id_publisher, fake2.id_developer
        )
        Await.ready(createAnotherGameFuture, Duration.Inf)

        val returnedGameSeqFuture: Future[Seq[Game]] = games.getAllGames()
        val returnedGameSeq: Seq[Game] = Await.result(returnedGameSeqFuture, Duration.Inf)

        returnedGameSeq.length should be(2)
        returnedGameSeq(0) should be(fake)
        returnedGameSeq(1) should be(fake2)
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

        val returnedDevelopersFuture: Future[Option[Developer]] = developers.getDeveloperById(fake_dev.id)
        var returnedDeveloper: Option[Developer] = Await.result(returnedDevelopersFuture, Duration.Inf)

        returnedDeveloper match {
            case Some(developer) => developer should be(fake_dev)
            case None => fail("Should return a developer.")
        }
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

        val returnedDevelopersFuture: Future[Option[Developer]] = developers.getDeveloperByName(fake_dev.name)
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

        val games: Developers = new Developers()

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
        val fake_dev: Genre = new Genre(
            1, "fake_dev_name"
        )
        val createGenreFuture: Future[Unit] = genres.createGenre(
            fake_dev.id, fake_dev.name
        )
        Await.ready(createGenreFuture, Duration.Inf)

        // Check that the future succeeds
        createGenreFuture.value should be(Some(Success(())))

        val getGenresFuture: Future[Seq[Genre]] = genres.getAllGenres()
        var allGenres: Seq[Genre] = Await.result(getGenresFuture, Duration.Inf)

        allGenres.length should be(1)
        allGenres.head should be(fake_dev)
    }



    test("Genres.getGenreById should return no id if it does not exist") {
        val genres: Genres = new Genres()

        val fake_dev: Genre = new Genre(
            1, "fake_dev_name"
        )
        val createGenreFuture: Future[Unit] = genres.createGenre(
            fake_dev.id, fake_dev.name
        )
        Await.ready(createGenreFuture, Duration.Inf)

        // Check that the future succeeds
        createGenreFuture.value should be(Some(Success(())))

        val returnedGenresFuture: Future[Option[Genre]] = genres.getGenreById(fake_dev.id)
        var returnedGenre: Option[Genre] = Await.result(returnedGenresFuture, Duration.Inf)

        returnedGenre match {
            case Some(genre) => genre should be(fake_dev)
            case None => fail("Should return a genre.")
        }
    }

    test("Genres.getGenreByName should return a genre") {
        val genres: Genres = new Genres()

        val fake_dev: Genre = new Genre(
            1, "fake_dev_name"
        )
        val createGenreFuture: Future[Unit] = genres.createGenre(
            fake_dev.id, fake_dev.name
        )
        Await.ready(createGenreFuture, Duration.Inf)

        // Check that the future succeeds
        createGenreFuture.value should be(Some(Success(())))

        val returnedGenresFuture: Future[Option[Genre]] = genres.getGenreByName(fake_dev.name)
        var returnedGenre: Option[Genre] = Await.result(returnedGenresFuture, Duration.Inf)

        returnedGenre match {
            case Some(genre) => genre should be(fake_dev)
            case None => fail("Should return a genre.")
        }
    }

    test("Games.getAllGenres should return a list of genres") {
        val genres: Genres = new Genres()

        val fake_dev: Genre = new Genre(
            1, "fake_dev_name"
        )
        val createGenreFuture: Future[Unit] = genres.createGenre(
            fake_dev.id, fake_dev.name
        )
        Await.ready(createGenreFuture, Duration.Inf)

        val games: Genres = new Genres()

        val fake_dev2: Genre = new Genre(
            2, "fake_dev_name2"
        )
        val createAnotherGenreFuture: Future[Unit] = genres.createGenre(
            fake_dev2.id, fake_dev2.name
        )
        Await.ready(createAnotherGenreFuture, Duration.Inf)


        val returnedGenreSeqFuture: Future[Seq[Genre]] = genres.getAllGenres()
        val returnedGenreSeq: Seq[Genre] = Await.result(returnedGenreSeqFuture, Duration.Inf)

        returnedGenreSeq.length should be(2)
        returnedGenreSeq(0) should be(fake_dev)
        returnedGenreSeq(1) should be(fake_dev2)
    }

}
