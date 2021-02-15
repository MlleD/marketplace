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

class InsertData ( developers: Developers , genres: Genres, publishers: Publishers, games : Games ) {
	val db = MyDatabase.db

	def ClearDB(){
		val Resetdev = sqlu"TRUNCATE TABLE developer; ALTER SEQUENCE developer_id_seq MINVALUE 0 RESTART WITH 0 ; "
	    val devFuture: Future[Int] = db.run(Resetdev)
	    val dev = Await.result(devFuture, Duration.Inf)

	    val Resetgame = sqlu"TRUNCATE TABLE game ;"
	    val gameFuture: Future[Int] = db.run(Resetgame)
	    val game = Await.result(gameFuture, Duration.Inf)

	    val Resetpub = sqlu"TRUNCATE TABLE publisher; ALTER SEQUENCE publisher_id_seq MINVALUE 0 RESTART WITH 0 ; "
	    val pubFuture: Future[Int] = db.run(Resetpub)
	    val pub = Await.result(pubFuture, Duration.Inf)

	    val Resetgenre = sqlu"TRUNCATE TABLE genre ;ALTER SEQUENCE genre_id_seq MINVALUE 0 RESTART WITH 0 ;"
	    val genreFuture: Future[Int] = db.run(Resetgenre)
	    val genre = Await.result(genreFuture, Duration.Inf)

	}

	def FillDevelopers(){
		developers.createDeveloper(0, "Nintendo EAD")
		developers.createDeveloper(1, "PUBG Corporation")
		developers.createDeveloper(2, "Game Freak")
		developers.createDeveloper(3, "Bullet Proof Software")
		developers.createDeveloper(4, "Mojang AB")
		developers.createDeveloper(5, "Nintendo R&D1")
		developers.createDeveloper(6, "Good Science Studio")
		developers.createDeveloper(7, "Rockstar North")
		developers.createDeveloper(8, "Nintendo SDD")
		developers.createDeveloper(9, "Team Garry")
		developers.createDeveloper(10, "Nintendo EAD / Retro Studios")
		developers.createDeveloper(11, "Nintendo R&D2")
		developers.createDeveloper(12, "Treyarch")
		developers.createDeveloper(13, "Nintendo EPD")
		developers.createDeveloper(14, "Valve Software")
		developers.createDeveloper(15, "Sonic Team")
		developers.createDeveloper(16, "Polyphony Digital")
		developers.createDeveloper(17, "Infinity Ward")
		Await.result(developers.createDeveloper(18, "Bungie Studios"), Duration.Inf)
		Await.result(developers.createDeveloper(19, "Rockstar Games"), Duration.Inf)
	}

	def FillPublishers(){
		publishers.createPublisher(0,"Nintendo")
		publishers.createPublisher(1,"PUBG Corporation")
		publishers.createPublisher(2,"Mojang")
		publishers.createPublisher(3,"Microsoft Game Studios")
		publishers.createPublisher(4,"Rockstar Games")
		publishers.createPublisher(5,"Unknown")
		publishers.createPublisher(6,"Activision")
		publishers.createPublisher(7,"VU Games")
		publishers.createPublisher(8,"Sega")
		publishers.createPublisher(9,"Sony Computer Entertainment")
		publishers.createPublisher(10,"Microsoft Studios")
		publishers.createPublisher(11,"Blizzard Entertainment")
		publishers.createPublisher(12,"EA Sports")
		Await.result(publishers.createPublisher(13,"Electronic Arts"), Duration.Inf)
		Await.result(publishers.createPublisher(14,"Ubisoft"), Duration.Inf)

	}

	def FillGenre(){
		genres.createGenre(0 ,"Sports")
		genres.createGenre(1,"Platform")
		genres.createGenre(2,"Racing")
		genres.createGenre(3,"Shooter")
		genres.createGenre(4,"Role-Playing")
		genres.createGenre(5,"Puzzle")
		genres.createGenre(6,"Misc")
		genres.createGenre(7,"Party")
		genres.createGenre(8,"Simulation")
		genres.createGenre(9,"Action")
		genres.createGenre(10,"Action-Adventure")
		genres.createGenre(11,"Fighting")
		genres.createGenre(12,"Strategy")
		genres.createGenre(13,"Adventure")
		genres.createGenre(14,"Music")
		genres.createGenre(15,"MMO")
		genres.createGenre(16,"Sandbox")
		genres.createGenre(17,"Visual Novel")
		Await.result(genres.createGenre(18,"Board Game"), Duration.Inf)
		Await.result(genres.createGenre(19,"Education"), Duration.Inf)

	}


