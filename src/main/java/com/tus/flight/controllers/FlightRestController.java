package com.tus.flight.controllers;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	// Setting up Logger
	Logger log = LoggerFactory.getLogger(FlightRestController.class);
		
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
	 * @return ResponseEntity with a status code of 201 CREATED and the created flight in the response body.
	 */
	@RequestMapping(value = "/flights", method = RequestMethod.POST)
	public ResponseEntity<Flight> create(@Valid @NotNull @RequestBody Flight flight) {
		log.info("FlightManagerApplication POST method called ");
		// Retrieving a 'Discount' instance from a third-party service using 'RestTemplate'
		Discount discount = restTemplate.getForObject(discountServiceURL + flight.getDiscountCode(), Discount.class);
		// Subtracting the discount from the flight's price if discount code exists
		if(discount==null) {
			flight.setPrice(flight.getPrice());
		}
		else {
			flight.setPrice(flight.getPrice().subtract(discount.getDiscount()));
		}
		// Saving the flight to the repository and returning it
		Flight flightCreated = repo.save(flight);
		// Using ServletUriComponentsBuilder to build the Location header URL
				URI location = ServletUriComponentsBuilder.fromCurrentRequest()
			            .path("/flights")
			            .buildAndExpand(flightCreated.getId())
			            .toUri();
		// Returning the ResponseEntity with the created flight in the response body
	    return ResponseEntity.created(location).body(flightCreated);
		
	}
	
	/**
	 * Handling GET request to retrieve all flights with the given destination from the repository or null if none found.
	 * Mapping HTTP GET requests to the '/flights/destinations/{destination}' endpoint
	 * @param destination The destination to search for.
	 * @return A ResponseEntity containing the List of retrieved flights or a not found status if no flight was not found.
	 */
	@RequestMapping(value = "/flights/destinations/{destination}", method = RequestMethod.GET)
	public ResponseEntity<List<Flight>> getFlightByDestination(@PathVariable("destination")@Valid String destination) {
		log.info("FlightManagerApplication GET method called with destination param");
		// Finding Flight objects with provided destination and saving it into flightsFoundByDestination list
		List<Flight> flightsFoundByDestination = repo.findByDestination(destination);
		// Returning a not found status if no matches found
		if(flightsFoundByDestination == null || flightsFoundByDestination.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		// Returning a response entity with the retrieved flights and an OK status
		return ResponseEntity.status(HttpStatus.OK).body(flightsFoundByDestination);
	}
	
	/**
	 * Retrieving all flights with the given origin from the repository
	 * Mapping HTTP GET requests to the '/flights/origins/{origin}'
	 * @param origin The origin to search for.
	 * @return A ResponseEntity containing the List of retrieved flights or a not found status if no flight was not found.
	 */
	@RequestMapping(value = "/flights/origins/{origin}", method = RequestMethod.GET)
	public ResponseEntity<List<Flight>> getFlightByOrigin(@PathVariable("origin")@Valid String origin) {
		log.info("FlightManagerApplication GET method called with origin param");
		// Finding Flight objects with provided destination and saving it into flightsFoundByOrigin list
		List<Flight> flightsFoundByOrigin = repo.findByOrigin(origin);
		// Returning a not found status if no matches found
		if(flightsFoundByOrigin == null || flightsFoundByOrigin.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		// Returning a response entity with the retrieved flights and an OK status
		return ResponseEntity.status(HttpStatus.OK).body(flightsFoundByOrigin);
	}
	
	/**
	 * A List of all Flight objects in the repository.
	 * Mapping HTTP GET requests to the '/flights' endpoint
	 * @return A ResponseEntity containing the List of retrieved flights or a not found status if no flight was not found.
	 */
	@RequestMapping(value = "/flights", method = RequestMethod.GET)
	public ResponseEntity<List<Flight>> getFlights() {
		log.info("FlightManagerApplication GET method called");
		// Finding all Flight objects and saving it into allFlights list
		List<Flight> allFlights = repo.findAll();
		// Returning a not found status if no matches found
		if(allFlights == null || allFlights.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		// Returning a response entity with the retrieved flights and an OK status
		return ResponseEntity.status(HttpStatus.OK).body(allFlights);
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
		log.info("FlightManagerApplication GET method called with minPrice and maxPrice params");
		// Returning a bad request status if the parameters are less than zero
		if (minPrice.compareTo(BigDecimal.ZERO) < 0 || maxPrice.compareTo(BigDecimal.ZERO) < 0) {
	        // Returning a bad request status if the input values are less than 0
	        return ResponseEntity.badRequest().build();
	    }
		List<Flight> flights = repo.findByPriceBetween(minPrice, maxPrice);
		// Returning a not found status if no matches found
		if(flights == null || flights.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		// Wrapping the flights in a ResponseEntity and returning it
	    return ResponseEntity.ok(flights);		
	}
}
