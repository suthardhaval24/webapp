package com.cloud.repository;

import com.cloud.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BillRepository extends JpaRepository<Bill, UUID> {

    @Query("SELECT count(id) FROM Bill WHERE id=:id")
    int isBillPresent(@Param("id") UUID id);

    @Query("FROM Bill b WHERE b.user.uuid=:user_id")
    List<Bill> findByOwnerId(@Param("user_id") UUID owner_id);
}
