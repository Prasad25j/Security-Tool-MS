package com.example.secondaryDev.repository.coa;

import com.example.entity.coa.AsChartOfAccountsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AsChartOfAccountsEntityRepository extends JpaRepository<AsChartOfAccountsEntity, String> {
    List<AsChartOfAccountsEntity> findByTRANSACTIONNAME(String TRANSACTIONNAME);
    List<AsChartOfAccountsEntity> findByCHARTOFACCOUNTSGUID(String CHARTOFACCOUNTSGUID);
    List<AsChartOfAccountsEntity> findByCHARTOFACCOUNTSGUIDIn(List<String> CHARTOFACCOUNTSGUIDs);
    List<AsChartOfAccountsEntity> findTop200By();
}
