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
import java.sql.Timestamp
import java.time.format.DateTimeFormatter
import java.time.ZoneId
import java.time.LocalDateTime

class InsertData ( developers: Developers , genres: Genres, publishers: Publishers, games: Games, users: Users, comments: Comments, orders: Orders, orderLines: OrderLines, carts: Carts, cartlines: CartLines , wallets : Wallets ) {
	val db = MyDatabase.db

	def ClearDB(){
		val Resetdev = sqlu"TRUNCATE TABLE developer; ALTER SEQUENCE developer_id_seq MINVALUE 0 RESTART WITH 0 ; "
	    val devFuture: Future[Int] = db.run(Resetdev)
	    val dev = Await.result(devFuture, Duration.Inf)

	    val Resetgame = sqlu"TRUNCATE TABLE game ;"
	    val gameFuture: Future[Int] = db.run(Resetgame)
	    val game = Await.result(gameFuture, Duration.Inf)

	    
	    val Resetwallet = sqlu"""TRUNCATE TABLE "Wallets" ;"""
	    val walletFuture: Future[Int] = db.run(Resetwallet)
	    val wallet = Await.result(walletFuture, Duration.Inf)
		

	    val Resetpub = sqlu"TRUNCATE TABLE publisher; ALTER SEQUENCE publisher_id_seq MINVALUE 0 RESTART WITH 0 ; "
	    val pubFuture: Future[Int] = db.run(Resetpub)
	    val pub = Await.result(pubFuture, Duration.Inf)

	    val Resetgenre = sqlu"TRUNCATE TABLE genre ;ALTER SEQUENCE genre_id_seq MINVALUE 0 RESTART WITH 0 ;"
	    val genreFuture: Future[Int] = db.run(Resetgenre)
	    val genre = Await.result(genreFuture, Duration.Inf)

		val Resetuser = sqlu"TRUNCATE TABLE users ;ALTER SEQUENCE users_id_seq MINVALUE 0 RESTART WITH 0 ;"
	    val userFuture: Future[Int] = db.run(Resetuser)
	    val user = Await.result(userFuture, Duration.Inf)

		val Resetcomment = sqlu"TRUNCATE TABLE comment ;ALTER SEQUENCE comment_id_seq MINVALUE 0 RESTART WITH 0 ;"
	    val commentFuture: Future[Int] = db.run(Resetcomment)
	    val comment = Await.result(commentFuture, Duration.Inf)

		val Clearorder = sqlu"""DELETE FROM "order";"""
		val orderFuture: Future[Int] = db.run(Clearorder)
		val order = Await.result(orderFuture, Duration.Inf)

		val Clearorderline = sqlu"""DELETE FROM "orderLine";"""
		val orderlineFuture: Future[Int] = db.run(Clearorderline)
		val orderline = Await.result(orderlineFuture, Duration.Inf)


		val Resetorder = sqlu"""TRUNCATE TABLE "order" ;ALTER SEQUENCE order_id_seq MINVALUE 0 RESTART WITH 0 ;"""
	    val resetOrderFuture: Future[Int] = db.run(Resetorder)
	    val cleanedOrder = Await.result(resetOrderFuture, Duration.Inf)

/*
		val ResetorderLine = sqlu"TRUNCATE TABLE "orderLine" ;ALTER SEQUENCE order_line_id_seq MINVALUE 0 RESTART WITH 0 ;"
	    val orderLineFuture: Future[Int] = db.run(ResetorderLine)
	    val orderLine = Await.result(orderLineFuture, Duration.Inf)
		*/

		val Resetcart = sqlu"TRUNCATE TABLE cart; ALTER SEQUENCE cart_id_seq MINVALUE 0 RESTART WITH 0;"
	    val cartFuture: Future[Int] = db.run(Resetcart)
	    val cart = Await.result(cartFuture, Duration.Inf)

		val Clearcartline = sqlu"DELETE FROM cartline;"
		val cartlineFuture: Future[Int] = db.run(Clearcartline)
		val cartline = Await.result(cartlineFuture, Duration.Inf)
		/*val Resetcartline = sqlu"TRUNCATE TABLE cartline;ALTER SEQUENCE cartline_id_seq MINVALUE 0 RESTART WITH 0;"
	    val cartlineFuture: Future[Int] = db.run(Resetcartline)
	    val cartline = Await.result(cartlineFuture, Duration.Inf)*/
	}

