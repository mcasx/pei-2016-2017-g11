package g11.muscle;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

//Used to conver url strings to valid strings
import java.net.URL;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class ExerciseActivity extends AppCompatActivity {

    private static final String TAG = "ExerciseActivity";

    //GUI
    private TextView exerciseView;
    private TextView kindView;
    private ImageView imgView;

    private TextView descriptionView;

    //Exercise name
    private String exercise;
    //User email
    private String email;


    // Defines several constants used when transmitting messages between the
    // service and the UI.
    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);

        // Information from previous activity
        final Intent intent = getIntent();
        exercise = intent.getStringExtra("exercise_name");
        email = intent.getStringExtra("email");


        //GUI elements
        exerciseView  = (TextView)findViewById(R.id.exercise);
        kindView  = (TextView)findViewById(R.id.kind);
        imgView = (ImageView)findViewById(R.id.image);
        descriptionView = (TextView)findViewById(R.id.description);

        // Exercise Name
        exerciseView.setText(exercise);

        // get exercise information
        String urlEx = "http://138.68.158.127/get_exercise";

        JsonObjectRequest Ex_Req = new JsonObjectRequest(
            JsonObjectRequest.Method.POST, urlEx, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                try{
                    Log.e(TAG,response.toString());
                    descriptionView.setText(response.getString("Description"));
                    kindView.setText(response.getString("Kind"));

                    //Image name must be in lowercase
                    String DrawableName = response.getString("Image").toLowerCase();
                    //Can't have extension
                    if(DrawableName.contains("."))
                        DrawableName = DrawableName.split("\\.")[0];
                    int resID = getResources().getIdentifier(DrawableName,"drawable",getPackageName());
                    imgView.setImageResource(resID);
                } catch (JSONException je){
                    Log.e(TAG,"GET EXERCISE DATA EXCEPTION");
                    Log.e(TAG, je.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Error Response
                System.out.println(error.toString());
            }
        }
        ) {
            // use params are specified here
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("exercise_name", exercise);
                return params;
            }
        };

        //Queue the request
        VolleyProvider.getInstance(ExerciseActivity.this).addRequest(Ex_Req);
    }

    @Override
    protected void onStart(){
        super.onStart();

        getLastExerciseInfo();
    }

    @Override
    public void onBackPressed() {
        Intent back = new Intent(ExerciseActivity.this, HomeActivity.class);
        back.putExtra("email",email);

        startActivity(back);
    }

    //Get last exercise info
    private void getLastExerciseInfo(){
        String urlLast = "https://138.68.158.127/get_last_exercise_of_user";

        // get last exercise information
        JsonObjectRequest Last_Ex_Req = new JsonObjectRequest(
                JsonObjectRequest.Method.POST, urlLast, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                try{
                    //last_weightView.setText(response.getString("Weight"));
                    //last_repsView.setText(response.getString("Repetitions"));
                    //last_intensityView.setText(response.getString("Intensity"));
                    Log.e(TAG,response.toString());
                } catch (Exception e){
                    Log.e(TAG, e.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // User doesn't have anything in it's exercise history
                Log.i(TAG,"User never did this exercise");
            }
        }
        ){
            // use params are specified here
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_email",email);
                params.put("exercise_name", exercise);
                return params;
            }
        };

        //Queue the request
        VolleyProvider.getInstance(ExerciseActivity.this).addRequest(Last_Ex_Req);
    }

    public void onClickStartButton(View view){
        Intent intent = new Intent(ExerciseActivity.this, FeedBackActivity.class);
        intent.putExtra("email", email);
        intent.putExtra("exercise_name", exercise);
        startActivity(intent);
    }
}
