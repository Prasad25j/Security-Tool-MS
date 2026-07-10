package com.example.secondaryDev.repository.coa;

import com.example.entity.coa.AsChartOfAccountsEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AsChartOfAccountsEntryRepository extends JpaRepository<AsChartOfAccountsEntry, String> {
    List<AsChartOfAccountsEntry> findByCHARTOFACCOUNTSENTITYGUID(String CHARTOFACCOUNTSENTITYGUID);
    List<AsChartOfAccountsEntry> findByCHARTOFACCOUNTSENTITYGUIDIn(List<String> CHARTOFACCOUNTSENTITYGUIDs);
    List<AsChartOfAccountsEntry> findTop300By();
}
