package edu.calvin.cs262.lab06;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Gets information on players
 * The code is based on Deitel's WeatherViewer (Chapter 17), simplified based on Murach's NewsReader (Chapter 10).
 * <p>
 * for CS 262, homework 2
 *
 * @author kvlinden
 * @version summer, 2016
 */


/**
 * Jesse Hulse, jjh35.
 *
 * 1. What does the application do for invalid cities?
 *  The code will check to see if the JSON object is null. If so, it will use a toast
 *  to notify the user of the issue and not update anything on the screen.
 * 2. What is the API key? An API key is a key used by a user of an API to confirm that
 *  they have permission to use the API. What does it do? It confirms that the user has the
 *  permissions to use the API>
 * 3. What does the full JSON response look like? It is formatted in JSON, it has a city string
 *  with and id and name. The format has a keys to arrays that contain information on the keys.
 *  So the array with the key "city" has information about the city. For example, it has an array
 *  with the key "name" which contains the string name of the city.
 * 4. What does the system do with the JSON data? The raw JSON response from the server is an JSON
 *  Object, not an array, so it must be converted to an arrayList before it can be displayed on the
 *  screen for the user. The system does this via the function convertJSONtoArrayList().
 * 5. What is the Weather class designed to do? It is designed to request weather information from
 *  the openweathermap.com API, convert the JSON response, and display the weather information to the
 *  user.
 *
 *
 */

public class MainActivity extends AppCompatActivity {

    private EditText idText;
    private Button fetchButton;

    private NumberFormat numberFormat = NumberFormat.getInstance();

    private List<Players> PlayersList = new ArrayList<>();
    private ListView itemsListView;

    /* This formater can be used as follows to format temperatures for display.
     *     numberFormat.format(SOME_DOUBLE_VALUE)
     */
    //private NumberFormat numberFormat = NumberFormat.getInstance();

    private static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        idText = (EditText) findViewById(R.id.id_value);
        fetchButton = (Button) findViewById(R.id.fetchButton);
        itemsListView = (ListView) findViewById(R.id.weatherListView);

        // See comments on this formatter above.
        //numberFormat.setMaximumFractionDigits(0);

        fetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissKeyboard(idText);
                new GetWeatherTask().execute(createURL(idText.getText().toString()));
            }
        });
    }

    /**
     * Formats a URL for the webservice specified in the string resources.
     *
     * @param id the id of the player
     * @return URL formatted for openweathermap.com
     */
    private URL createURL(String id) {
        String urlString;
        Log.i( "the id is ", id );
        try {
                if ( id.equals( "" ) )
                {
                    urlString = "http://cs262.cs.calvin.edu:8089/monopoly/players";
                }
                else
                {
                    urlString = "http://cs262.cs.calvin.edu:8089/monopoly/player/" + id;
                }

            return new URL(urlString);
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
        }

        return null;
    }

    /**
     * Deitel's method for programmatically dismissing the keyboard.
     *
     * @param view the TextView currently being edited
     */
    private void dismissKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Inner class for GETing the current weather data from openweathermap.org asynchronously
     */
    private class GetWeatherTask extends AsyncTask<URL, Void, JSONArray> {

        @Override
        protected JSONArray doInBackground(URL... params) {
            HttpURLConnection connection = null;
            StringBuilder result = new StringBuilder();
            try {
                connection = (HttpURLConnection) params[0].openConnection();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    // The following if statement will convert jsonobject format to jsonarray format
                    if ( result.toString().substring(0, 1).equals( "{" ) )
                    {
                        String jsonString = result.toString();
                        jsonString = "[" + jsonString + "]";
                        return new JSONArray( jsonString );
                    }
                    return new JSONArray(result.toString());
                } else {
                    throw new Exception();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONArray weather) {
            if (weather != null) {
                //Log.d(TAG, weather.toString());
                convertJSONtoArrayList(weather);
                MainActivity.this.updateDisplay();
            } else {
                Toast.makeText(MainActivity.this, "invalid id", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Converts the JSON weather forecast data to an arraylist suitable for a listview adapter
     *
     * @param players
     */
    private void convertJSONtoArrayList(JSONArray players) {
        PlayersList.clear(); // clear old weather data

        for ( int i = 0 ; i < players.length(); i++ )
        {
            try
            {
                JSONObject player = players.getJSONObject(i);
                PlayersList.add(new Players(
                        player.getInt( "id" ),
                        player.getString("emailaddress"),
                        player.getString("name")));
            }
            catch (JSONException e) {

                try {
                JSONObject player = players.getJSONObject(i);
                PlayersList.add(new Players(
                        player.getInt("id"),
                        player.getString("emailaddress"),
                        "no name given"));
                }
                catch(JSONException e2){
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Refresh the weather data on the forecast ListView through a simple adapter
     */
    private void updateDisplay() {
        //Log.i( "update display ", PlayersList.toString() );
        if (PlayersList == null) {
            Toast.makeText(MainActivity.this, getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
        }
        ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
        for (Players item : PlayersList) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("id", Integer.toString( item.getID() ));
            map.put("emailaddress", item.getEmail());
            map.put("name", item.getName());
            data.add(map);
        }

        int resource = R.layout.weather_item;
        String[] from = {"id", "emailaddress", "name"};
        int[] to = {R.id.numberTextView, R.id.emailTextView, R.id.nameTextView};

        SimpleAdapter adapter = new SimpleAdapter(this, data, resource, from, to);
        itemsListView.setAdapter(adapter);
    }

}
