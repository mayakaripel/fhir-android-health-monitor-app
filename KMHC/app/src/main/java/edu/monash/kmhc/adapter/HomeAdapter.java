package edu.monash.kmhc.adapter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.Collections;
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
    private OnPatientClickListener onPatientClickListener;
    private float averageCholesterolValue;
    private int x;
    private int y;
    private final int HIGH_SYSTOLIC_READING = 140;

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

        for( PatientModel p: getUniquePatients()){
            total += Float.parseFloat(p.getObservationReading(ObservationType.CHOLESTEROL).getValue());
        }
        averageCholesterolValue = total/getUniquePatients().size();
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
            if (Float.parseFloat(observationModel.getValue()) > averageCholesterolValue){
                homeViewHolder.cholesterolValue.setChipBackgroundColorResource(R.color.colorRed);
                homeViewHolder.patientName.setTextColor(R.color.colorRed);
            }
            homeViewHolder.cholesterolValue.setText(cholStat);
            homeViewHolder.cholesterolTime.setText(observationModel.getDateTime());
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
                if (Float.parseFloat(observationModel.getSystolic()) > HIGH_SYSTOLIC_READING) {
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
        Chip cholesterolTime;
        Chip bpTime;
        Chip systolicBP;
        Chip diastolicBP;
        TextView titleCholesterol;
        TextView titleBP;
        Chip showLatestSystolic;
        Chip showSystolicGraph;
        TextView latestSystolicReadings;
        LineChart latestSystolicGraph;
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
            cholesterolTime = itemView.findViewById(R.id.cv_chol_time);
            bpTime = itemView.findViewById(R.id.cv_bp_time);
            systolicBP = itemView.findViewById(R.id.cv_systolicbp);
            diastolicBP = itemView.findViewById(R.id.cv_diastolicbp);
            titleCholesterol = itemView.findViewById(R.id.cv_title_cholVal); // title for cholesterol value
            titleBP = itemView.findViewById(R.id.cv_title_bpVal); // title for bp values
            showLatestSystolic = itemView.findViewById(R.id.cv_n_latest_systolic); // selection chip to show latest 5 systolic readings
            showSystolicGraph = itemView.findViewById(R.id.cv_systolic_graph); // selection chip to show systolic graph
            latestSystolicReadings = itemView.findViewById(R.id.txt_show_n_latest_systolic); // text view with 5 latest systolic readings
            latestSystolicGraph = itemView.findViewById(R.id.barchart); // line graph to show systolic graph
            itemView.setOnClickListener(this);
            showLatestSystolic.setOnClickListener(view -> showLatestSystolicReadings(getAdapterPosition()));
            showSystolicGraph.setOnClickListener(view -> showLatestSystolicGraph(getAdapterPosition()));
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
            cholesterolTime.setVisibility(View.GONE);
            systolicBP.setVisibility(View.GONE);
            diastolicBP.setVisibility(View.GONE);
            bpTime.setVisibility(View.GONE);
            showLatestSystolic.setVisibility(View.GONE);
            latestSystolicReadings.setVisibility(View.GONE);
            showSystolicGraph.setVisibility(View.GONE);
            titleCholesterol.setVisibility(View.GONE);
            titleBP.setVisibility(View.GONE);
            latestSystolicGraph.setVisibility(View.GONE);
        }

        /**
         * This method is called when the practitioner chooses to monitor cholesterol values.
         * Cholesterol value of the patient will be displayed
         */
        private void showCholesterolViews(){
            titleCholesterol.setVisibility(View.VISIBLE);
            cholesterolValue.setVisibility(View.VISIBLE);
            cholesterolTime.setVisibility(View.VISIBLE);
        }

        /**
         * This method is called when the practitioner chooses to monitor bp values. Bp values of the
         * patient will be displayed
         */
        private void showBPView(){
            titleBP.setVisibility(View.VISIBLE);
            systolicBP.setVisibility(View.VISIBLE);
            diastolicBP.setVisibility(View.VISIBLE);
            bpTime.setVisibility(View.VISIBLE);
        }

        /**
         * This method is called when the patient has high systolic pressure.
         * The practitioner is given the option to monitor the patient's past readings and also
         * view the systolic reading line graph.
         */
        private void showLatestSystolicChips() {
            showLatestSystolic.setVisibility(View.VISIBLE);
            showSystolicGraph.setVisibility(View.VISIBLE);
        }

        /**
         * This method is used for the chip when pressed to show the text view with the latest 5
         * systolic readings.
         * @param position position of adapter (patient)
         */
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

        /**
         * This method is used for the chip when pressed to show the graph of systolic readings
         * @param position position of adapter (patient)
         */
        private void showLatestSystolicGraph(int position) {
            if (showSystolicGraph.isChecked()) {
                ArrayList<Entry> entries = new ArrayList<Entry>();
                LineChart systolicGraph = itemView.findViewById(R.id.barchart);

                for (int i = 0; i < getUniquePatients().get(position).getLatestReadings(ObservationType.BLOOD_PRESSURE).size(); i++) {
                    entries.add(new Entry(Float.parseFloat(getUniquePatients().get(position).getLatestReadings(ObservationType.BLOOD_PRESSURE).get(i).getSystolic()), i));
                }

                LineDataSet dataSet = new LineDataSet(entries, "");
                dataSet.setCircleColor(Color.BLUE);
                LineData lineData = new LineData();
                lineData.addDataSet(dataSet);
                systolicGraph.setHorizontalScrollBarEnabled(true);
                systolicGraph.setData(lineData);
                systolicGraph.invalidate();
                latestSystolicGraph.setVisibility(View.VISIBLE);
            }
            else {
                latestSystolicGraph.setVisibility(View.GONE);
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
