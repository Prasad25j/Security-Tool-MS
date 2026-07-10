package com.example.secondaryDev.repository;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.entity.security.AsAuthProductInquiry;

@Repository
public interface AsAuthProductInquiryRepository extends JpaRepository<AsAuthProductInquiry, String> {
    interface Projection {
        String getAUTHPRODUCTINQUIRYGUID();
        String getAUTHPRODUCTGUID();
        String getINQUIRYSCREENGUID();
    }
    List<AsAuthProductInquiry> findBySECURITYGROUPGUID(String securityGroupGuid);
    List<AsAuthProductInquiry> findBySECURITYGROUPGUIDAndCOMPANYGUIDAndPRODUCTGUID(String securityGroupGuid, String companyGuid, String productGuid);
    List<Projection> findByAUTHPRODUCTGUIDIn(Collection<String> authProductGuids);

    @Query("select i.AUTHPRODUCTINQUIRYGUID as AUTHPRODUCTINQUIRYGUID, i.AUTHPRODUCTGUID as AUTHPRODUCTGUID, i.INQUIRYSCREENGUID as INQUIRYSCREENGUID " +
           "from AsAuthProductInquiry i " +
           "join AsAuthProduct ap on i.AUTHPRODUCTGUID = ap.AUTHPRODUCTGUID " +
           "join AsAuthCompany c on ap.AUTHCOMPANYGUID = c.AUTHCOMPANYGUID " +
           "where c.SECURITYGROUPGUID = :securityGroupGuid")
    List<Projection> findBySecurityGroupGuid(@Param("securityGroupGuid") String securityGroupGuid);
}
