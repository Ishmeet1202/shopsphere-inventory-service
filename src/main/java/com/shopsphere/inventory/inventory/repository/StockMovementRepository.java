package com.shopsphere.inventory.inventory.repository;

import com.shopsphere.inventory.inventory.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, String> {

}
