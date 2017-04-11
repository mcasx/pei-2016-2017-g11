package g11.muscle;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.SyncStateContract;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import g11.muscle.PickExerciseActivity;


public class RegisterActivity extends AppCompatActivity {
    //TODO: shared preferences;
    private String email;
    private String password;
    private String repeatedPassword;

    ProgressBar progressBar;
    TextInputLayout email_layout;
    TextInputLayout password_input_layout;
    TextInputLayout password_confirmation_layout;
    Button signUpButton;

    @Override
    public void onBackPressed(){
        Intent back = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(back);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.i("TG","on create");
        setContentView(R.layout.activity_register);

        // needed to set visibility
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        email_layout = (TextInputLayout) findViewById(R.id.email_input_layout);
        password_input_layout = (TextInputLayout) findViewById(R.id.password_input_layout);
        password_confirmation_layout = (TextInputLayout) findViewById(R.id.password_confirmation_layout);
        signUpButton = (Button) findViewById(R.id.sign_up_button);

        // all this code to submit when done button is pressed fcn java man
        final TextInputEditText password_confirmation = (TextInputEditText)findViewById(R.id.password_confirmation);
        password_confirmation.setOnEditorActionListener(
                new TextInputEditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                                actionId == EditorInfo.IME_ACTION_DONE ||
                                event.getAction() == KeyEvent.ACTION_DOWN &&
                                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

                            InputMethodManager inputManager = (InputMethodManager)
                                    getSystemService(Context.INPUT_METHOD_SERVICE);

                            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                                    InputMethodManager.HIDE_NOT_ALWAYS);

                            onClickNext(v.getRootView());
                            password_confirmation.setImeOptions(EditorInfo.IME_ACTION_DONE);


                            return true;
                        }
                        return false;
                    }
                });
    }



    public void onClickNext(View view){

        TextInputEditText email_input, password_input, repPass_input;

        email  = ((TextInputEditText) findViewById(R.id.email_input)).getText().toString().trim();

        password = ((TextInputEditText) findViewById((R.id.password_input))).getText().toString();

        repeatedPassword = ((TextInputEditText) findViewById((R.id.password_confirmation))).getText().toString();

        set_progressBar_visibility(View.VISIBLE);

        if(!validFields())
            return;

        String baseUrl = "https://138.68.158.127";

        String addUserUrl = "/add_user";
        //Create the list items through a request

        StringRequest stringRequest = new StringRequest(Request.Method.POST, baseUrl + addUserUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response){

                        if(response.equals("User added")) {

                            // Intent intent = new Intent(RegisterActivity.this, FormActivity.class);
                            // Later in development (like tomorrow) it will redirect to a page where user specefies more parameters
                            // For now it takes the user to the PickExerciseActivity
                            Intent intent = new Intent(RegisterActivity.this, PickExerciseActivity.class);
                            intent.putExtra("email", email);
                            startActivity(intent);
                        }
                        else{
                            email_layout.setError("Email already in use");
                            set_progressBar_visibility(View.GONE);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        AlertDialog alertDialog = new AlertDialog.Builder(RegisterActivity.this).create();
                        alertDialog.setTitle("No Internet Connection");
                        // Please connect your device to the Internet and try again
                        alertDialog.setMessage(error.toString());
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();
                        set_progressBar_visibility(View.GONE);
                    }
                }
        ){
            // use params are specified here
            // DoB, height, gender and weight are specefied later, for now they have default values
            // effin not nulls
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                params.put("email", email);
                params.put("name", email);
                params.put("password", password);
                // in this step we put name = email, beacause fkn not nulls

                params.put("date_of_birth", getDeafaultAgeDoB());
                // we literally assume the gender
                params.put("gender", "1");
                // i assume it's in centimeters, could be height and weight were mixed tho
                params.put("height", "170");
                params.put("weight", "70.0");
                return params;
            }
        };

        VolleyProvider.getInstance(this).addRequest(stringRequest);

    }

    private void set_progressBar_visibility(int view){

        if(View.GONE == view){
            progressBar.setVisibility(View.GONE);
            email_layout.setVisibility(View.VISIBLE);
            password_input_layout.setVisibility(View.VISIBLE);
            password_confirmation_layout.setVisibility(View.VISIBLE);
            signUpButton.setVisibility(View.VISIBLE);
        }

        else{

            progressBar.setVisibility(View.VISIBLE);
            email_layout.setVisibility(View.GONE);
            password_input_layout.setVisibility(View.GONE);
            password_confirmation_layout.setVisibility(View.GONE);
            signUpButton.setVisibility(View.GONE);

        }
    }

    private boolean validFields(){

        // reset fields
        email_layout.setError(null);
        password_input_layout.setError(null);
        password_confirmation_layout.setError(null);

        if(email.equals("")){
            email_layout.setError("Insert your account's email address");
            set_progressBar_visibility(View.GONE);
             return false;
        }

        if(!isValidEmail(email)){
            email_layout.setError("Invalid Email");
            set_progressBar_visibility(View.GONE);
            return false;
        }

        if(password.length() < 6){
            password_input_layout.setError("Password Invalid");
            set_progressBar_visibility(View.GONE);
            return false;
        }

        if(password.length() < 6){
            password_input_layout.setError("Password Invalid");
            set_progressBar_visibility(View.GONE);
            return false;
        }

        if(repeatedPassword.equals("")){
            password_confirmation_layout.setError("Confirm your password");
            set_progressBar_visibility(View.GONE);
            return false;
        }

        if(!repeatedPassword.equals(password)) {
            password_confirmation_layout.setError("Password doesn't match");
            set_progressBar_visibility(View.GONE);
            return false;
        }
        return true;
    }

    private final static boolean isValidEmail(String target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    public String getDeafaultAgeDoB(){
        String pattern = "dd-MM-yyyy";
        Calendar cal = Calendar.getInstance();
        Date today = cal.getTime();
        cal.add(Calendar.YEAR, -25); // to get previous year add -1
        Date age = cal.getTime();
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        String mysqlDateString = formatter.format(age);
        return mysqlDateString;
    }




}