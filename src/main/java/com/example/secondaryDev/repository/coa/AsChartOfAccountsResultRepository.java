package com.example.secondaryDev.repository.coa;

import com.example.entity.coa.AsChartOfAccountsResult;
import com.example.entity.coa.AsChartOfAccountsResultId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AsChartOfAccountsResultRepository extends JpaRepository<AsChartOfAccountsResult, AsChartOfAccountsResultId> {
    List<AsChartOfAccountsResult> findByCHARTOFACCOUNTSENTRYGUID(String CHARTOFACCOUNTSENTRYGUID);
}
