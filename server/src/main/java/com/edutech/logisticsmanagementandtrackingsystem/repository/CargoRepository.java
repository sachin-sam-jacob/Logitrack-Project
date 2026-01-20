package com.edutech.logisticsmanagementandtrackingsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.edutech.logisticsmanagementandtrackingsystem.entity.Cargo;

import java.util.List;
import java.util.Optional;

@Repository
public interface CargoRepository extends JpaRepository<Cargo,Long>{
    List<Cargo> findByBusinessId(Long businessId);

    List<Cargo> findByDriverId(Long driverId);

    // NEW: Find by tracking number
    Optional<Cargo> findByTrackingNumber(String trackingNumber);

    @Query(
    "SELECT c FROM Cargo c " +
    "WHERE c.business.id = :businessId " +
    "AND (" +
    "LOWER(c.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
    "OR LOWER(c.status) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
    "OR (:cargoId IS NOT NULL AND c.id = :cargoId)" +
    ")"
)
List<Cargo> searchBusinessCargo(
        @Param("businessId") Long businessId,
        @Param("keyword") String keyword,
        @Param("cargoId") Long cargoId
);
}