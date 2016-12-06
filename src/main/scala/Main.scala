import scala.util.Try
import com.cybozu.labs.langdetect.DetectorFactory
import com.mongodb.{BasicDBObject, DB, DBCollection, MongoClient}
import twitter4j._

/**
  * Created by Thagus on 08/09/16.
  */
object Main {
  //Initialize connection to MongoDB
  val mongoClient = new MongoClient( "localhost" , 27017 )
  val db: DB = mongoClient.getDB("twitter")
  val tweetsColl: DBCollection = db.getCollection("tweets")

  def main(args : Array[String]) {
    System.setProperty("twitter4j.oauth.consumerKey", Constants.consumerKey)
    System.setProperty("twitter4j.oauth.consumerSecret", Constants.consumerSecret)
    System.setProperty("twitter4j.oauth.accessToken", Constants.accessToken)
    System.setProperty("twitter4j.oauth.accessTokenSecret", Constants.accessTokenSecret)

    //Load language profiles folder from resources
    DetectorFactory.loadProfile(this.getClass.getResource("languageProfiles").getFile)

    //Define Twitter stream listener
    val listener = new StatusListener(){
      def onStatus(t : Status) {
        val language = detectLanguage(onlyWords(t.getText))

        if (language == "es" || language == "en") {
          val geoLoc = t.getGeoLocation
          val userLocation = t.getUser.getLocation
          var lat, lng = 0.0

          if (geoLoc != null) {
            lat = geoLoc.getLatitude
            lng = geoLoc.getLongitude
          }
          else if (userLocation != null) {
            //Find the coordinates for the user location
            val coords = OpenStreetMapUtils.getInstance().getCoordinates(userLocation)
            lat = coords.getOrDefault("lat", 0.0)
            lng = coords.getOrDefault("lon", 0.0)
          }
          else {
            println("The location is null")
          }

          val text = t.getText
          val sentiment = getSentiment(onlyWords(t.getText), language)

          println(t.getUser.getName + "(" + t.getUser.getScreenName + ") " + sentiment + ".- " + text)

          val doc = new BasicDBObject()

          doc.append("id", t.getId)
          doc.append("user", t.getUser.getScreenName)
          doc.append("created_at", t.getCreatedAt.toInstant.toString)
          doc.append("location", userLocation)
          doc.append("lat", lat)
          doc.append("lng", lng)
          doc.append("text", text)
          doc.append("retweet", t.getRetweetCount)
          doc.append("language", language)
          doc.append("sentiment", sentiment)

          tweetsColl.insert(doc)
        }
      }
      def onDeletionNotice(statusDeletionNotice : StatusDeletionNotice) {}
      def onTrackLimitationNotice(numberOfLimitedStatuses : Int) {}
      def onException(ex : Exception) {
        ex.printStackTrace()
      }
      def onStallWarning(stallWarning: StallWarning): Unit = {}
      def onScrubGeo(l: Long, l1: Long): Unit = {}
    }


    val filter = new FilterQuery()
    filter.track("INADEM", "@INADEM_SE", "#MujeresPYME", "#INADEM")

    val twitterStream = new TwitterStreamFactory().getInstance()
    twitterStream.addListener(listener)
    twitterStream.filter(filter)
  }

  def getSentiment(text : String, lang : String) : Double = {
    MeaningCloudUtils.getInstance().getSentiment(text, lang) match {
      case "NONE" => 0.0
      case "N+" => 1.0
      case "N" => 2.0
      case "NEU" => 3.0
      case "P" => 4.0
      case "P+" => 5.0
      case _ => -1.0
    }
  }

  def detectLanguage(text: String) : String = {
    Try {
      val detector = DetectorFactory.create()
      detector.append(text)
      detector.detect()
    }.getOrElse("unknown")
  }

  def onlyWords(text: String) : String = {
    text.split(" ").filter(_.matches("^[a-zA-Z0-9 ]+$")).fold("")((a,b) => a + " " + b).trim
  }
}