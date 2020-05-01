package edu.monash.kmhc.service.repository;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Observation;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import edu.monash.kmhc.model.observation.CholesterolObservationModel;
import edu.monash.kmhc.model.observation.ObservationModel;
import edu.monash.kmhc.model.observation.ObservationType;
import edu.monash.kmhc.service.FhirService;

/**
 * This class is responsible for instantiating other Observation models using the Factory design pattern.
 */
public class ObservationRepositoryFactory extends FhirService {

    private IGenericClient client = super.client;

    /**
     * Gets the bundle from the FHIR server for Observation.
     * @return observation bundle
     */
    private Observation getObservation(String patientId, String code) {
        Bundle bundle = client.search()
                .forResource(Observation.class)
                .where(Observation.PATIENT.hasId(patientId))
                .and(Observation.CODE.exactly().code(code))
                .sort().descending(Observation.DATE)
                .returnBundle(Bundle.class)
                .execute();

        return (Observation) (bundle.getEntry().get(0)).getResource();
    }

    public ObservationModel getObservationModel(String patientId, ObservationType type) {
        switch (type) {
            case CHOLESTEROL:
                return createCholesterolModel(patientId);
            default:
                throw new IllegalArgumentException("Observation type invalid");
        }
    }

    private CholesterolObservationModel createCholesterolModel(String patientId) {
        return new CholesterolObservationModel(getObservation(patientId, ObservationType.CHOLESTEROL.getObservationCode()));
    }
}