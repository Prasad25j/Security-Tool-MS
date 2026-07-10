package com.example.secondaryDev.repository;

import com.example.entity.AsCode;
import com.example.entity.AsCodeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AsCodeRepository extends JpaRepository<AsCode, AsCodeId> {
    List<AsCode> findByCODENAME(String codeName);

    @Query("SELECT a FROM AsCode a WHERE a.CODENAME LIKE %:pattern%")
    List<AsCode> findByCodeNameLike(@Param("pattern") String pattern);
}
