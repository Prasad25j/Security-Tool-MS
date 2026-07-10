package com.example.secondaryDev.repository.coa;

import com.example.entity.coa.AsChartOfAccounts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AsChartOfAccountsRepository extends JpaRepository<AsChartOfAccounts, String> {
    Optional<AsChartOfAccounts> findByACCOUNTNUMBER(String ACCOUNTNUMBER);
    List<AsChartOfAccounts> findByCOMPANYGUID(String COMPANYGUID);

    @Query(value = "SELECT c.COMPANYGUID, c.COMPANYNAME, " +
                   "a.CHARTOFACCOUNTSGUID, a.ACCOUNTNUMBER, a.ACCOUNTDESCRIPTION, " +
                   "e.CHARTOFACCOUNTSENTITYGUID, e.CHARTOFACCOUNTSENTITYCODE, e.TRANSACTIONNAME, " +
                   "en.CHARTOFACCOUNTSENTRYGUID, en.DEBITCREDITCODE, en.ENTRYDESCRIPTION, " +
                   "en.ACCOUNTINGTYPECODE, en.ACCOUNTINGAMOUNTFIELD, en.FUNDTYPECODE, " +
                   "en.ORIGINALDISBURSEMENTSTATUSCODE " +
                   "FROM ASCOMPANY c " +
                   "JOIN ASCHARTOFACCOUNTS a ON a.COMPANYGUID = c.COMPANYGUID " +
                   "LEFT JOIN ASCHARTOFACCOUNTSENTITY e ON e.CHARTOFACCOUNTSGUID = a.CHARTOFACCOUNTSGUID " +
                   "LEFT JOIN ASCHARTOFACCOUNTSENTRY en ON en.CHARTOFACCOUNTSENTITYGUID = e.CHARTOFACCOUNTSENTITYGUID " +
                   "WHERE c.COMPANYGUID = :companyGuid", nativeQuery = true)
    List<Object[]> fetchHierarchyData(@Param("companyGuid") String companyGuid);
}
