import java.util.HashMap;
import java.util.Map;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class OpenStreetMapUtils {
    private static OpenStreetMapUtils instance = null;

    public static OpenStreetMapUtils getInstance() {
        if (instance == null) {
            instance = new OpenStreetMapUtils();
        }
        return instance;
    }

    public Map<String, Double> getCoordinates(String address) {
        Map<String, Double> res;
        StringBuffer query;
        String[] split = address.split(" ");
        String queryResult = null;

        query = new StringBuffer();
        res = new HashMap<String, Double>();

        if (split.length == 0) {
            return null;
        }

        for (int i = 0; i < split.length; i++) {
            query.append(split[i]);
            if (i < (split.length - 1)) {
                query.append("+");
            }
        }

        try {
            HttpResponse<String> response = Unirest.get("http://nominatim.openstreetmap.org/search?q=" + query.toString() + "&format=json&addressdetails=0&limit=1").asString();
            queryResult = response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (queryResult == null) {
            return null;
        }

        Object obj = JSONValue.parse(queryResult);

        if (obj instanceof JSONArray) {
            JSONArray array = (JSONArray) obj;
            if (array.size() > 0) {
                JSONObject jsonObject = (JSONObject) array.get(0);

                res.put("lon", Double.parseDouble((String) jsonObject.get("lon")));
                res.put("lat", Double.parseDouble((String) jsonObject.get("lat")));
            }
        }

        return res;
    }
}