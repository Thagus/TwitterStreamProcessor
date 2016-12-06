import com.mongodb.*;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Created by Thagus on 05/12/16.
 */
public class PieCharts extends Application{
    public static void main(String[] args){
        launch(args);
    }

    public void start(Stage stage) throws Exception {

        MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
        DB facebookDB = mongoClient.getDB("facebook");
        DBCollection commentsColl = facebookDB.getCollection("comments");

        DB twitterDB = mongoClient.getDB("twitter");
        DBCollection tweetsColl = twitterDB.getCollection("tweets");

        DBCursor cursor = commentsColl.find();
        int[] sentimentCountFacebook = countSentiment(cursor);
        cursor = tweetsColl.find();
        int[] sentimentCountTwitter = countSentiment(cursor);

        HBox layout = new HBox();


        ObservableList<PieChart.Data> pieChartDataFacebook =
                FXCollections.observableArrayList(
                        new PieChart.Data("Muy negativo", sentimentCountFacebook[0]),
                        new PieChart.Data("Negativo", sentimentCountFacebook[1]),
                        new PieChart.Data("Neutral", sentimentCountFacebook[2]),
                        new PieChart.Data("Positivo", sentimentCountFacebook[3]),
                        new PieChart.Data("Muy positivo", sentimentCountFacebook[4]));

        ObservableList<PieChart.Data> pieChartDataTwitter =
                FXCollections.observableArrayList(
                        new PieChart.Data("Muy negativo", sentimentCountTwitter[0]),
                        new PieChart.Data("Negativo", sentimentCountTwitter[1]),
                        new PieChart.Data("Neutral", sentimentCountTwitter[2]),
                        new PieChart.Data("Positivo", sentimentCountTwitter[3]),
                        new PieChart.Data("Muy positivo", sentimentCountTwitter[4]));

        final PieChart facebookChart = new PieChart(pieChartDataFacebook);
        facebookChart.setTitle("Facebook sentiment");
        facebookChart.setLegendVisible(false);

        final PieChart twitterChart = new PieChart(pieChartDataTwitter);
        twitterChart.setTitle("Twitter sentiment");
        twitterChart.setLegendVisible(false);



        layout.getChildren().addAll(facebookChart, twitterChart);


        Scene scene = new Scene(layout, 1048, 512);
        stage.setTitle("Sentiment");
        stage.setWidth(500);
        stage.setHeight(500);

        stage.setScene(scene);
        stage.show();

        applyCustomColorSequence(
                pieChartDataFacebook,
                "red",
                "orange",
                "yellow",
                "lightgreen",
                "green"
        );

        applyCustomColorSequence(
                pieChartDataTwitter,
                "red",
                "orange",
                "yellow",
                "lightgreen",
                "green"
        );

    }

    private int[] countSentiment(DBCursor cursor){
        int[] sentimentCount = new int[5];

        while (cursor.hasNext()){
            DBObject document = cursor.next();

            int sentiment = ((Double) document.get("sentiment")).intValue();

            switch (sentiment){
                case 0: sentimentCount[2]++;
                    break;
                case 1: sentimentCount[0]++;
                    break;
                case 2: sentimentCount[1]++;
                    break;
                case 3: sentimentCount[2]++;
                    break;
                case 4: sentimentCount[3]++;
                    break;
                case 5: sentimentCount[4]++;
                    break;
            }
        }

        return sentimentCount;
    }

    private void applyCustomColorSequence(ObservableList<PieChart.Data> pieChartData, String... pieColors) {
        int i = 0;
        for (PieChart.Data data : pieChartData) {
            data.getNode().setStyle("-fx-pie-color: " + pieColors[i % pieColors.length] + ";");
            i++;
        }
    }
}
