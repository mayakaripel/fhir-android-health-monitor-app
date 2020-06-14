package edu.monash.kmhc.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.HashMap;

import edu.monash.kmhc.R;
import edu.monash.kmhc.model.PatientModel;
import edu.monash.kmhc.model.observation.BloodPressureObservationModel;
import edu.monash.kmhc.model.observation.ObservationModel;
import edu.monash.kmhc.model.observation.ObservationType;

/**
 * HomeAdapter class extends from BaseAdapter
 * HomeAdapter is the class that is responsible to create recycler view
 * that displays Patient's cholesterol value.
 */
public class HomeAdapter extends BaseAdapter<HomeAdapter.HomeViewHolder> {
    private ArrayList<String> patientIDs = new ArrayList<>();
    private OnPatientClickListener onPatientClickListener;
    private float averageCholValue;
    private int x;
    private int y;
    private final int HIGH_BP = 140;

    /**
     * The Home Adapter constructor, this initialises the adapter that will be used to update the home fragment UI.
     * @param patientObservationHashMap A hash map that contains all the Patient Models that the practitioner is monitoring
     * @param onPatientClickListener the class that is listening to individual patient card clicks
     */
    public HomeAdapter(HashMap<String, PatientModel> patientObservationHashMap, OnPatientClickListener onPatientClickListener,int x,int y) {
        super(patientObservationHashMap);
        this.onPatientClickListener = onPatientClickListener;

        ArrayList<PatientModel> patients = new ArrayList<>();

        getPatientsHashMap().forEach((patientID,patientModel) -> {
            patientIDs.add(patientID);
            patients.add(patientModel);
        });
        setUniquePatients(patients);

        calculateAverage();
        this.x = x;
        this.y = y;
    }

    /**
     * This method calculate the average cholesterol value
     */
    private void calculateAverage() {
        float total = 0;

        for( PatientModel p : getUniquePatients()){
            total += Float.parseFloat(p.getObservationReading(ObservationType.CHOLESTEROL).getValue());
        }
        averageCholValue = total/getUniquePatients().size();
    }


