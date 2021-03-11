
package poca

import scala.concurrent.Future
import akka.http.scaladsl.server.Directives.{path, get, post, formFieldMap, complete, concat}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.{HttpEntity, HttpResponse, ContentTypes, StatusCodes}
import com.typesafe.scalalogging.LazyLogging
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import TwirlMarshaller._
import scala.collection.mutable.HashMap
import java.sql.Timestamp
import java.time.format.DateTimeFormatter
import java.time.ZoneId
import java.time.LocalDateTime

class Routes(users: Users , developers: Developers , genres: Genres, publishers: Publishers, games : Games, comments: Comments, carts: Carts, cartlines: CartLines , wallets : Wallets, orders: Orders, orderlines: OrderLines) extends LazyLogging {
    implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global

    def getHome() = {
        logger.info("I got a request to greet.")
        val genreSeqFuture : Future[Seq[Genre]] = genres.getAllGenres()
        genreSeqFuture.map[ToResponseMarshallable] {
            case genreSeq =>
                //val gameSeqFuture: Future[Seq[Game]] = games.getAllGames()
                val devSeqFuture : Future[Seq[Developer]] = developers.getAllDevelopers()
                devSeqFuture.map[ToResponseMarshallable] {
                    case devSeq =>
                        val gameSeqFuture: Future[Seq[Game]] = games.getAllGames()
                        //gameSeqFuture.map(gameSeq => html.home(gameSeq , genreSeq, devSeq))
                        val publisherSeqFuture : Future[Seq[Publisher]] = publishers.getAllPublishers()
                        publisherSeqFuture.map[ToResponseMarshallable] {
                            case publisherSeq =>
                                //val gameSeqFuture: Future[Seq[Game]] = games.getAllGames()
                                gameSeqFuture.map(gameSeq => html.home(gameSeq , genreSeq, devSeq , publisherSeq ) )
                }
                //gameSeqFuture.map(gameSeq => html.home(gameSeq, genreSeq , ))
        }
        /*val devSeqFuture : Future[Seq[Developer]] = developers.getAllDevelopers()
        devSeqFuture.map[ToResponseMarshallable] {
            case devSeq =>
                val gameSeqFuture: Future[Seq[Game]] = games.getAllGames()
                gameSeqFuture.map(gameSeq => html.home(gameSeq, devSeq))
        }*/
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


    def getUsers() = {
        logger.info("I got a request to get user list.")

        val userSeqFuture: Future[Seq[User]] = users.getAllUsers()

        userSeqFuture.map(userSeq => html.users(userSeq))
    }
    
    def getPublishers() = {
        logger.info("I got a request to get publisher list.")

        val publisherSeqFuture: Future[Seq[Publisher]] = publishers.getAllPublishers()

        publisherSeqFuture.map(publisherSeq => html.all_publishers(publisherSeq))
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

    def getDeveloper(id: Int) = {
        logger.info("I got a request to get informations of the developer with id " + id + ".")
        
        val developer = developers.getDeveloperById(id)

        developer.map[ToResponseMarshallable] {
            case Some(developer) => {
                val dgamesSeqFuture: Future[Seq[Game]] = games.getGamesFromDeveloper(developer.id)
                dgamesSeqFuture.map(dgamesSeq => html.developer(developer, dgamesSeq))
            }
            case None => {
                html.developer(null, null)
            }
        }
    }
    
    def getGenres() = {
        logger.info("I got a request to get genre list.")

        val genreSeqFuture: Future[Seq[Genre]] = genres.getAllGenres()

        genreSeqFuture.map(genreSeq => html.genres(genreSeq))    
    }

    def getSoldeById(id : Int) = {

        /** 
        http://localhost:8080/wallet?id=1
        **/ 
        logger.info("I got a request to get Solde of the Id : " + id + ".")
        
        val wallet = wallets.getSoldeById(id)

        wallet.map[ToResponseMarshallable] {
            case Some(wallet) => {
                html.wallets(wallet.id, wallet.solde)
            }
        }
    }
    def getGenre(id: Int) = {
        logger.info("I got a request to get informations of the genre " + id + ".")
        
        val genre = genres.getGenreById(id)

        genre.map[ToResponseMarshallable] {
            case Some(genre) => {
                val pgamesSeqFuture: Future[Seq[Game]] = games.getGamesFromGenre(genre.id)
                pgamesSeqFuture.map(pgamesSeq => html.genre(genre, pgamesSeq))
            }
            case None => {
                html.genre(null, null)
            }
        }
    }

    def getGame(id : Int) = {
        logger.info("I got a request to get informations of a game.")
        
        val game = games.getGameById(id)

        game.map[ToResponseMarshallable] {
            case Some(game) => val genre = genres.getGenreById(game.id_genre)
                genre.map[ToResponseMarshallable] {
                    case Some(genre) => val dev = developers.getDeveloperById(game.id_developer)
                        dev.map[ToResponseMarshallable] {
                                case Some(dev) => val pub = publishers.getPublisherById(game.id_publisher)
                                    pub.map[ToResponseMarshallable] {
                                        case Some(pub) =>
                                        val comSeqFuture: Future[Seq[Comment]] = comments.getCommentsById(id)
                                        comSeqFuture.map(comSeq =>html.product(game, genre, dev, comSeq, pub))
                                    }
                        }
                }
                               
        }

     }
    
    def getPublisher(name: String) = {
        logger.info("I got a request to get informations of the publisher " + name + ".")
        
        val publisher = publishers.getPublisherByName(name)

        publisher.map[ToResponseMarshallable] {
            case Some(publisher) => {
                val pgamesSeqFuture: Future[Seq[Game]] = games.getGamesFromPublisher(publisher.id)
                pgamesSeqFuture.map(pgamesSeq => html.publisher(publisher, pgamesSeq))
            }
            case None => {
                html.publisher(null, null)
            }
        }
    }
    
    def addComment(fields: Map[String, String]) = {
        fields.get("commentaire") match {
            case Some(commentaire) => {
                val rate = fields.get("rate").get.toInt
                val idproduct = fields.get("idproduct").get.toInt
                val commentCreation: Future[Unit] = comments.createComment(0, iduser=1, idproduct=idproduct, comment=commentaire, nbstars=rate)

                /*userCreation.map(_ => {
                    HttpResponse(
                        StatusCodes.OK,
                        entity=s"Comment added.",
                    )
                })*/
                getGame(idproduct)
            }
        }
    }
    def creditCashToSolde( id: Int, cash: Int) = {
        logger.info("I got a request to add to wallet "+ id +" cash :" + cash + ".")
        val wallet = wallets.creditWallet(id, cash)  
        wallet.map[ToResponseMarshallable] {
            case Some(wallet) => {
                if( cash <= 0){
                    logger.info("cash < 0")
                    val w = wallets.debitWallet(id,cash)
                    w.map[ToResponseMarshallable] {
                        case Some(w) => {
                            html.wallets(w.id, w.solde)
                        }
                    }
                }else{
                    html.wallets(wallet.id, wallet.solde)
                }
            }
        }
    }
    def debitCashToSolde( id: Int, cash: Int) = {
        logger.info("I got a request to get back to wallet "+ id +" cash :" + cash + ".")
        
        val wallet = wallets.debitWallet(id, cash)  
        wallet.map[ToResponseMarshallable] {
            case Some(wallet) => {
                if( wallet.solde < 0 || cash <= 0){
                    logger.info("solde < 0 || cash <= 0")
                    val w = wallets.creditWallet(id,cash)
                    w.map[ToResponseMarshallable] {
                        case Some(w) => {
                            html.wallets(w.id, w.solde)
                        }
                    }
                }
                else{
                    html.wallets(wallet.id, wallet.solde)
                }
            }
        }
    }

    def viewCart(iduser: Int) = {
        logger.info("I got a request to get informations of the cart.")
        val cart = carts.getCartByIdUser(iduser)
        cart.map[ToResponseMarshallable] {
            case Some(cart) => {
                val cartlinesFuture = cartlines.getCartLinesByIdCart(cart.id)
                var total = 0.0
                cartlinesFuture.map[ToResponseMarshallable] {
                    seq => {
                        seq.map(item => total += item.quantity * item.price)
                        html.cart(seq, "%.2f".format(total))
                    }
                }
            }
            case None => html.cart(null, "0")
        }
    }

    def add_to_cart(fields: Map[String, String]) = {
        fields.get("id") match {
            case Some(id) =>
                val product = games.getGameById(id.toInt)
                // reseller n'étant toujours pas implémenté, on met idreseller à 1 et prix à 50
                val creationCartLine = cartlines.createCartLine(idcart=0, idproduct=id.toInt, idreseller=1, price=50.0, quantity=1)
                creationCartLine.map(_ => {
                            HttpResponse(
                                StatusCodes.OK,
                                entity = s"Product '$id' added to cart.",
                            )
                        })
        }
        
    }

    def updateCartQuantities(idcart: Int, hashmap: HashMap[(Int, Int), Int]) = {
        logger.info("I got a request to update the cart " + idcart + ".")
        hashmap.keys.foreach{
            key => hashmap(key) match {
                case 0 => cartlines.deleteCartline(idcart, key._1, key._2)
                case _ => cartlines.updateCartlineQuantity(idcart, key._1, key._2, hashmap(key))
            }
        }
        val getCartFuture: Future[Option[Cart]] = carts.getCartById(idcart)
        getCartFuture.map[ToResponseMarshallable] {
            case Some(cart) => html.cart_updated(cart.iduser)
            case None => html.cart_updated(-1)
        }
    }

    def checkout(idcart: Int) = {
        logger.info("I got a request to checkout the cart " + idcart + ".")
        carts.getCartById(idcart).map[ToResponseMarshallable] {
            case Some(cart) => {
                cartlines.getCartLinesByIdCart(idcart).map[ToResponseMarshallable] {
                    seq => {
                        val total: Double = seq.foldLeft(0.0) {(s,a) => s + a.quantity * a.price }
                        val roundTotal: Double = "%.2f".format(total).replace(",", ".").toDouble
                        wallets.getSoldeById(cart.iduser).map[ToResponseMarshallable] {
                            case Some(wallet) => {
                                html.checkout(roundTotal, wallet, idcart)
                            }
                            case None => HttpResponse(
                                        StatusCodes.OK,
                                        entity = s"The user doesn't have a wallet.",
                                    )
                        }
                    }
                }
            }
            case None => HttpResponse(
                                StatusCodes.OK,
                                entity = s"Cannot checkout a nonexistent cart with id '$idcart'.",
                            )
        }
    }

    def order(idcart: Int) = {
        logger.info(s"I got a request to create an order from the cart id '$idcart'.")
        carts.getCartById(idcart).map[ToResponseMarshallable] {
            case Some(cart) => {
                cartlines.getCartLinesByIdCart(idcart).map[ToResponseMarshallable] {
                    var total: Double = 0.0
                    seq => seq.length match {
                        case 0 => HttpResponse(
                                    StatusCodes.OK,
                                    entity = s"Cannot create an order from am empty cart.")
                        case _ => {
                            val fmt = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")
                            val time = LocalDateTime.now(ZoneId.of("America/New_York")).format(fmt)
                            orders.createOrder(1, cart.iduser, Timestamp.valueOf(time))
                            orders.getLastOrderFromUser(cart.iduser).map[ToResponseMarshallable] {
                                case Some(order) => {
                                    seq.map(
                                        cLine => {
                                            cartlines.deleteCartline(cLine.idcart, cLine.idproduct, cLine.idreseller)
                                            orderlines.createOrderLine(order.id, cLine.idproduct, cLine.idreseller, 1, cLine.price, cLine.quantity)
                                            total += cLine.price * cLine.quantity
                                        }
                                    )
                                    // arrondir au centième le total
                                    total = (total * 100).toInt / 100.0
                                    wallets.getSoldeById(order.iduser).map[ToResponseMarshallable] {
                                        case Some (wallet) => {
                                            if (wallet.solde >= total) {
                                                wallets.debitWallet(wallet.id, total)
                                                users.getEmailFromUser(cart.iduser).map[ToResponseMarshallable] {
                                                    case Some(email) => send_email(email, order.id)
                                                                        html.order_confirmed(order.id, email)
                                                    case None => HttpResponse(
                                                                    StatusCodes.OK,
                                                                    entity = s"The user doesn't have an email address.")
                                                }
                                            }
                                            else {
                                                html.checkout(total, wallet, idcart)
                                            }
                                        }
                                        case None => HttpResponse(
                                                        StatusCodes.OK,
                                                        entity = s"The user doesn't have a wallet.")
                                    }
                                }
                                case None => HttpResponse(
                                    StatusCodes.OK,
                                    entity = s"The creation of the order failed.")

                            }
                        }
                    }
                }
            }
            case None => HttpResponse(
                            StatusCodes.OK,
                            entity = s"Cannot create an order from a nonexistent cart with id '$idcart'.")
        }
    }

    def send_email(email: String, idorder: Int) = {
        logger.info(s"I got a request send an email at '$email'.")
        import mail._

        orderlines.getOrderLinesByIdOrder(idorder).map {
            olList =>  send a new Mail (
                            from = ("noreply@equipe7.fr", "Équipe 7"),
                            to = email,
                            subject = "Recap Order n° " + idorder.toString,
                            message = html.email(olList).toString
                        )
        }

        
       
    }

    def getMyOrders(iduser: Int) = {
        orders.getOrderByIdUser(iduser).map[ToResponseMarshallable] {
            myorders => html.my_orders(myorders)
        }
    }

    def viewOrder(id: Int) = {
        orderlines.getOrderLinesByIdOrder(id).map[ToResponseMarshallable] {
            myorderlines => html.order(myorderlines)
        }
    }

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
            path("users") {
                get {
                    complete(getUsers)
                }
            },
            path("all-developers") {
                get {
                    complete(getDevelopers)
                }
            },
            path("developer") {
                get {
                    parameter('id.as[Int]) {
                        id => complete(getDeveloper(id))
                    }
                }
            },
            path("genres") {
                get {
                    complete(getGenres)
                }
            },
            path("genre") {
                get {
                    parameter('id.as[Int]) { id =>
                     complete(getGenre(id))
                    }
                }
            },
            path("all-publishers") {
                get {
                    complete(getPublishers)
                }
            },
            path("publisher") {
                get {
            		parameter('name.as[String]) {
                        name => complete(getPublisher(name))
                	}
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
            },
            path("commentaire") {
                (post & formFieldMap) { fields =>
                    complete(addComment(fields))
                }
            },
            path("wallet"){
                get {
                    parameter('id.as[Int]) { id =>
                     complete(getSoldeById(id))
                    }
                }
            },
            path("add-wallet"){
                get {
                    parameter('id.as[Int], 'cash.as[Int]){
                        (id, cash) => {
                            val i = (id,cash)._1
                            val c = (id,cash)._2
                            complete( creditCashToSolde(i ,c ))
                        }
                    }
                }
            },path("debit-wallet"){
                get {
                    parameter('id.as[Int], 'cash.as[Int]){
                        (id, cash) => {
                            val i = (id,cash)._1
                            val c = (id,cash)._2
                            complete( debitCashToSolde(i ,c ))
                        }
                    }
                }
            },
            /*
            path("cart") {
                get {
                    parameter('iduser.as[Int]) {
                        iduser => complete(viewCart(iduser))
                    }
                }
            },*/
            path("add_cart"){
                (post & formFieldMap) { fields =>
                    complete(add_to_cart(fields))
                }
            },
            path("cart"){
                get {
                    complete(viewCart(1))
                }
            },
            path("cart-update-quantities") {
                get {
                    parameterMap { params => {
                        var hashmap: HashMap[(Int, Int), Int] = HashMap()
                        var idcart: Int = -1
                        params.map(param => {
                            param._1 match {
                                case "idcart" => idcart = param._2.toInt
                                case _ => {
                                    val arr: Array[String] = param._1.split("-")
                                    hashmap += ((arr(1).toInt, arr(2).toInt) -> param._2.toInt)
                                }
                            }
                            })
                        complete(updateCartQuantities(idcart, hashmap))
                        }
                    }
                }
            },
            path("checkout") {
                get {
                    parameter('idcart.as[Int]) {
                        idcart => {
                            complete(checkout(idcart))
                        }
                    }
                }
            },
            path("order") {
                get {
                    parameter('idcart.as[Int]) {
                        idcart => complete(order(idcart))
                    }
                }
            },
            path("my-orders") {
                get {
                    parameter('iduser.as[Int]) {
                        iduser => complete(getMyOrders(iduser))
                    }
                }
            },
            path("my-order") {
                get {
                    parameter('id.as[Int]) {
                        id => complete(viewOrder(id))
                    }
                }
            },
        )

}
