package com.mentorship.food_delivery_app.restaurant.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mentorship.food_delivery_app.restaurant.entity.RestaurantBranch;

@Repository
public interface RestaurantBranchRepository extends JpaRepository<RestaurantBranch, UUID> {

    @Query("select b.id from RestaurantBranch b where b.restaurantId = :restaurantId")
    List<UUID> findIdsByRestaurantId(@Param("restaurantId") UUID restaurantId);
}
