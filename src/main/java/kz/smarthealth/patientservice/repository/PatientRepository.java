package kz.smarthealth.patientservice.repository;

import kz.smarthealth.patientservice.model.entity.PatientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<PatientEntity, UUID> {

    List<PatientEntity> findAllByUserId(UUID userId);
}