	def FillDevelopers(){
		Await.result(developers.createDeveloper(0, "Nintendo EAD"), Duration.Inf)
		Await.result(developers.createDeveloper(1, "PUBG Corporation"), Duration.Inf)
		Await.result(developers.createDeveloper(2, "Game Freak"), Duration.Inf)
		Await.result(developers.createDeveloper(3, "Bullet Proof Software"), Duration.Inf)
		Await.result(developers.createDeveloper(4, "Mojang AB"), Duration.Inf)
		Await.result(developers.createDeveloper(5, "Nintendo R&D1"), Duration.Inf)
		Await.result(developers.createDeveloper(6, "Good Science Studio"), Duration.Inf)
		Await.result(developers.createDeveloper(7, "Rockstar North"), Duration.Inf)
		Await.result(developers.createDeveloper(8, "Nintendo SDD"), Duration.Inf)
		Await.result(developers.createDeveloper(9, "Team Garry"), Duration.Inf)
		Await.result(developers.createDeveloper(10, "Nintendo EAD / Retro Studios"), Duration.Inf)
		Await.result(developers.createDeveloper(11, "Nintendo R&D2"), Duration.Inf)
		Await.result(developers.createDeveloper(12, "Treyarch"), Duration.Inf)
		Await.result(developers.createDeveloper(13, "Nintendo EPD"), Duration.Inf)
		Await.result(developers.createDeveloper(14, "Valve Software"), Duration.Inf)
		Await.result(developers.createDeveloper(15, "Sonic Team"), Duration.Inf)
		Await.result(developers.createDeveloper(16, "Polyphony Digital"), Duration.Inf)
		Await.result(developers.createDeveloper(17, "Infinity Ward"), Duration.Inf)
		Await.result(developers.createDeveloper(18, "Bungie Studios"), Duration.Inf)
		Await.result(developers.createDeveloper(19, "Rockstar Games"), Duration.Inf)
	}

	def FillPublishers(){
		Await.result(publishers.createPublisher(0,"Nintendo"), Duration.Inf)
		Await.result(publishers.createPublisher(1,"PUBG Corporation"), Duration.Inf)
		Await.result(publishers.createPublisher(2,"Mojang"), Duration.Inf)
		Await.result(publishers.createPublisher(3,"Microsoft Game Studios"), Duration.Inf)
		Await.result(publishers.createPublisher(4,"Rockstar Games"), Duration.Inf)
		Await.result(publishers.createPublisher(5,"Unknown"), Duration.Inf)
		Await.result(publishers.createPublisher(6,"Activision"), Duration.Inf)
		Await.result(publishers.createPublisher(7,"VU Games"), Duration.Inf)
		Await.result(publishers.createPublisher(8,"Sega"), Duration.Inf)
		Await.result(publishers.createPublisher(9,"Sony Computer Entertainment"), Duration.Inf)
		Await.result(publishers.createPublisher(10,"Microsoft Studios"), Duration.Inf)
		Await.result(publishers.createPublisher(11,"Blizzard Entertainment"), Duration.Inf)
		Await.result(publishers.createPublisher(12,"EA Sports"), Duration.Inf)
		Await.result(publishers.createPublisher(13,"Electronic Arts"), Duration.Inf)
		Await.result(publishers.createPublisher(14,"Ubisoft"), Duration.Inf)

	}

