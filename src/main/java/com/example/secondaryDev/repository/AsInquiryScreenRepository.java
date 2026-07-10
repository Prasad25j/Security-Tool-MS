package com.example.secondaryDev.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.entity.AsInquiryScreen;

@Repository
public interface AsInquiryScreenRepository extends JpaRepository<AsInquiryScreen, String> {
    List<AsInquiryScreen> findByCOMPANYGUID(String companyGuid);
    List<AsInquiryScreen> findByPLANGUID(String planGuid);
    List<AsInquiryScreen> findByPRODUCTGUID(String productGuid);
}
