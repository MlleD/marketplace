package poca

import slick.jdbc.PostgresProfile.api._

class GameTable(tag: Tag) extends Table[(Int, String, String, Int, Double, String, String, String, Int, Int)](tag, "game") {
        def id = column[Int]("id", O.PrimaryKey)
        def name = column[String]("name",O.Length(255))
        def basename = column[String]("basename",O.Length(255))
        def id_genre = column[Int]("id_genre",O.Length(10))
        def year = column[Double]("year",O.Length(10))
        def plateform = column[String]("plateform",O.Length(255))
        def ESRB = column[String]("ESRB",O.Length(255))
        def url_image = column[String]("url_image",O.Length(255))
        def id_publisher = column[Int]("id_publisher",O.Length(10))
        def id_developer = column[Int]("id_developer",O.Length(10))
        def * = (id,name,basename,id_genre,year,plateform,ESRB,url_image,id_publisher,id_developer)
    }