	def FillGenre(){
		Await.result(genres.createGenre(0 ,"Sports"), Duration.Inf)
		Await.result(genres.createGenre(1,"Platform"), Duration.Inf)
		Await.result(genres.createGenre(2,"Racing"), Duration.Inf)
		Await.result(genres.createGenre(3,"Shooter"), Duration.Inf)
		Await.result(genres.createGenre(4,"Role-Playing"), Duration.Inf)
		Await.result(genres.createGenre(5,"Puzzle"), Duration.Inf)
		Await.result(genres.createGenre(6,"Misc"), Duration.Inf)
		Await.result(genres.createGenre(7,"Party"), Duration.Inf)
		Await.result(genres.createGenre(8,"Simulation"), Duration.Inf)
		Await.result(genres.createGenre(9,"Action"), Duration.Inf)
		Await.result(genres.createGenre(10,"Action-Adventure"), Duration.Inf)
		Await.result(genres.createGenre(11,"Fighting"), Duration.Inf)
		Await.result(genres.createGenre(12,"Strategy"), Duration.Inf)
		Await.result(genres.createGenre(13,"Adventure"), Duration.Inf)
		Await.result(genres.createGenre(14,"Music"), Duration.Inf)
		Await.result(genres.createGenre(15,"MMO"), Duration.Inf)
		Await.result(genres.createGenre(16,"Sandbox"), Duration.Inf)
		Await.result(genres.createGenre(17,"Visual Novel"), Duration.Inf)
		Await.result(genres.createGenre(18,"Board Game"), Duration.Inf)
		Await.result(genres.createGenre(19,"Education"), Duration.Inf)

	}


