import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class MeaningCloudUtils {
    private static MeaningCloudUtils instance = null;

    public static MeaningCloudUtils getInstance() {
        if (instance == null) {
            instance = new MeaningCloudUtils();
        }
        return instance;
    }

    public String getSentiment(String text, String language) {
        try {
            do {
                HttpResponse<String> response = Unirest.post("http://api.meaningcloud.com/sentiment-2.1")
                        .header("content-type", "application/x-www-form-urlencoded")
                        .body("key=" + Constants.meaningCloudKey + "&lang=" + language + "&txt=" + text + "&model=general")
                        .asString();

                JSONObject jsonObject = (JSONObject) JSONValue.parse(response.getBody());

                JSONObject status = (JSONObject) jsonObject.get("status");
                String code = status.get("code").toString();

                if (code.equals("104")) {
                    System.out.println("Exceeded requests per second, waiting 5 seconds");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    return (String) jsonObject.get("score_tag");
                }
            }
            while (true);

        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return "NONE";
    }
}