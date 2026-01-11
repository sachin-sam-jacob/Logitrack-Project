package com.edutech.logisticsmanagementandtrackingsystem;

import com.edutech.logisticsmanagementandtrackingsystem.dto.LoginRequest;
import com.edutech.logisticsmanagementandtrackingsystem.entity.*;
import com.edutech.logisticsmanagementandtrackingsystem.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class LogisticsManagementAndTrackingSystemApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BusinessRepository businessRepository;

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private DriverRepository driverRepository;

	@Autowired
	private CargoRepository cargoRepository;


	@BeforeEach
	public void setUp() {
		// Clear the database before each test
		userRepository.deleteAll();
		cargoRepository.deleteAll();
		driverRepository.deleteAll();
		customerRepository.deleteAll();
		businessRepository.deleteAll();
	}

	@Test
	public void testRegisterBusiness() throws Exception {
		// Create a User object for registration
		User user = new User();
		user.setUsername("testUser");
		user.setPassword("testPassword");
		user.setEmail("test@example.com");
		user.setRole("BUSINESS");

		// Perform a POST request to the /register endpoint using MockMvc
		mockMvc.perform(post("/api/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsBytes(user)))
				.andExpect(jsonPath("$.name").value(user.getUsername()))
				.andExpect(jsonPath("$.email").value(user.getEmail()));

		// Assert business is created in the database
		Business savedBusiness = businessRepository.findAll().get(0);
		assertEquals(user.getUsername(), savedBusiness.getName());
		assertEquals(user.getEmail(), savedBusiness.getEmail());

		// Assert user is created in the database
		User savedUser = userRepository.findAll().get(0);
		assertEquals(user.getEmail(), savedUser.getEmail());
		assertEquals("BUSINESS", savedUser.getRole());
	}

	@Test
	public void testRegisterCustomer() throws Exception {
		// Create a User object for registration
		User user = new User();
		user.setUsername("testCustomer");
		user.setPassword("testPassword");
		user.setEmail("test@example.com");
		user.setRole("CUSTOMER");

		// Perform a POST request to the /register endpoint using MockMvc
		mockMvc.perform(post("/api/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsBytes(user)))
				.andExpect(jsonPath("$.name").value(user.getUsername()))
				.andExpect(jsonPath("$.email").value(user.getEmail()));

		// Assert business is created in the database
		Customer savedCustomer = customerRepository.findAll().get(0);
		assertEquals(user.getUsername(), savedCustomer.getName());
		assertEquals(user.getEmail(), savedCustomer.getEmail());

		// Assert user is created in the database
		User savedUser = userRepository.findAll().get(0);
		assertEquals(user.getEmail(), savedUser.getEmail());
		assertEquals("CUSTOMER", savedUser.getRole());
	}

	@Test
	public void testRegisterDriver() throws Exception {
		// Create a User object for registration
		User user = new User();
		user.setUsername("testDriver");
		user.setPassword("testPassword");
		user.setEmail("test@example.com");
		user.setRole("DRIVER");

		// Perform a POST request to the /register endpoint using MockMvc
		mockMvc.perform(post("/api/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsBytes(user)))
				.andExpect(jsonPath("$.name").value(user.getUsername()))
				.andExpect(jsonPath("$.email").value(user.getEmail()));

		// Assert business is created in the database
		Driver savedDriver = driverRepository.findAll().get(0);
		assertEquals(user.getUsername(), savedDriver.getName());
		assertEquals(user.getEmail(), savedDriver.getEmail());

		// Assert user is created in the database
		User savedUser = userRepository.findAll().get(0);
		assertEquals(user.getEmail(), savedUser.getEmail());
		assertEquals("DRIVER", savedUser.getRole());
	}

	@Test
	public void testLoginUser() throws Exception {
		// Create a user for registration
		User user = new User();
		user.setUsername("user1");
		user.setPassword("password");
		user.setRole("CUSTOMER");
		user.setEmail("user@gmail.com");
		// Register the user
		mockMvc.perform(post("/api/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(user)));

		// Login with the registered user
		LoginRequest loginRequest = new LoginRequest("user1", "password");

		mockMvc.perform(post("/api/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(loginRequest)))
				.andExpect(jsonPath("$.token").exists());
	}

	@Test
	public void testLoginWithWrongUsernameOrPassword() throws Exception {
		// Create a login request with a wrong username
		LoginRequest loginRequest = new LoginRequest("wronguser", "password");

		mockMvc.perform(post("/api/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(loginRequest)))
				.andExpect(status().isUnauthorized()); // Expect a 401 Unauthorized response
	}

	@Test
	@WithMockUser(authorities = "CUSTOMER")
	public void testViewCargoStatus() throws Exception {

		Cargo cargo = new Cargo();
		cargo.setContent("Test content");
		cargo.setSize("Medium");
		cargo.setStatus("PENDING");

		cargo = cargoRepository.save(cargo);

		// Performing the request and asserting the response
		mockMvc.perform(MockMvcRequestBuilders.get("/api/customer/cargo-status")
						.param("cargoId", String.valueOf(cargo.getId()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.cargoId").value(cargo.getId()))
				.andExpect(jsonPath("$.status").value(cargo.getStatus()));

	}

	@Test
	@WithMockUser(authorities = "DRIVER")
	public void testUpdateCargoStatus() throws Exception {

		Cargo cargo = new Cargo();
		cargo.setContent("Test content");
		cargo.setSize("Medium");
		cargo.setStatus("PENDING");

		cargo = cargoRepository.save(cargo);

		// Performing the request and asserting the response
		mockMvc.perform(MockMvcRequestBuilders.put("/api/driver/update-cargo-status")
						.param("cargoId", String.valueOf(cargo.getId()))
						.param("newStatus", "DELIVERED")
						.contentType(MediaType.APPLICATION_JSON));

		// Assert the cargo status is updated in the database
		Cargo updatedCargo = cargoRepository.findById(cargo.getId()).get();
		assertEquals("DELIVERED", updatedCargo.getStatus());

	}

	@Test
	@WithMockUser(authorities = "DRIVER")
	public void testViewAssignedCargos() throws Exception {
		// Create a driver
		Driver driver = new Driver();
		driver.setName("John Doe");
		driver.setEmail("john.doe@example.com");
		driverRepository.save(driver);

		// Create cargos assigned to the driver
		Cargo cargo1 = new Cargo();
		cargo1.setContent("Cargo 1");
		cargo1.setSize("Large");
		cargo1.setStatus("PENDING");
		cargo1.setDriver(driver);
		cargoRepository.save(cargo1);

		Cargo cargo2 = new Cargo();
		cargo2.setContent("Cargo 2");
		cargo2.setSize("Medium");
		cargo2.setStatus("IN_TRANSIT");
		cargo2.setDriver(driver);
		cargoRepository.save(cargo2);

		mockMvc.perform(MockMvcRequestBuilders.get("/api/driver/cargo")
						.param("driverId", String.valueOf(driver.getId()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].content", is("Cargo 1")))
				.andExpect(jsonPath("$[1].content", is("Cargo 2")));
	}

	@Test
	@WithMockUser(authorities = "BUSINESS")
	public void testViewAllCargos() throws Exception {
		// Create sample cargos
		Cargo cargo1 = new Cargo();
		cargo1.setContent("Cargo 1");
		cargo1.setSize("Large");
		cargo1.setStatus("PENDING");
		cargoRepository.save(cargo1);

		Cargo cargo2 = new Cargo();
		cargo2.setContent("Cargo 2");
		cargo2.setSize("Medium");
		cargo2.setStatus("IN_TRANSIT");
		cargoRepository.save(cargo2);

		// Perform the GET request to the endpoint
		mockMvc.perform(MockMvcRequestBuilders.get("/api/business/cargo")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].content", is("Cargo 1")))
				.andExpect(jsonPath("$[0].size", is("Large")))
				.andExpect(jsonPath("$[0].status", is("PENDING")))
				.andExpect(jsonPath("$[1].content", is("Cargo 2")))
				.andExpect(jsonPath("$[1].size", is("Medium")))
				.andExpect(jsonPath("$[1].status", is("IN_TRANSIT")));
	}

	@Test
	@WithMockUser(authorities = "BUSINESS")
	public void testAssignCargoToDriver() throws Exception {
		// Create a driver
		Driver driver = new Driver();
		driver.setName("John Doe");
		driver.setEmail("john.doe@example.com");
		driverRepository.save(driver);

		// Create a cargo
		Cargo cargo = new Cargo();
		cargo.setContent("Test Cargo");
		cargo.setSize("Medium");
		cargo.setStatus("PENDING");
		cargoRepository.save(cargo);


		// Perform the POST request to the endpoint
		mockMvc.perform(post("/api/business/assign-cargo")
				.param("cargoId", String.valueOf(cargo.getId()))
				.param("driverId", String.valueOf(driver.getId()))
						.contentType(MediaType.APPLICATION_JSON));

		// Verify that the cargo has been assigned to the driver
		Cargo assignedCargo = cargoRepository.findById(cargo.getId()).orElse(null);
		assertNotNull(assignedCargo);
		assertEquals(driver.getId(), assignedCargo.getDriver().getId());
	}

	@Test
	@WithMockUser(authorities = "BUSINESS")
	public void testAddCargo() throws Exception {
		// Prepare a sample Cargo object for the request payload
		Cargo cargoToAdd = new Cargo();
		cargoToAdd.setContent("Test Cargo");
		cargoToAdd.setSize("Medium");
		cargoToAdd.setStatus("PENDING");

		String payload = objectMapper.writeValueAsString(cargoToAdd);

		// Perform the POST request to the endpoint with the correct Content-Type header
		mockMvc.perform(post("/api/business/cargo")
						.contentType(MediaType.APPLICATION_JSON) // Set the content type
						.content(payload))
				.andExpect(jsonPath("$.content", is("Test Cargo")))
				.andExpect(jsonPath("$.size", is("Medium")))
				.andExpect(jsonPath("$.status", is("PENDING")));

	}

	@Test
	public void testPermissionForBusinessEndpoints() throws Exception {
		mockMvc.perform(post("/api/business/cargo"))
				.andExpect(status().isForbidden());

		mockMvc.perform(MockMvcRequestBuilders.get("/api/business/cargo"))
				.andExpect(status().isForbidden());

		mockMvc.perform(post("/api/business/assign-cargo"))
				.andExpect(status().isForbidden());
	}

	@Test
	public void testPermissionForDriverEndpoints() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/api/driver/cargo"))
				.andExpect(status().isForbidden());

		mockMvc.perform(MockMvcRequestBuilders.put("/api/driver/update-cargo-status"))
				.andExpect(status().isForbidden());
	}

	@Test
	public void testPermissionForCustomerEndpoints() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/api/customer/cargo-status"))
				.andExpect(status().isForbidden());
	}

}