	def FillGame(){
		Await.result(games.createGame(0,	"Wii Sports","wii-sports"	,0	,2006	,"Wii",	"E"	,"http://www.vgchartz.com/games/boxart/full_2258645AmericaFrontccc.jpg",	0	,0 ), Duration.Inf)
		Await.result(games.createGame(2,	"Mario Kart Wii","mario-kart-wii"	,2	,2008,	"Wii",	"E"	, "http://www.vgchartz.com/games/boxart/full_8932480AmericaFrontccc.jpg" ,0,	0), Duration.Inf)
		Await.result(games.createGame(4,	"Wii Sports Resort","wii-sports-resort"	,0	,2009	,"Wii",	"E","http://www.vgchartz.com/games/boxart/full_7295041AmericaFrontccc.jpg",	0,0), Duration.Inf)
		Await.result(games.createGame(6,	"New Super Mario Bros.","new-super-mario-bros"	,1,	2006	,"DS"	,"E",	"http://www.vgchartz.com/games/boxart/full_2916260AmericaFrontccc.jpg",	0	,0), Duration.Inf)
		Await.result(games.createGame(8,	"New Super Mario Bros. Wii"	,"new-super-mario-bros-wii"	,1	,2009	,"Wii",	"E"	,"http://www.vgchartz.com/games/boxart/full_1410872AmericaFrontccc.jpg",	0	,0), Duration.Inf)
		Await.result(games.createGame(11,	"Wii Play"	,"wii-play"	,6	,2007	,"Wii",	"E","http://www.vgchartz.com/games/boxart/full_6165969AmericaFrontccc.jpg",	0	,0), Duration.Inf)
		Await.result(games.createGame(13	,"Nintendogs"	,"nintendogs"	,8	,2005	,"DS"	,"E"	,"http://www.vgchartz.com/games/boxart/full_4870931AmericaFrontccc.jpg"	,0	,0), Duration.Inf)
		Await.result(games.createGame(14	,"Mario Kart DS"	,"mario-kart-ds"	,2	,2005,	"DS"	,"E"	,"http://www.vgchartz.com/games/boxart/full_9551652AmericaFrontccc.jpg"	,0,	0), Duration.Inf)
		Await.result(games.createGame(16,	"Wii Fit"	,"wii-fit",	0	,2008	,"Wii"	,"E"	,"http://www.vgchartz.com/games/boxart/full_3619557AmericaFrontccc.jpg"	,0,	0), Duration.Inf)
		Await.result(games.createGame(17,	"Wii Fit Plus"	,"wii-fit-plus"	,0	,2009	,"Wii"	,"E"	,"http://www.vgchartz.com/games/boxart/full_2716475AmericaFrontccc.jpg"	,0	,0), Duration.Inf)
		Await.result(games.createGame(18,	"Super Mario World"	,"super-mario-world"	,1	,1991,	"SNES"	,"E",	"http://www.vgchartz.com/games/boxart/full_1091265AmericaFrontccc.jpg"	,0	,0), Duration.Inf)
		Await.result(games.createGame(24,	"Mario Kart 7"	,"mario-kart-7"	,2	,2011	,"3DS"	,"E"	,"http://www.vgchartz.com/games/boxart/full_mario-kart-7_649AmericaFront.jpg",0	,10), Duration.Inf)
		Await.result(games.createGame(27,	"Super Mario Bros. 3"	,"super-mario-bros-3",	1	,1990	,"NES"	,"E"	,"http://www.vgchartz.com/games/boxart/4113056ccc.jpg"	,0	,11), Duration.Inf)
		Await.result(games.createGame(34,	"Call of Duty: Black Ops 3"	,"call-of-duty-black-ops-3"	,3	,2015	,"PS4"	,"M"	,"http://www.vgchartz.com/games/boxart/full_4990510AmericaFrontccc.jpg",	6	,12), Duration.Inf)
		Await.result(games.createGame(35,	"Mario Kart 8 Deluxe"	,"mario-kart-8-deluxe"	,2	,2017	,"NS"	,"E"	,"http://www.vgchartz.com/games/boxart/full_895207AmericaFrontccc.png"	,0,	13), Duration.Inf)
		Await.result(games.createGame(36,	"Counter-Strike: Source","counter-strike-source",3	,2004	,"PC"	,"M"	,"http://www.vgchartz.com/games/boxart/full_9030886AmericaFrontccc.jpg"	,7	,14), Duration.Inf)
		Await.result(games.createGame(41,	"Call of Duty: Black Ops"	,"call-of-duty-black-ops",3	,2010	,"X360"	,"M"	,"http://www.vgchartz.com/games/boxart/full_call-of-duty-black-ops_5AmericaFront.jpg"	,6	,12), Duration.Inf)
		//games.createGame(46,	"Call of Duty: Black Ops II" ,"call-of-duty-black-ops-ii" ,	3	,2012	,"X360"	,"M"	,"http://www.vgchartz.com/games/boxart/full_1977964AmericaFrontccc.jpg"	,6	,12)
		Await.result(games.createGame(47,	"Call of Duty: Black Ops II" ,"call-of-duty-black-ops-ii" ,3	,2012	,"PS3"	,"M"	,"http://www.vgchartz.com/games/boxart/full_4649679AmericaFrontccc.png"	,6	,12), Duration.Inf)
		Await.result(games.createGame(48,	"Super Mario Odyssey"	,"super-mario-odyssey",	1	,2017	,"NS"	,"E10"	,"http://www.vgchartz.com/games/boxart/full_1659701AmericaFrontccc.jpg"	,0,	13), Duration.Inf)
		Await.result(games.createGame(55,	"Portal 2"	,"portal-2",	3	,2011,	"PC",	"E10",	"http://www.vgchartz.com/games/boxart/full_portal-2_617AmericaFront.jpg"	,10	,14), Duration.Inf)
		//games.createGame(59,	"Call of Duty: Black Ops"	,"call-of-duty-black-ops",	3	,2010	,"PS3"	,"M"	,"http://www.vgchartz.com/games/boxart/full_call-of-duty-black-ops_3AmericaFront.jpg",6,	12)
		Await.result(games.createGame(62,	"Animal Crossing: New Leaf"	,"animal-crossing-new-leaf",	8	,2013	,"3DS"	,"E"	,"http://www.vgchartz.com/games/boxart/full_7774244AmericaFrontccc.jpg",	0,	0), Duration.Inf)
		Await.result(games.createGame(67,	"Super Mario 64"	,"super-mario-64",	1	,1996	,"N64"	,"E",	"http://www.vgchartz.com/games/boxart/full_9007863AmericaFrontccc.jpg"	,0,	0), Duration.Inf)
		Await.result(games.createGame(70,	"Animal Crossing: Wild World"	,"animal-crossing-wild-world",	8	,2005	,"DS"	,"E"	,"http://www.vgchartz.com/games/boxart/full_8328377AmericaFrontccc.jpg"	,0,	0), Duration.Inf)
		Await.result(games.createGame(71,	"The Legend of Zelda: Breath of the Wild"	,"the-legend-of-zelda-breath-of-the-wild",	10	,2017	,"NS"	,"E10"	,"http://www.vgchartz.com/games/boxart/full_4436858AmericaFrontccc.png"	,0	,13), Duration.Inf)
		Await.result(games.createGame(74,	"Super Mario 64 DS"	,"super-mario-64-ds",	1,	2004	,"DS"	,"E"	,"http://www.vgchartz.com/games/boxart/full_1719347AmericaFrontccc.jpg"	,0,	0), Duration.Inf)
		Await.result(games.createGame(80,	"Super Mario All-Stars"	,"super-mario-all-stars",	1	,1993	,"SNES"	,"E"	,"http://www.vgchartz.com/games/boxart/8836826ccc.jpg"	,0	,0), Duration.Inf)
		Await.result(games.createGame(91,	"Mario Kart 64"	,"mario-kart-64",	2	,1997,	"N64"	,"E"	,"http://www.vgchartz.com/games/boxart/full_8835641AmericaFrontccc.jpg"	,0,	0), Duration.Inf)
		Await.result(games.createGame(97,	"Call of Duty: Black Ops IIII" ,"call-of-duty-black-ops-iiii",	3	,2018	,"PS4"	,"M"	,"http://www.vgchartz.com/games/boxart/full_6532460AmericaFrontccc.jpg"	,6	,12), Duration.Inf)
		Await.result(games.createGame(103,	"Half-Life 2"	,"half-life-2",	3	,2004	,"PC"	,"M"	,"http://www.vgchartz.com/games/boxart/6354662ccc.jpg",	7	,14), Duration.Inf)
		Await.result(games.createGame(106,	"Super Mario Kart"	,"super-mario-kart",	2	,1992,	"SNES",	"E"	,"http://www.vgchartz.com/games/boxart/full_3980311AmericaFrontccc.jpg"	,0	,0), Duration.Inf)
		Await.result(games.createGame(117,	"Splatoon 2"	,"splatoon-2",	3	,2017,	"NS"	,"E10"	,"http://www.vgchartz.com/games/boxart/full_7536933AmericaFrontccc.png"	,0,	13), Duration.Inf)
		Await.result(games.createGame(127,	"The Legend of Zelda: Ocarina of Time"	,"the-legend-of-zelda-ocarina-of-time",	13	,1998	,"N64"	,"E"	,"http://www.vgchartz.com/games/boxart/full_1349358AmericaFrontccc.jpg"	,0	,0), Duration.Inf)
		Await.result(games.createGame(130,	"Call of Duty: World at War"	,"call-of-duty-world-at-war",	3,	2008	,"X360"	,"M"	,"http://www.vgchartz.com/games/boxart/full_3557116AmericaFrontccc.jpg"	,6	,12), Duration.Inf)
		//games.createGame(134,	"Call of Duty: Black Ops 3"	,"call-of-duty-black-ops-3",	3	,2015	,"XOne",	"M",	"http://www.vgchartz.com/games/boxart/full_8414457AmericaFrontccc.jpg",	6	,12)
		Await.result(games.createGame(136,	"The Legend of Zelda: Twilight Princess"	,"the-legend-of-zelda-twilight-princess",13	,2006	,"Wii"	,"T"	,"http://www.vgchartz.com/games/boxart/full_2379938AmericaFrontccc.jpg"	,0	,0), Duration.Inf)
		Await.result(games.createGame(146,	"Mario Kart: Double Dash!!"	,"mario-kart-double-dash",	2	,2003,	"GC",	"E",	"http://www.vgchartz.com/games/boxart/7829386ccc.jpg"	,0,	0), Duration.Inf)
		Await.result(games.createGame(174,	"Big Brain Academy"	,"big-brain-academy",	6	,2006	,"DS"	,"E"	,"http://www.vgchartz.com/games/boxart/full_8805912AmericaFrontccc.jpg"	,0	,0), Duration.Inf)
		Await.result(games.createGame(183,	"Super Mario Sunshine"	,"super-mario-sunshine",	1	,2002,	"GC"	,"E"	,"http://www.vgchartz.com/games/boxart/full_7690529AmericaFrontccc.jpg"	,0	,0), Duration.Inf)
		Await.result(games.createGame(187,	"Link's Crossbow Training"	,"links-crossbow-training",	3,	2007	,"Wii"	,"T"	,"http://www.vgchartz.com/games/boxart/full_links-crossbow-training_1AmericaFront.jpg"	,0,	0), Duration.Inf)
		Await.result(games.createGame(189,	"New Super Mario Bros. U"	,"new-super-mario-bros-u",	1	,2012	,"WiiU",	"E"	,"http://www.vgchartz.com/games/boxart/full_3288399AmericaFrontccc.jpg"	,0	,0), Duration.Inf)
		Await.result(games.createGame(194,	"Super Mario World: Super Mario Advance 2"	,"super-mario-world-super-mario-advance-2",	1	,2002	,"GBA"	,"E"	,"http://www.vgchartz.com/games/boxart/5020276ccc.jpg"	,0	,0), Duration.Inf)
		Await.result(games.createGame(199,	"Super Mario Advance"	,"super-mario-advance",	1	,2001	,"GBA"	,"E"	,"http://www.vgchartz.com/games/boxart/1255012ccc.png"	,0	,0), Duration.Inf)
		//Await.result(games.createGame(212,	"Call of Duty: World at War"	,"call-of-duty-world-at-war",	3,	2008,	"PS3"	,"M"	,"http://www.vgchartz.com/games/boxart/full_4070815AmericaFrontccc.jpg"	,6,	12), Duration.Inf)
		Await.result(games.createGame(310,	"Spider-Man: The Movie"	,"spider-man-the-movie",	9	,2002	,"PS2"	,"E"	,"http://www.vgchartz.com/games/boxart/full_9484527AmericaFrontccc.jpg",	6	,12), Duration.Inf)
		Await.result(games.createGame(311,	"Nintendogs + cats"	,"nintendogs-cats",	8	,2011	,"3DS"	,"E"	,"http://www.vgchartz.com/games/boxart/full_98856AmericaFrontccc.jpg"	,0	,0), Duration.Inf)
		Await.result(games.createGame(313,	"The Legend of Zelda: The Wind Waker"	,"the-legend-of-zelda-the-wind-waker",	13	,2003,	"GC",	"E"	,"http://www.vgchartz.com/games/boxart/full_6636308AmericaFrontccc.jpg",	0	,0), Duration.Inf)
	}

