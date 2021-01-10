package com.example.modelcontroller;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.modelcontroller.helpers.Constants;
import com.example.modelcontroller.helpers.DataHolder;
import com.example.modelcontroller.helpers.SimulationStatus;
import com.example.modelcontroller.models.Simulation;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class SimulatingFragment extends Fragment {
    private final String TAG = "SimulatingFragment";
    private String RequestTag = "Request";

    private RequestQueue queue;
    private Timer dataGetter;

    private boolean finished = false;


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_simulating, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        queue = Volley.newRequestQueue(SimulatingFragment.this.getContext());

        Log.d(TAG, "Simulating...");
        dataGetter = new Timer();
        dataGetter.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // build the final url
                String finalUrl = buildUrl(DataHolder.getDeviceIp(), Constants.StatusRoute);

                // send request
                sendSimulationRequest(finalUrl);
            }

            @Override
            public boolean cancel() {
                queue.cancelAll(RequestTag);
                Log.d(TAG, "Requests cancelled");
                return super.cancel();
            }
        }, 500, 1000);
    }

    private String buildUrl(String ip, String route) {
        String result = ip + route;
        Log.d(TAG, "Build Url: " + result);
        return result;
    }

    private void sendSimulationRequest(String url) {
        // build the request
        Log.d(TAG, "GET - " + url);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.d(TAG, "onResponse - " + response.toString());
                    String status = response.getString(Constants.SimulationStatus);
                    if(status.equals(SimulationStatus.Finished)) {
                        goToSimulated();
                        Log.d(TAG, "Simulation Finished");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Invalid device response format");
                    Toast.makeText(getContext(), "Invalid device response format", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "There was a problem retrieving the data", error);
                Toast.makeText(SimulatingFragment.this.getContext(), "There was a problem retrieving the data\nPlease recheck your options and connection", Toast.LENGTH_SHORT).show();
            }
        });
        request.setTag(RequestTag);

        // add request to queue
        queue.add(request);
    }

    private void goToSimulated() {
        if(finished) {
            return;
        }
        finished = true;
        dataGetter.cancel();
        NavHostFragment.findNavController(SimulatingFragment.this)
            .navigate(R.id.action_Simulating_to_Simulated);
    }

}