    /**
     * This method overrides its superclass's onCreateViewHolder method
     * It is responsible for creating new card view holders that will be used to hold
     * and display the data.
     * @return A HomeViewHolder instance that will be used to display the data
     */
    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.patient_cardview, parent, false);
        return new HomeAdapter.HomeViewHolder(v, onPatientClickListener);
    }

    /**
     * This method overrides its superclass's onBindViewHolder method
     * This method is used to update the contents of the HomeViewHolder to reflect the patient
     * at the given position.
     *
     * This method also highlights the patient if his/her cholesterol value is higher
     * than the average cholesterol value.
     * @param holder the homeViewHolder that will hold patient[position]'s data
     * @param position the current UI adapter position
     */
    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        HomeViewHolder homeViewHolder = (HomeViewHolder) holder;
        boolean cholMonitored = getUniquePatients().get(position).isObservationMonitored(ObservationType.CHOLESTEROL) ;
        boolean bpMonitored = getUniquePatients().get(position).isObservationMonitored(ObservationType.BLOOD_PRESSURE);

        // set patients name
        homeViewHolder.patientName.setText(getUniquePatients().get(position).getName());

        // hide all view holders
        homeViewHolder.hideAllTextView();

        // if the current patient cholesterol value is being  monitored
        if (cholMonitored) {
            homeViewHolder.showCholesterolViews();
            ObservationModel observationModel = getObservationModel(ObservationType.CHOLESTEROL,position);
            String cholStat = observationModel.getValue() + " " + observationModel.getUnit();

            //if current patients cholesterol value is greater than average
            //highlight cholesterol value in red
            if (Float.parseFloat(observationModel.getValue()) > averageCholValue){
                homeViewHolder.cholesterolValue.setChipBackgroundColorResource(R.color.colorRed);
                homeViewHolder.patientName.setTextColor(R.color.colorRed);
            }
            homeViewHolder.cholesterolValue.setText(cholStat);
            homeViewHolder.cholTime.setText(observationModel.getDateTime());
        }
        if (bpMonitored) {
            BloodPressureObservationModel observationModel = (BloodPressureObservationModel) getObservationModel(ObservationType.BLOOD_PRESSURE,position);
            if ( observationModel != null) {
                homeViewHolder.showBPView();
                String systolicBP = observationModel.getSystolic() + " " + observationModel.getUnit();
                String diastolicBP = observationModel.getDiastolic() + " " + observationModel.getUnit();

                //if current patients BP value is greater than x/y
                if (x > 0 && Float.parseFloat(observationModel.getSystolic()) > x) {
                    homeViewHolder.systolicBP.setChipBackgroundColorResource(R.color.colorBlue);
                    homeViewHolder.patientName.setTextColor(R.color.colorRed);
                }
                if (y > 0 && Float.parseFloat(observationModel.getDiastolic()) > y) {
                    homeViewHolder.diastolicBP.setChipBackgroundColorResource(R.color.colorBlue);
                    homeViewHolder.patientName.setTextColor(R.color.colorRed);
                }
                if (Float.parseFloat(observationModel.getSystolic()) > HIGH_BP) {
                    homeViewHolder.showLatestSystolicChips();
                }

                homeViewHolder.systolicBP.setText(systolicBP);
                homeViewHolder.diastolicBP.setText(diastolicBP);
                homeViewHolder.bpTime.setText(observationModel.getDateTime());
            }
        }
    }

    private ObservationModel getObservationModel(ObservationType type, int position){
        return getUniquePatients().get(position).getObservationReading(type);
    }

    /**
     * This method overrides its superclass's method,
     * It gets the total number of patients that Health Practitioner is monitoring.
     * @return total number of patients
     */
    @Override
    public int getItemCount() {
        return getUniquePatients().size();
    }

    /**
     * The HomeViewHolder class are objects that holds the reference to the individual
     * card views that is reused to display different sets of data in the recycler view.
     *
     * This class implements View.OnClickListener interface.
     */
    public class HomeViewHolder extends BaseViewHolder {
        TextView patientName;
        Chip cholesterolValue;
        Chip cholTime;
        Chip bpTime;
        Chip systolicBP;
        Chip diastolicBP;
        Chip showLatestSystolic;
        Chip showSystolicGraph;
        TextView latestSystolicReadings;
        OnPatientClickListener onPatientClickListener;

        /**
         * HomeViewHolder Constructor
         * This initialises the HomeViewHolder object.
         */
        HomeViewHolder(@NonNull View itemView, OnPatientClickListener onPatientClickListener) {
            super(itemView);
            this.onPatientClickListener = onPatientClickListener;
            patientName = itemView.findViewById(R.id.cv_patientName);
            cholesterolValue = itemView.findViewById(R.id.cv_cholVal);
            cholTime = itemView.findViewById(R.id.cv_chol_time);
            bpTime = itemView.findViewById(R.id.cv_bp_time);
            systolicBP = itemView.findViewById(R.id.cv_systolicbp);
            diastolicBP = itemView.findViewById(R.id.cv_diastolicbp);
            showLatestSystolic = itemView.findViewById(R.id.cv_n_latest_systolic);
            showSystolicGraph = itemView.findViewById(R.id.cv_systolic_graph);
            latestSystolicReadings = itemView.findViewById(R.id.txt_show_n_latest_systolic);
            itemView.setOnClickListener(this);
            showLatestSystolic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showLatestSystolicReadings(getAdapterPosition());
                }
            });
        }

        /**
         * This method is called when user clicks on a patient's card-view.
         * It notifies its onClickListener (Home Fragment) to perform a fragment switch to display
         *  individual patient details.
         */
        @Override
        public void onClick(View v) {
            onPatientClickListener.onPatientClick(getAdapterPosition(), getUniquePatients().get(getAdapterPosition()));
        }

        private void hideAllTextView(){
            cholesterolValue.setVisibility(View.GONE);
            cholTime.setVisibility(View.GONE);
            systolicBP.setVisibility(View.GONE);
            diastolicBP.setVisibility(View.GONE);
            bpTime.setVisibility(View.GONE);
            showLatestSystolic.setVisibility(View.GONE);
            showSystolicGraph.setVisibility(View.GONE);
            latestSystolicReadings.setVisibility(View.GONE);
        }

        private void showCholesterolViews(){
            cholesterolValue.setVisibility(View.VISIBLE);
            cholTime.setVisibility(View.VISIBLE);
        }

        private void showBPView() {
            systolicBP.setVisibility(View.VISIBLE);
            diastolicBP.setVisibility(View.VISIBLE);
            bpTime.setVisibility(View.VISIBLE);
        }

        private void showLatestSystolicChips() {
            showLatestSystolic.setVisibility(View.VISIBLE);
            showSystolicGraph.setVisibility(View.VISIBLE);
        }

        private void showLatestSystolicReadings(int position) {
            if (showLatestSystolic.isChecked()) {
                StringBuilder latestReadings = new StringBuilder();
                for (BloodPressureObservationModel reading: getUniquePatients().get(position).getLatestReadings(ObservationType.BLOOD_PRESSURE)) {
                    latestReadings.append(reading.getSystolic()).append(" ").append(reading.getDateTime()).append("\t");
                }
                latestSystolicReadings.setText(latestReadings);
                latestSystolicReadings.setVisibility(View.VISIBLE);
            }
            else {
                latestSystolicReadings.setVisibility(View.GONE);
            }
        }
    }
    /**
     * Class the uses this interface must implement their own onPatientClick method.
     * onPatientClick is handle the events that should happen when a patient's card-view is clicked.
     */
    public interface OnPatientClickListener {
        void onPatientClick(int position, PatientModel patient);
    }

}