	def FillUser(){
		Await.result(users.createUser(1, "Jean-Edouard", "de la FesMol", "JEF@gmail.com", "root", "addressJEF", "0666666666", "root"), Duration.Inf)
		Await.result(users.createUser(2, "Marie", "Kartiol", "lp.cadiou1996@gmail.com", "root", "addressMK", "0777777777", "root"), Duration.Inf)
		Await.result(users.createUser(3, "Nabila", "Aliban", "NA@gmail.com", "root", "addressNA", "0888888888", "root"), Duration.Inf)
		Await.result(users.createUser(4, "Paul", "Sambousek", "PS@gmail.com", "root", "addressPS", "0111111111", "root"), Duration.Inf)
		Await.result(users.createUser(5, "Carlita", "Walla", "CW@gmail.com", "root", "addressCW", "0222222222", "root"), Duration.Inf)
	}

	def FillOrder(){
		val fmt = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")
		val time = LocalDateTime.now(ZoneId.of("America/New_York")).format(fmt)
		Await.result(orders.createOrder(1,3, Timestamp.valueOf(time)), Duration.Inf)
		Await.result(orders.createOrder(2,1, Timestamp.valueOf(time)), Duration.Inf)
	}


	def FillOrderLine(){
		Await.result(orderLines.createOrderLine(9999, 0, 1, 1, 10.0, 1), Duration.Inf)
		Await.result(orderLines.createOrderLine(9999, 2, 1, 1, 10.0, 1), Duration.Inf)
		Await.result(orderLines.createOrderLine(1001, 0, 1, 1, 10.0, 1), Duration.Inf)
		Await.result(orderLines.createOrderLine(1001, 4, 1, 1, 10.0, 1), Duration.Inf)
	}

