package com.example.secondaryDev.repository.coa;

import com.example.entity.coa.AsChartOfAccountsCriteria;
import com.example.entity.coa.AsChartOfAccountsCriteriaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AsChartOfAccountsCriteriaRepository extends JpaRepository<AsChartOfAccountsCriteria, AsChartOfAccountsCriteriaId> {
    List<AsChartOfAccountsCriteria> findByCHARTOFACCOUNTSENTRYGUID(String CHARTOFACCOUNTSENTRYGUID);
}
