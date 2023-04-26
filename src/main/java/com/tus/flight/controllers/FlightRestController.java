package com.tus.flight.controllers;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.tus.flight.dto.Discount;
import com.tus.flight.model.Flight;
import com.tus.flight.repos.FlightRepo;

@RestController
@RequestMapping("/flightapi")
public class FlightRestController {

	@Autowired
	private FlightRepo repo;

	@Autowired
	private RestTemplate restTemplate;

	@Value("${discountService.url}")
	private String discountServiceURL;
	
	@RequestMapping(value = "/flights", method = RequestMethod.POST)
	public Flight create(@RequestBody Flight flight) {
		Discount discount = restTemplate.getForObject(discountServiceURL + flight.getDiscountCode(), Discount.class);
		flight.setPrice(flight.getPrice().subtract(discount.getDiscount()));
		return repo.save(flight);
	}
	
	@RequestMapping(value = "/flights/destinations/{destination}", method = RequestMethod.GET)
	public List<Flight> getFlightByDestination(@PathVariable("destination") String destination) {
		return repo.findByDestination(destination);
	}
	
	@RequestMapping(value = "/flights/origins/{origin}", method = RequestMethod.GET)
	public List<Flight> getFlightByOrigin(@PathVariable("origin") String origin) {
		return repo.findByOrigin(origin);
	}
	
	@RequestMapping(value = "/flights", method = RequestMethod.GET)
	public List<Flight> getFlights() {
		return repo.findAll();
	}
	
	@GetMapping(value = "/flights/by-price")
	public ResponseEntity<List<Flight>> getEntitiesByPriceRange(@RequestParam BigDecimal minPrice, @RequestParam BigDecimal maxPrice) {
		List<Flight> flights = repo.findByPriceBetween(minPrice, maxPrice);
	    return ResponseEntity.ok(flights);		
	}
}