	def FillGame(){
		games.createGame(0,	"Wii Sports","wii-sports"	,0	,2006	,"Wii",	"E"	,"http://www.vgchartz.com/games/boxart/full_2258645AmericaFrontccc.jpg",	0	,0 )
		games.createGame(2,	"Mario Kart Wii","mario-kart-wii"	,2	,2008,	"Wii",	"E"	, "http://www.vgchartz.com/games/boxart/full_8932480AmericaFrontccc.jpg" ,0,	0)
		games.createGame(4,	"Wii Sports Resort","wii-sports-resort"	,0	,2009	,"Wii",	"E","http://www.vgchartz.com/games/boxart/full_7295041AmericaFrontccc.jpg",	0,0)
		games.createGame(6,	"New Super Mario Bros.","new-super-mario-bros"	,1,	2006	,"DS"	,"E",	"http://www.vgchartz.com/games/boxart/full_2916260AmericaFrontccc.jpg",	0	,0)
		games.createGame(8,	"New Super Mario Bros. Wii"	,"new-super-mario-bros-wii"	,1	,2009	,"Wii",	"E"	,"http://www.vgchartz.com/games/boxart/full_1410872AmericaFrontccc.jpg",	0	,0)
		games.createGame(11,	"Wii Play"	,"wii-play"	,6	,2007	,"Wii",	"E","http://www.vgchartz.com/games/boxart/full_6165969AmericaFrontccc.jpg",	0	,0)
		games.createGame(13	,"Nintendogs"	,"nintendogs"	,8	,2005	,"DS"	,"E"	,"http://www.vgchartz.com/games/boxart/full_4870931AmericaFrontccc.jpg"	,0	,0)
		games.createGame(14	,"Mario Kart DS"	,"mario-kart-ds"	,2	,2005,	"DS"	,"E"	,"http://www.vgchartz.com/games/boxart/full_9551652AmericaFrontccc.jpg"	,0,	0)
		games.createGame(16,	"Wii Fit"	,"wii-fit",	0	,2008	,"Wii"	,"E"	,"http://www.vgchartz.com/games/boxart/full_3619557AmericaFrontccc.jpg"	,0,	0)
		games.createGame(17,	"Wii Fit Plus"	,"wii-fit-plus"	,0	,2009	,"Wii"	,"E"	,"http://www.vgchartz.com/games/boxart/full_2716475AmericaFrontccc.jpg"	,0	,0)
		games.createGame(18,	"Super Mario World"	,"super-mario-world"	,1	,1991,	"SNES"	,"E",	"http://www.vgchartz.com/games/boxart/full_1091265AmericaFrontccc.jpg"	,0	,0)
		games.createGame(24,	"Mario Kart 7"	,"mario-kart-7"	,2	,2011	,"3DS"	,"E"	,"http://www.vgchartz.com/games/boxart/full_mario-kart-7_649AmericaFront.jpg",0	,10)
		games.createGame(27,	"Super Mario Bros. 3"	,"super-mario-bros-3",	1	,1990	,"NES"	,"E"	,"http://www.vgchartz.com/games/boxart/4113056ccc.jpg"	,0	,11)
		games.createGame(34,	"Call of Duty: Black Ops 3"	,"call-of-duty-black-ops-3"	,3	,2015	,"PS4"	,"M"	,"http://www.vgchartz.com/games/boxart/full_4990510AmericaFrontccc.jpg",	6	,12)
		games.createGame(35,	"Mario Kart 8 Deluxe"	,"mario-kart-8-deluxe"	,2	,2017	,"NS"	,"E"	,"http://www.vgchartz.com/games/boxart/full_895207AmericaFrontccc.png"	,0,	13)
		games.createGame(36,	"Counter-Strike: Source","counter-strike-source",3	,2004	,"PC"	,"M"	,"http://www.vgchartz.com/games/boxart/full_9030886AmericaFrontccc.jpg"	,7	,14)
		games.createGame(41,	"Call of Duty: Black Ops"	,"call-of-duty-black-ops",3	,2010	,"X360"	,"M"	,"http://www.vgchartz.com/games/boxart/full_call-of-duty-black-ops_5AmericaFront.jpg"	,6	,12)
		games.createGame(46,	"Call of Duty: Black Ops II" ,"call-of-duty-black-ops-ii" ,	3	,2012	,"X360"	,"M"	,"http://www.vgchartz.com/games/boxart/full_1977964AmericaFrontccc.jpg"	,6	,12)
		games.createGame(47,	"Call of Duty: Black Ops II" ,"call-of-duty-black-ops-ii" ,3	,2012	,"PS3"	,"M"	,"http://www.vgchartz.com/games/boxart/full_4649679AmericaFrontccc.png"	,6	,12)
		games.createGame(48,	"Super Mario Odyssey"	,"super-mario-odyssey",	1	,2017	,"NS"	,"E10"	,"http://www.vgchartz.com/games/boxart/full_1659701AmericaFrontccc.jpg"	,0,	13)
		games.createGame(55,	"Portal 2"	,"portal-2",	3	,2011,	"PC",	"E10",	"http://www.vgchartz.com/games/boxart/full_portal-2_617AmericaFront.jpg"	,10	,14)
		games.createGame(59,	"Call of Duty: Black Ops"	,"call-of-duty-black-ops",	3	,2010	,"PS3"	,"M"	,"http://www.vgchartz.com/games/boxart/full_call-of-duty-black-ops_3AmericaFront.jpg",6,	12)
		games.createGame(62,	"Animal Crossing: New Leaf"	,"animal-crossing-new-leaf",	8	,2013	,"3DS"	,"E"	,"http://www.vgchartz.com/games/boxart/full_7774244AmericaFrontccc.jpg",	0,	0)
		games.createGame(67,	"Super Mario 64"	,"super-mario-64",	1	,1996	,"N64"	,"E",	"http://www.vgchartz.com/games/boxart/full_9007863AmericaFrontccc.jpg"	,0,	0)
		games.createGame(70,	"Animal Crossing: Wild World"	,"animal-crossing-wild-world",	8	,2005	,"DS"	,"E"	,"http://www.vgchartz.com/games/boxart/full_8328377AmericaFrontccc.jpg"	,0,	0)
		games.createGame(71,	"The Legend of Zelda: Breath of the Wild"	,"the-legend-of-zelda-breath-of-the-wild",	10	,2017	,"NS"	,"E10"	,"http://www.vgchartz.com/games/boxart/full_4436858AmericaFrontccc.png"	,0	,13)
		games.createGame(74,	"Super Mario 64 DS"	,"super-mario-64-ds",	1,	2004	,"DS"	,"E"	,"http://www.vgchartz.com/games/boxart/full_1719347AmericaFrontccc.jpg"	,0,	0)
		games.createGame(80,	"Super Mario All-Stars"	,"super-mario-all-stars",	1	,1993	,"SNES"	,"E"	,"http://www.vgchartz.com/games/boxart/8836826ccc.jpg"	,0	,0)
		games.createGame(91,	"Mario Kart 64"	,"mario-kart-64",	2	,1997,	"N64"	,"E"	,"http://www.vgchartz.com/games/boxart/full_8835641AmericaFrontccc.jpg"	,0,	0)
		games.createGame(97,	"Call of Duty: Black Ops IIII" ,"call-of-duty-black-ops-iiii",	3	,2018	,"PS4"	,"M"	,"http://www.vgchartz.com/games/boxart/full_6532460AmericaFrontccc.jpg"	,6	,12)
		games.createGame(103,	"Half-Life 2"	,"half-life-2",	3	,2004	,"PC"	,"M"	,"http://www.vgchartz.com/games/boxart/6354662ccc.jpg",	7	,14)
		games.createGame(106,	"Super Mario Kart"	,"super-mario-kart",	2	,1992,	"SNES",	"E"	,"http://www.vgchartz.com/games/boxart/full_3980311AmericaFrontccc.jpg"	,0	,0)
		games.createGame(117,	"Splatoon 2"	,"splatoon-2",	3	,2017,	"NS"	,"E10"	,"http://www.vgchartz.com/games/boxart/full_7536933AmericaFrontccc.png"	,0,	13)
		games.createGame(127,	"The Legend of Zelda: Ocarina of Time"	,"the-legend-of-zelda-ocarina-of-time",	13	,1998	,"N64"	,"E"	,"http://www.vgchartz.com/games/boxart/full_1349358AmericaFrontccc.jpg"	,0	,0)
		games.createGame(130,	"Call of Duty: World at War"	,"call-of-duty-world-at-war",	3,	2008	,"X360"	,"M"	,"http://www.vgchartz.com/games/boxart/full_3557116AmericaFrontccc.jpg"	,6	,12)
		games.createGame(134,	"Call of Duty: Black Ops 3"	,"call-of-duty-black-ops-3",	3	,2015	,"XOne",	"M",	"http://www.vgchartz.com/games/boxart/full_8414457AmericaFrontccc.jpg",	6	,12)
		games.createGame(136,	"The Legend of Zelda: Twilight Princess"	,"the-legend-of-zelda-twilight-princess",13	,2006	,"Wii"	,"T"	,"http://www.vgchartz.com/games/boxart/full_2379938AmericaFrontccc.jpg"	,0	,0)
		games.createGame(146,	"Mario Kart: Double Dash!!"	,"mario-kart-double-dash",	2	,2003,	"GC",	"E",	"http://www.vgchartz.com/games/boxart/7829386ccc.jpg"	,0,	0)
		games.createGame(174,	"Big Brain Academy"	,"big-brain-academy",	6	,2006	,"DS"	,"E"	,"http://www.vgchartz.com/games/boxart/full_8805912AmericaFrontccc.jpg"	,0	,0)
		games.createGame(183,	"Super Mario Sunshine"	,"super-mario-sunshine",	1	,2002,	"GC"	,"E"	,"http://www.vgchartz.com/games/boxart/full_7690529AmericaFrontccc.jpg"	,0	,0)
		games.createGame(187,	"Link's Crossbow Training"	,"links-crossbow-training",	3,	2007	,"Wii"	,"T"	,"http://www.vgchartz.com/games/boxart/full_links-crossbow-training_1AmericaFront.jpg"	,0,	0)
		games.createGame(189,	"New Super Mario Bros. U"	,"new-super-mario-bros-u",	1	,2012	,"WiiU",	"E"	,"http://www.vgchartz.com/games/boxart/full_3288399AmericaFrontccc.jpg"	,0	,0)
		games.createGame(194,	"Super Mario World: Super Mario Advance 2"	,"super-mario-world-super-mario-advance-2",	1	,2002	,"GBA"	,"E"	,"http://www.vgchartz.com/games/boxart/5020276ccc.jpg"	,0	,0)
		Await.result(games.createGame(199,	"Super Mario Advance"	,"super-mario-advance",	1	,2001	,"GBA"	,"E"	,"http://www.vgchartz.com/games/boxart/1255012ccc.png"	,0	,0), Duration.Inf)
		//Await.result(games.createGame(212,	"Call of Duty: World at War"	,"call-of-duty-world-at-war",	3,	2008,	"PS3"	,"M"	,"http://www.vgchartz.com/games/boxart/full_4070815AmericaFrontccc.jpg"	,6,	12), Duration.Inf)
		Await.result(games.createGame(310,	"Spider-Man: The Movie"	,"spider-man-the-movie",	9	,2002	,"PS2"	,"E"	,"http://www.vgchartz.com/games/boxart/full_9484527AmericaFrontccc.jpg",	6	,12), Duration.Inf)
		Await.result(games.createGame(311,	"Nintendogs + cats"	,"nintendogs-cats",	8	,2011	,"3DS"	,"E"	,"http://www.vgchartz.com/games/boxart/full_98856AmericaFrontccc.jpg"	,0	,0), Duration.Inf)
		Await.result(games.createGame(313,	"The Legend of Zelda: The Wind Waker"	,"the-legend-of-zelda-the-wind-waker",	13	,2003,	"GC",	"E"	,"http://www.vgchartz.com/games/boxart/full_6636308AmericaFrontccc.jpg",	0	,0), Duration.Inf)
	}

}