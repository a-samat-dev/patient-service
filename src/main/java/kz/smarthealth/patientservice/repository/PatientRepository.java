package kz.smarthealth.patientservice.repository;

import kz.smarthealth.patientservice.model.entity.PatientDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientRepository extends MongoRepository<PatientDocument, String> {

    List<PatientDocument> findAllByUserId(String userId);
}
