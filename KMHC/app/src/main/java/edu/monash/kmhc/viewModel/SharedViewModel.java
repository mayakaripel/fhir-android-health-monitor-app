package edu.monash.kmhc.viewModel;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.HashMap;

import edu.monash.kmhc.model.PatientModel;
import edu.monash.kmhc.model.observation.ObservationModel;
import edu.monash.kmhc.model.observation.ObservationType;
import edu.monash.kmhc.service.repository.ObservationRepositoryFactory;
import edu.monash.kmhc.service.repository.PatientRepository;

/**
 * This class is responsible for providing data that is displayed in the HomeFragment.
 * The class also acts as a Subject in the Observer pattern whereby it updates the patientObservations
 * LiveData by polling the server every N seconds with RxJava. It implements the Poll class and
 * provides its own implementation for polling the server.
 */
public class SharedViewModel extends ViewModel implements Poll {

    private PatientRepository patientRepository;
    private ObservationRepositoryFactory observationRepositoryFactory;
    private MutableLiveData<HashMap<String, PatientModel>> patientObservations = new MutableLiveData<>();
    private MutableLiveData<String> currentSelected = new MutableLiveData<>() ;
    private int frequency;

    public SharedViewModel() {
        //TODO: Change practitioner
        patientRepository = new PatientRepository("3656083");
        observationRepositoryFactory = new ObservationRepositoryFactory();

        frequency = 20000; // default
        polling();
    }
    public LiveData<String> getCurrentSelected() {
        return currentSelected;
    }

    public void updateCurrentSelected(String currentSelected) {
        this.currentSelected.setValue(currentSelected);
        frequency = Integer.parseInt(currentSelected.replace(" seconds","")) * 1000;
        //debug purpose
        System.out.println(frequency);
        System.out.println(currentSelected);
    }

    /**
     * Store patients and their observations in LiveData so that UI will be notified when there are changes.
     * @return LiveData HashMap of patient and their observations
     */
    public LiveData<HashMap<String, PatientModel>> getAllPatientObservations() {
        return patientObservations;
    }

    /**
     * Returns all patients monitored by the practitioner
     * @return All patients monitored by the practitioner
     */
    private ArrayList<PatientModel> getAllPatients() {
        return patientRepository.getAllPatients();
    }

    /**
     * Returns the observation reading based on the observation type.
     *
     * This is extensible as different
     * observations in the future only need to specify the type to get the observation reading.
     * @param id patient id
     * @param observationType type of observation. ie. cholesterol
     * @return observation reading for the patient
     */
    private ObservationModel getObservation(String id, ObservationType observationType) {
        return observationRepositoryFactory.getObservationModel(id, observationType);
    }

    /**
     * This method is responsible for polling the server and updating the observers when the data
     * is updated.
     */
    public void polling() {
        // run asynchronous tasks on background thread to prevent network on main exception
        HandlerThread backgroundThread = new HandlerThread("Background Thread");
        backgroundThread.start();
        Handler timer = new Handler(backgroundThread.getLooper());

        timer.post(new Runnable() {
            @Override
            public void run() {
                HashMap < String, PatientModel > poHashMap = new HashMap<>();
                // loop through all patients
                for (PatientModel patient : getAllPatients()) {
                    // set new cholesterol observation reading
                    patient.setObservation(ObservationType.CHOLESTEROL,
                            getObservation(patient.getPatientID(), ObservationType.CHOLESTEROL));
                    poHashMap.put(patient.getPatientID(), patient);
                }

                // update LiveData and notify observers
                patientObservations.postValue(poHashMap);

                Log.i("SharedViewModel", "current polling frequency :" + frequency);
                timer.postDelayed(this, frequency);
            }});
    }

}
