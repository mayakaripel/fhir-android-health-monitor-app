package edu.monash.kmhc.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.HashMap;

import edu.monash.kmhc.R;
import edu.monash.kmhc.model.PatientModel;
import edu.monash.kmhc.model.observation.ObservationModel;
import edu.monash.kmhc.model.observation.ObservationType;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeViewHolder> {

    private HashMap<String, PatientModel> patientObservationHashMap ;
    private ArrayList<PatientModel> patients = new ArrayList<>();
    private ArrayList<String> patientIDs = new ArrayList<>();

    public HomeAdapter(HashMap<String, PatientModel> patientObservationHashMap) {
        this.patientObservationHashMap = patientObservationHashMap;
        patientObservationHashMap.forEach((patientID,patientModel) -> {
            patientIDs.add(patientID);
            patients.add(patientModel);
        });
        System.out.println(patients.toString());
        System.out.println(patientIDs.toString());
        Log.i("HomeAdapter","HomeAdapter - Constructor Called");

    }

    @NonNull
    @Override
    public HomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.i("HomeAdapter","HomeAdapter - OnCreateViewHolder Called");
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.patient_cardview,parent,false);
        HomeViewHolder viewHolder = new HomeViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull HomeViewHolder holder, int position) {
        Log.i("HomeAdapter", "HomeAdapter - onBindViewHolder Called");
        ObservationModel observationModel= patients.get(position).getObservationReading(ObservationType.CHOLESTEROL);
        String cholStat = observationModel.getValue() + " " + observationModel.getUnit();
        String date = observationModel.getDateTime();
        holder.patientName.setText(patients.get(position).getName());
        holder.cholesterolValue.setText(cholStat);
        holder.time.setText(date);

        System.out.println(observationModel.toString());


    }


    @Override
    public int getItemCount() {
        return patientObservationHashMap.size();
    }

    public class HomeViewHolder extends  RecyclerView.ViewHolder {
        public TextView patientName;
        public TextView cholesterolValue;
        public TextView time;
        public HomeViewHolder(@NonNull View itemView) {
            super(itemView);

            patientName = itemView.findViewById(R.id.cv_patientName);
            cholesterolValue = itemView.findViewById(R.id.cv_cholVal);
            time = itemView.findViewById(R.id.cv_time);

        }
    }
}