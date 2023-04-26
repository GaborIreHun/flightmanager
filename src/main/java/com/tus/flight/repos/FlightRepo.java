package com.tus.flight.repos;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tus.flight.model.Flight;

public interface FlightRepo extends JpaRepository<Flight, Long> {
	
	List<Flight> findByDestination(String destination);
	List<Flight> findByOrigin(String origin);
	List<Flight> findAll();
	List<Flight> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
}
