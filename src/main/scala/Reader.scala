import java.io.{BufferedReader, InputStreamReader}

import com.mashape.unirest.http.Unirest
import com.mongodb._
import org.json.simple.{JSONObject, JSONValue}

import scala.collection.mutable

/**
  * Created by Thagus on 04/12/16.
  */
object Reader {
  def main(args : Array[String]): Unit = {
    //Initialize connection to MongoDB
    val mongoClient = new MongoClient( "localhost" , 27017 )
    val db: DB = mongoClient.getDB("twitter")
    val tweetsColl: DBCollection = db.getCollection("tweets")

    val cursor = tweetsColl.find()

    var text = ""

    while(cursor.hasNext){
      val document = cursor.next()
      //state(document)

      text += document.get("text") + " "
    }

    removeStopWordsText(text)
  }

  def state(document: DBObject): Unit = {
    try {
      val response = Unirest.get("http://nominatim.openstreetmap.org/reverse?format=json&lat=" + document.get("lat") + "&lon=" + document.get("lng") + "&zoom=16&addressdetails=1").asString()

      val jsonObject = JSONValue.parse(response.getBody).asInstanceOf[JSONObject]

      val address = jsonObject.get("address").asInstanceOf[JSONObject]

      if (address != null) {
        println(address.get("state") + "\t" + document.get("sentiment"))
      }
    }
    catch {
      case e: Exception => e.printStackTrace()

    }
  }

  def removeStopWordsText(text: String): Unit = {
    val stopWords = new mutable.HashSet[String]

    val inputStream = getClass.getResourceAsStream("/stopwords/es.txt")

    val br = new BufferedReader(new InputStreamReader(inputStream))

    var line: String = ""

    while ({line = br.readLine() ; line != null }){
      stopWords += line
    }
    stopWords += "https"
    stopWords += "RT"

    val words = text.split("[:,.…!?/\\s]+").filterNot(p => stopWords.contains(p))

    var newText = ""

    words.foreach(f => {
      newText += f + " "
    })

    println(newText)
  }
}
