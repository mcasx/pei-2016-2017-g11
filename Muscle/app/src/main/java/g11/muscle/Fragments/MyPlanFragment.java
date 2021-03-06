package g11.muscle.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import g11.muscle.Classes.PlanExerciseItem;
import g11.muscle.Classes.TrainingsItem;
import g11.muscle.Classes.PlanExerciseAdapter;

import g11.muscle.DB.DBConnect;
import g11.muscle.ExerciseActivity;
import g11.muscle.R;
import g11.muscle.DB.VolleyProvider;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MyPlanFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MyPlanFragment# newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyPlanFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

    private static final String TAG = "MyPlanFragment";
    private static final String ERROR_MSG = "Please try to reconnect";

    private String email;

    private int countPlanExercises; // nº of my plan exercises
    private int currentExerciseCount; //used to count exercises between trainings

    // Initialization of plan_group array
    private List<TrainingsItem> plan_trainings;
    private List<PlanExerciseItem> training_data;

    // training_count
    private int[] trainingExCount;
    private int currentTrainingID;
    private PlanExerciseItem nextExercise;

    //GUI
    private RecyclerView recyclerView;
    private Spinner dropdown;
    private Button nextExerciseButton;
    private ProgressBar myPlanProgressBar;
    private RelativeLayout content;

    public MyPlanFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        plan_trainings = new ArrayList<>();
        training_data = new ArrayList<>();



        // get user email
        email = getActivity().getIntent().getStringExtra("email");
        countPlanExercises = 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Fragment View
        View fView = inflater.inflate(R.layout.fragment_my_plan, container, false);



        //GUI elements
        dropdown = (Spinner)fView.findViewById(R.id.spinner);

        nextExerciseButton = (Button)fView.findViewById(R.id.buttonNextExercise);
        nextExerciseButton.setOnClickListener(onClickListener);

        content = (RelativeLayout) fView.findViewById(R.id.myPlanContent);
        myPlanProgressBar = (ProgressBar) fView.findViewById(R.id.myPlanProgressBar);
        content.setVisibility(View.INVISIBLE);
        myPlanProgressBar.setVisibility(View.VISIBLE);

        getTrainings();

        recyclerView = (RecyclerView) fView.findViewById(R.id.PE_recyclerview);

        return fView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    // Get User Plan Trainings ( ID + Name )
    private void getTrainings(){
        String url = DBConnect.serverURL + "/get_user_plan_trainings";

        currentExerciseCount = 0;

        //Create the exercise plan_group request
        StringRequest StrHistReq = new StringRequest(Request.Method.POST,url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);

                            plan_trainings.clear(); // check the list is empty before adding items
                            try {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    String tmp_name = new JSONObject(jsonArray.getString(i)).getString("Name");
                                    int tmp_id = Integer.parseInt(new JSONObject(jsonArray.getString(i)).getString("ID"));
                                    plan_trainings.add(new TrainingsItem(tmp_id,tmp_name));
                                    myPlanProgressBar.setVisibility(View.INVISIBLE);
                                }
                            } catch (JSONException je){
                                Log.e(TAG, je.toString());
                                myPlanProgressBar.setVisibility(View.INVISIBLE);
                            }
                        }catch (JSONException e2){
                            e2.printStackTrace();
                            myPlanProgressBar.setVisibility(View.INVISIBLE);
                        }


                        trainingExCount = new int[plan_trainings.size()];

                        String[] items = new String[plan_trainings.size()];
                        for(int i = 0; i < plan_trainings.size();i++){
                            items[i] = "Day " + String.valueOf(i+1) + " - " + plan_trainings.get(i).getName();
                        }
                        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, items);
                        dropdown.setAdapter(adapter1);
                        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                                currentTrainingID = pos;
                                getTrainingExercises(plan_trainings.get(pos).getIdStr());
                            }
                            public void onNothingSelected(AdapterView<?> parent) {
                            }
                        });
                        myPlanProgressBar.setVisibility(View.INVISIBLE);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Handle error response

                        /*AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                        alertDialog.setTitle("No Internet Connection");
                        //"Please connect your device to the Internet and try again")
                        alertDialog.setMessage(ERROR_MSG);
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();*/
                        myPlanProgressBar.setVisibility(View.INVISIBLE);
                    }
                }
        ){
            // use params are specified here
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                params.put("email", email);
                return params;
            }
        };

        // Add the request to the RequestQueue
        VolleyProvider.getInstance(getActivity()).addRequest(StrHistReq);
    }

    private void getTrainingExercises(final String training) {
        String url = DBConnect.serverURL + "/get_my_training_exercises";

        //Create the exercise plan_group request
        StringRequest StrHistReq = new StringRequest(Request.Method.POST,url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            training_data.clear(); // clear data - when other training selected
                            try {
                                // My Plan Exercises count
                                countPlanExercises = Integer.parseInt(new JSONObject(jsonArray.getString(jsonArray.length()-1)).getString("Plan_Exercise_Count"));
                                if(trainingExCount[currentTrainingID] == 0)
                                    trainingExCount[currentTrainingID] = jsonArray.length() - 1;

                                currentExerciseCount = 0;
                                for(int j = 0; j < currentTrainingID;j++)
                                    currentExerciseCount += trainingExCount[j];

                                boolean firstUncheckedExercise = true;

                                for (int i = 0; i < jsonArray.length() - 1; i++) {
                                    String tmp_name = new JSONObject(jsonArray.getString(i)).getString("Exercise_name");
                                    int tmp_reps = Integer.parseInt(new JSONObject(jsonArray.getString(i)).getString("Repetitions"));
                                    int tmp_sets = Integer.parseInt(new JSONObject(jsonArray.getString(i)).getString("Sets"));
                                    String tmp_rest = new JSONObject(jsonArray.getString(i)).getString("Resting_Time");
                                    int tmp_weight = Integer.parseInt(new JSONObject(jsonArray.getString(i)).getString("Weight"));


                                    if(currentExerciseCount < countPlanExercises){ // Exercise was already completed
                                        training_data.add(new PlanExerciseItem(tmp_name,tmp_reps,tmp_sets,tmp_rest,tmp_weight,1));
                                        currentExerciseCount++;
                                    }else{
                                        if((currentExerciseCount == countPlanExercises) && firstUncheckedExercise){
                                            training_data.add(new PlanExerciseItem(tmp_name,tmp_reps,tmp_sets,tmp_rest,tmp_weight,2));
                                            firstUncheckedExercise = false;
                                            if(nextExercise == null)
                                                nextExercise = new PlanExerciseItem(tmp_name,tmp_reps,tmp_sets,tmp_rest,tmp_weight,2);
                                        }
                                        else
                                            training_data.add(new PlanExerciseItem(tmp_name,tmp_reps,tmp_sets,tmp_rest,tmp_weight,0));
                                    }
                                }
                            } catch (JSONException je){
                                Log.e(TAG, je.toString());
                                myPlanProgressBar.setVisibility(View.INVISIBLE);

                            }
                        }catch (JSONException e2){
                            e2.printStackTrace();
                            myPlanProgressBar.setVisibility(View.INVISIBLE);

                        }

                        PlanExerciseAdapter adapter = new PlanExerciseAdapter(training_data);
                        adapter.mOnClickListener = new MyOnClickListener();

                        recyclerView.setAdapter(adapter);
                        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

                        myPlanProgressBar.setVisibility(View.INVISIBLE);
                        content.setVisibility(View.VISIBLE);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Handle error response
                        /*AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                        alertDialog.setTitle("No Internet Connection");
                        //"Please connect your device to the Internet and try again")
                        alertDialog.setMessage(ERROR_MSG);
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();*/
                        Log.e("HELP","10");
                        myPlanProgressBar.setVisibility(View.INVISIBLE);
                    }
                }
        ){
            // use params are specified here
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                params.put("training",training);
                params.put("User_email",email);
                return params;
            }
        };

        // Add the request to the RequestQueue
        VolleyProvider.getInstance(getActivity()).addRequest(StrHistReq);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            if(nextExercise != null) {
                String email = getContext().getSharedPreferences("UserData", 0).getString("email", null);
                Intent intent = new Intent(getActivity(), ExerciseActivity.class);
                intent.putExtra("email", email);
                intent.putExtra("exercise_name", nextExercise.getExercise_name());
                intent.putExtra("exercise_reps", nextExercise.getExercise_reps());
                intent.putExtra("exercise_sets", nextExercise.getExercise_sets());
                intent.putExtra("exercise_rest", nextExercise.getExercise_rest());
                intent.putExtra("exercise_weight", nextExercise.getExercise_weight());
                startActivity(intent);
            }
        }
    };

    private class MyOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View view) {
            int itemPosition = recyclerView.getChildLayoutPosition(view);
            PlanExerciseItem item = training_data.get(itemPosition);

            for(int j = 0; j < currentTrainingID;j++)
                itemPosition += trainingExCount[j];

            if(itemPosition == countPlanExercises) { // Next Exercise
                String email = getContext().getSharedPreferences("UserData", 0).getString("email", null);
                Intent intent = new Intent(getActivity(), ExerciseActivity.class);
                intent.putExtra("email", email);
                intent.putExtra("exercise_name", item.getExercise_name());
                intent.putExtra("exercise_reps",item.getExercise_reps());
                intent.putExtra("exercise_sets",item.getExercise_sets());
                intent.putExtra("exercise_rest",item.getExercise_rest());
                intent.putExtra("exercise_weight",item.getExercise_weight());
                startActivity(intent);
            }
        }
    }
}
