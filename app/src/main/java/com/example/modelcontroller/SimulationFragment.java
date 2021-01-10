package com.example.modelcontroller;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
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
import com.example.modelcontroller.models.Simulation;

import org.json.JSONException;
import org.json.JSONObject;

import static java.lang.Double.parseDouble;

public class SimulationFragment extends Fragment {
    private String TAG = "SimulationFragment";
    private String RequestTag = "Request";

    private EditText editVolume, editFlowRate;
    private Spinner spinnerConcentration;

    private RequestQueue queue;

    private static final double MaxVolume = 320;
    private static final double MinFlowrate = 5;
    private static final double MaxFlowrate = 20;



    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_simulation, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        editVolume = view.findViewById(R.id.editVolume);
        editFlowRate = view.findViewById(R.id.editFlowRate);
        spinnerConcentration = view.findViewById(R.id.spinnerConcentration);

        // on text change
        editVolume.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    double volume = Double.parseDouble(charSequence.toString());
                    if(volume > MaxVolume) {
                        Toast.makeText(getContext(), String.format("Maximum Volume is %s", Double.toString(MaxVolume)), Toast.LENGTH_SHORT).show();
                        editVolume.setText(Double.toString(MaxVolume));
                    }else if (volume < 0) {
                        Toast.makeText(getContext(), "Volume can't be negative", Toast.LENGTH_SHORT).show();
                        editVolume.setText("0");
                    }
                }
                catch (Exception e){
                    Log.d(TAG, e.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        editFlowRate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    double flowRate = Double.parseDouble(charSequence.toString());
                    if(flowRate > MaxFlowrate) {
                        Toast.makeText(getContext(), String.format("Maximum Flow Rate is %s", Double.toString(MaxFlowrate)), Toast.LENGTH_SHORT).show();
                        editVolume.setText(Double.toString(MaxFlowrate));
                    }else if(flowRate < MinFlowrate){
                        Toast.makeText(getContext(), String.format("Minimum Flow Rate is %s", Double.toString(MinFlowrate)), Toast.LENGTH_SHORT).show();
                        editVolume.setText(Double.toString(MinFlowrate));
                    }
                }
                catch (Exception e){
                    Log.d(TAG, e.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        queue = Volley.newRequestQueue(SimulationFragment.this.getContext());

        view.findViewById(R.id.btnStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            showOptions();
            }
        });

        view.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(SimulationFragment.this)
                        .navigate(R.id.action_Simulation_to_Home);
            }
        });
    }

    private void showOptions() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_options, null);
        final EditText editUrl = view.findViewById(R.id.editUrl);

        // set the initial values
        editUrl.setText(DataHolder.getDeviceIp());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Device Options")
                .setView(view)
                .setPositiveButton("Start", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // get the device ip
                        String deviceIp = editUrl.getText().toString();
                        if(deviceIp.isEmpty()) {
                            Toast.makeText(getContext(), "Please enter the address of the device", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // update the options
                        DataHolder.setDeviceIp(deviceIp);
                        Log.d(TAG, "Device Options Saved");

                        // pag nag kuha ung ip ng device, start na
                        startSimulation();

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d(TAG, "Device Options Cancelled");
                        dialog.cancel();
                    }
                });
        builder.create().show();
    }

    private void startSimulation() {
        // Get model from the input values
        Simulation model = getSimulationModel();

        // build the final url
        String finalUrl = buildUrl(DataHolder.getDeviceIp(), Constants.ControllerRoute, model);

        // send request
        sendSimulationRequest(finalUrl);
    }

    private Simulation getSimulationModel() {
        Simulation model = new Simulation(
                editVolume.getText().toString(),
                editFlowRate.getText().toString(),
                spinnerConcentration.getSelectedItem().toString()
        );

        return model;
    }

    private String buildUrl(String ip, String route, Simulation model) {
        String queryString = String.format("?volume=%s&flowrate=%s", model.getVolume(), model.getFlowRate());
        String result = ip + route + queryString;
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

                    boolean simulationStarted = response.getBoolean(Constants.SimulationStarted);
                    if(simulationStarted) {
                        goToSimulating();
                        Log.d(TAG, "Simulation Started");
                    } else {
                        Toast.makeText(getContext(), "Couldn't start simulation, try again", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(SimulationFragment.this.getContext(), "There was a problem retrieving the data\nPlease recheck your options and connection", Toast.LENGTH_SHORT).show();
            }
        });
        request.setTag(RequestTag);

        // add request to queue
        queue.add(request);
    }

    private void goToSimulating() {
        NavHostFragment.findNavController(SimulationFragment.this)
                .navigate(R.id.action_Simulation_to_Simulating);
    }
}
