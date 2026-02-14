package com.MedLab.dicom_converter_web.repository;

import com.MedLab.dicom_converter_web.domain.ConversionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversionRepository extends JpaRepository<ConversionEntity, Long> {
    List<ConversionEntity> findByDicomId(Long dicomId);
}
