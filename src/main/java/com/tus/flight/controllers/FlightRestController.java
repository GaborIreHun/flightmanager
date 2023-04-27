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

/**
 * This class represents a REST controller that handles HTTP requests and responses
 * It also maps all endpoints in this class to the base URL '/flightapi'
 * @author A00304775
 * 
 */
@RestController
@RequestMapping("/flightapi")
public class FlightRestController {

	// Injecting a 'FlightRepo' instance into this class
	@Autowired
	private FlightRepo repo;

	// Injecting a 'RestTemplate' instance into this class
	@Autowired
	private RestTemplate restTemplate;

	// Retrieving the value of 'discountService.url' from the application.properties file
	@Value("${discountService.url}")
	private String discountServiceURL;
	
	/**
	 * Handling a POST request to create a new flight while utilising relevant discount code for the price
	 * Mapping HTTP POST requests to the '/flights' endpoint
	 * @param flight The Flight object to be created.
	 * @return The created Flight object
	 */
	@RequestMapping(value = "/flights", method = RequestMethod.POST)
	public Flight create(@RequestBody Flight flight) {
		// Retrieving a 'Discount' instance from a third-party service using 'RestTemplate'
		Discount discount = restTemplate.getForObject(discountServiceURL + flight.getDiscountCode(), Discount.class);
		// Subtracting the discount from the flight's price
		flight.setPrice(flight.getPrice().subtract(discount.getDiscount()));
		// Saving the flight to the repository and returning it
		return repo.save(flight);
	}
	
	/**
	 * Handling GET request to retrieve all flights with the given destination from the repository or null if none found.
	 * Mapping HTTP GET requests to the '/flights/destinations/{destination}' endpoint
	 * @param destination The destination to search for.
	 * @return A List of Flight objects that have the specified destination.
	 */
	@RequestMapping(value = "/flights/destinations/{destination}", method = RequestMethod.GET)
	public List<Flight> getFlightByDestination(@PathVariable("destination") String destination) {
		return repo.findByDestination(destination);
	}
	
	/**
	 * Retrieving all flights with the given origin from the repository
	 * Mapping HTTP GET requests to the '/flights/origins/{origin}'
	 * @param origin The origin to search for.
	 * @return A List of Flight objects that have the specified origin.
	 */
	@RequestMapping(value = "/flights/origins/{origin}", method = RequestMethod.GET)
	public List<Flight> getFlightByOrigin(@PathVariable("origin") String origin) {
		return repo.findByOrigin(origin);
	}
	
	/**
	 * A List of all Flight objects in the repository.
	 * Mapping HTTP GET requests to the '/flights' endpoint
	 * @return A List of all Flight objects in the repository.
	 */
	@RequestMapping(value = "/flights", method = RequestMethod.GET)
	public List<Flight> getFlights() {
		return repo.findAll();
	}
	
	/**
	 * Handles a GET request to retrieve all flights within a specified price range.
	 * Mapping HTTP GET requests to the '/flights/by-price' endpoint
	 * @param minPrice The minimum price of flights to search for.
	 * @param maxPrice The maximum price of flights to search for.
	 * @return A ResponseEntity containing a List of Flight objects within the specified price range,
	 * or an HTTP status code indicating an error.
	 */
	@GetMapping(value = "/flights/by-price")
	public ResponseEntity<List<Flight>> getEntitiesByPriceRange(@RequestParam BigDecimal minPrice, @RequestParam BigDecimal maxPrice) {
		List<Flight> flights = repo.findByPriceBetween(minPrice, maxPrice);
		// Wrapping the flights in a ResponseEntity and returning it
	    return ResponseEntity.ok(flights);		
	}
}
