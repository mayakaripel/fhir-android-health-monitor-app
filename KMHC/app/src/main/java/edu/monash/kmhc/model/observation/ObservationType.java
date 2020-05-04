package edu.monash.kmhc.model.observation;

/**
 * This class is responsible for storing the different observation types and their codes.
 *
 * It's to allow future designs to be extensible whereby future observation types and their codes
 * only have to be added into this enum.
 */
public enum ObservationType {
    CHOLESTEROL;

    private String observationCode;

    static {
        CHOLESTEROL.observationCode = "2093-3";
    }

    public String getObservationCode() {
        return observationCode;
    }
}
