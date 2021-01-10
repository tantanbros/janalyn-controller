package com.example.modelcontroller;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
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
import com.example.modelcontroller.helpers.FlowRateCalculator;
import com.example.modelcontroller.models.Patient;
import com.example.modelcontroller.models.Simulation;

import org.json.JSONException;
import org.json.JSONObject;

public class PatientFragment extends Fragment {
    public static final String TAG = "PatientFragment";
    private String RequestTag = "Request";

    EditText editAge, editFluid;
    Spinner spinnerGender, spinnerStatus;

    private RequestQueue queue;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_patient, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        editAge = view.findViewById(R.id.editAge);
        editFluid = view.findViewById(R.id.editFluid);
        spinnerGender = view.findViewById(R.id.spinnerGender);
        spinnerStatus = view.findViewById(R.id.spinnerStatus);

        queue = Volley.newRequestQueue(PatientFragment.this.getContext());


        view.findViewById(R.id.btnStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOptions();
            }
        });

        view.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(PatientFragment.this)
                        .navigate(R.id.action_Patient_to_Home);
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
        Patient patient = getPatientModel();

        // set simulation model given patient
        setSimulationModel(patient);

        // build the final url
        String finalUrl = buildUrl(DataHolder.getDeviceIp(), Constants.ControllerRoute, DataHolder.getSimulation());

        // send request
        sendSimulationRequest(finalUrl);
    }


    private Patient getPatientModel() {
        try {
            Patient patient = new Patient(
                    Integer.parseInt(editAge.getText().toString()),
                    spinnerGender.getSelectedItem().toString(),
                    spinnerStatus.getSelectedItem().toString(),
                    Double.parseDouble(editFluid.getText().toString())
            );

            return patient;
        }catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid number format", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void setSimulationModel(Patient patient) {
        if(patient == null) {
            return;
        }

        int flowRate = FlowRateCalculator.ComputeFlowRate(patient);
        Log.d(TAG, "Flowrate is " + flowRate);

        // Set the global data
        DataHolder.setPatient(patient);
        DataHolder.getSimulation().setFlowRate(flowRate);
        DataHolder.getSimulation().setVolume(patient.getFluidIntake());

        // Specified Age was not in range
        if(flowRate == -1) {
            Toast.makeText(PatientFragment.this.getContext(), "Age is not range", Toast.LENGTH_SHORT).show();
            return;
        }
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
                Toast.makeText(PatientFragment.this.getContext(), "There was a problem retrieving the data\nPlease recheck your options and connection", Toast.LENGTH_SHORT).show();
            }
        });
        request.setTag(RequestTag);

        // add request to queue
        queue.add(request);
    }


    private void goToSimulating() {
        NavHostFragment.findNavController(PatientFragment.this)
                .navigate(R.id.action_Patient_to_Simulating);
    }
}