	def FillComment(){
		Await.result(comments.createComment(1, 1, 0, "Le jeu est bien mais la.cartouche bug souvent.", 3), Duration.Inf)
		Await.result(comments.createComment(2, 1, 0, "Juste une tuerie ce jeu", 5), Duration.Inf)
		Await.result(comments.createComment(3, 1, 55, "Très bon jeu les graphismes sont très bien.", 5), Duration.Inf)
		Await.result(comments.createComment(4, 1, 189, "Super jeux avec une remise et reçu quelques jours après par mail un code pour le jeu pour débloquer super cool", 5), Duration.Inf)
		Await.result(comments.createComment(5, 2, 0, "En 4 heures de jeux , j’ai eux 9 crash ou le jeu se ferme seul sans sauvegarder , j’ai eux aussi un chargement en plein liberté qui a duré 10 min puis j’ai éteint sa avancer pas plus .", 1), Duration.Inf)
		Await.result(comments.createComment(6, 2, 2, "Le jeu crash trop souvent", 2), Duration.Inf)
		Await.result(comments.createComment(7, 2, 4, "Reçu en très bon état mais je n'ai pas reçu le bonus de précommande (mission bonus) qui était promis.", 4), Duration.Inf)
		Await.result(comments.createComment(8, 3, 0, "Je mets 4 étoiles parce que le jeu est bien et qu'il y a plusieurs niveaux", 4), Duration.Inf)
		Await.result(comments.createComment(9, 3, 2, "0 bug très fluide monde ouvert très beau jeu mes mdr facile de faire un monde ouvert sans scénario y a rien il manque cruellement d échange et de personnages c est monotones et ennuyeux pour un jeu avec temps de potentiel.", 3), Duration.Inf)
		Await.result(comments.createComment(10, 4, 0, "Super jeu!", 5), Duration.Inf)
		Await.result(comments.createComment(11, 1, 189, "Une bouse sans nom", 1), Duration.Inf)
		Await.result(comments.createComment(12, 2, 174, "Livraison rapide , jeux vraiment fun", 4), Duration.Inf)
		Await.result(comments.createComment(13, 3, 174, " le jeu est vraiment mauvais", 1), Duration.Inf)
		Await.result(comments.createComment(14, 1, 62, "Rien à redire sur le rpoduit. En revanche la boîte du jeu à été abîmée lors de la livraison.", 1), Duration.Inf)
		Await.result(comments.createComment(15, 2, 62, "très bon jeu, par contre je n ai pas reçu le code de mission bonus alors que je l’ai précommandé .", 4), Duration.Inf)
		Await.result(comments.createComment(16, 3, 103, "Jeux pas trop mal, dommage qu'il n'est pas de mode versus", 3), Duration.Inf)
		Await.result(comments.createComment(17, 1, 74, "J’ai joué 2 jours car ce jeu est nul", 2), Duration.Inf)
		Await.result(comments.createComment(18, 2, 74, "Très bon jeu arrivée en avance bien emballé bon état fonctionne très bien", 5), Duration.Inf)
		Await.result(comments.createComment(19, 3, 74, "Livré sous blister envoie du code cadeau par mail", 5), Duration.Inf)
		Await.result(comments.createComment(20, 2, 136, "J’adore ! Enfin un jeu a monde ouvert.", 5), Duration.Inf)
	}

	def FillCart() {
		Await.result(carts.createCart(0, 1), Duration.Inf)
	}

	def FillCartLine() {
		Await.result(cartlines.createCartLine(0, 136, 1, 59.99, 1), Duration.Inf)
		Await.result(cartlines.createCartLine(0, 313, 2, 45.0, 1), Duration.Inf)
		Await.result(cartlines.createCartLine(0, 71, 1, 59.99, 1), Duration.Inf)
	}
	def FillWallets() {
		Await.result(wallets.createWallets(1 , 500 ), Duration.Inf)
		Await.result(wallets.createWallets(2 , 200 ), Duration.Inf)
		Await.result(wallets.createWallets(3 , 300 ), Duration.Inf)
		Await.result(wallets.createWallets(4 , 400 ), Duration.Inf)
		Await.result(wallets.createWallets(5 , 500 ), Duration.Inf)

	}
}