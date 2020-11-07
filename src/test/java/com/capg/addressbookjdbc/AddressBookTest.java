package com.capg.addressbookjdbc;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.capg.addressbook.AddressBookService;
import com.capg.addressbook.AddressBookService.IOService;
import com.capg.addressbook.Contact;
import com.google.gson.Gson;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class AddressBookTest {

	@Test
	public void givenContactDataInDB_WhenRetrieved_ShouldMatchContactCount() throws DatabaseException {
		AddressBookService addressBookService = new AddressBookService();
		List<Contact> contactData = addressBookService.readContactData(IOService.DB_IO);
		assertEquals(4, contactData.size());
	}

	@Test
	public void givenNewDataForContact_WhenUpdated_ShouldBeInSync() throws DatabaseException, SQLException {
		AddressBookService addressBookService = new AddressBookService();
		addressBookService.readContactData(IOService.DB_IO);
		addressBookService.updatePersonsPhone("Tushar Patil", 9876543210L, IOService.DB_IO);
		addressBookService.readContactData(IOService.DB_IO);
		boolean result = addressBookService.checkContactDataSync("Tushar Patil");
		assertEquals(true, result);
	}

	@Test
	public void givenContactInDB_WhenRetrievedForDateRange_ShouldMatchContactCount() throws DatabaseException {
		AddressBookService addressBookService = new AddressBookService();
		addressBookService.readContactData(IOService.DB_IO);
		List<Contact> resultList = addressBookService.getContactForDateRange(LocalDate.of(2020, 01, 02),
				LocalDate.of(2021, 01, 02));
		assertEquals(1, resultList.size());
	}

	@Test
	public void givenContactInDB_WhenRetrievedForCityAndState_ShouldMatchContactCount() throws DatabaseException {
		AddressBookService addressBookService = new AddressBookService();
		addressBookService.readContactData(IOService.DB_IO);
		List<Contact> resultList = addressBookService.getContactForCityAndState("Kolhapur", "Maharashta");
		assertEquals(2, resultList.size());
	}

	@Test
	public void givenContactInDB_WhenAdded_ShouldBeAddedInSingleTransaction() throws DatabaseException, SQLException {
		AddressBookService addressBookService = new AddressBookService();
		addressBookService.readContactData(IOService.DB_IO);
		addressBookService.addContactInDatabase("Mukesh", "Ambani", "Ichalkaranji", 416115L, "Kolhapur", "Maharashtra",
				9856743210L, "mukesh@gmail.com", LocalDate.of(2021, 01, 02), 1);
		boolean result = addressBookService.checkContactDataSync("Mukesh");
		assertEquals(true, result);
	}

	@Test
	public void geivenTwoContacts_WhenAddedToDB_ShouldMatchContactEntries() throws DatabaseException {
		Contact[] contactArray = {
				new Contact("Jeff", "Bezos", "Ichalkaranji", "Kolhapur", "Maharashtra", 416115L, 9850273350L,
						"jeff@gmail.com", LocalDate.of(2021, 01, 02), 2),
				new Contact("Elon", "Musk", "Ichalkaranji", "Kolhapur", "Maharashtra", 416115L, 9887483853L,
						"elon@gmail.com", LocalDate.of(2021, 01, 01), 2) };
		AddressBookService addressBookService = new AddressBookService();
		addressBookService.readContactData(IOService.DB_IO);
		Instant start = Instant.now();
		addressBookService.addContactToDB(Arrays.asList(contactArray));
		Instant threadEnd = Instant.now();
		System.out.println("Duration with Thread: " + Duration.between(start, threadEnd));
		long result = addressBookService.countEntries(IOService.DB_IO);
		assertEquals(3, result);
	}

	@Test
	public void geivenTwoPersons_WhenUpdatedPhoneNumer_ShouldSyncWithDB() throws DatabaseException {
		Map<String, Long> contactMap = new HashMap<>();
		contactMap.put("Jeff Bezos", 9788996633L);
		contactMap.put("Elon Musk", 9455776699L);
		AddressBookService addressBookService = new AddressBookService();
		addressBookService.readContactData(IOService.DB_IO);
		Instant start = Instant.now();
		addressBookService.updatePhoneNumber(contactMap);
		Instant end = Instant.now();
		System.out.println("Duration with Thread: " + Duration.between(start, end));
		boolean result = addressBookService.checkContactInSyncWithDB(Arrays.asList("Mukesh Ambani"));
		assertEquals(true, result);
	}

	@Before
	public void setup() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = 3000;
	}

	private Contact[] getContactList() {
		Response response = RestAssured.get("/contact");
		System.out.println("Address book contact entries in JSONServer:\n" + response.asString());
		Contact[] arrayOfContact = new Gson().fromJson(response.asString(), Contact[].class);
		return arrayOfContact;
	}

	private Response addContactToJsonServer(Contact contact) {
		String contactJson = new Gson().toJson(contact);
		RequestSpecification request = RestAssured.given();
		request.header("Content-Type", "application/json");
		request.body(contactJson);
		return request.post("/contact");
	}

	@Test
	public void givenEmployeeDataInJSONServer_WhenRetrieved_ShouldMatchTheCount() {
		Contact[] arrayOfContact = getContactList();
		AddressBookService addressBookService = new AddressBookService(Arrays.asList(arrayOfContact));
		long entries = addressBookService.countEntries(IOService.REST_IO);
		assertEquals(1, entries);

	}

	@Test
	public void givenListOfNewContacts_WhenAdded_ShouldMatch201ResponseAndCount() {
		Contact[] arrayOfContact = getContactList();
		AddressBookService addService = new AddressBookService(Arrays.asList(arrayOfContact));
		Contact[] arrayOfCon = {
				new Contact("Sachin", "Tenndulkar", "Ichalkaranji", "Kolhapur", "Maharashtra", 416115L, 9850273350L,
						"sachin@gmail.com", LocalDate.of(2021, 01, 02), 2),
				new Contact("Virat", "Kohli", "Ichalkaranji", "Kolhapur", "Maharashtra", 416115L, 9887483853L,
						"virat@gmail.com", LocalDate.of(2021, 01, 02), 2) };
		List<Contact> contactList = Arrays.asList(arrayOfCon);
		contactList.forEach(contact -> {
			Runnable task = () -> {
				Response response = addContactToJsonServer(contact);
				int statusCode = response.getStatusCode();
				assertEquals(201, statusCode);
				Contact newContact = new Gson().fromJson(response.asString(), Contact.class);
				addService.addContactToAddressBook(newContact);
			};
			Thread thread = new Thread(task, contact.firstName);
			thread.start();
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		long count = addService.countEntries(IOService.REST_IO);
		assertEquals(3, count);
	}

	@Test
	public void givenNewPhoneForContact_WhenUpdated_ShouldMatchRequest() throws DatabaseException, SQLException {
		Contact[] arrayOfContact = getContactList();
		AddressBookService addService = new AddressBookService(Arrays.asList(arrayOfContact));
		addService.updatePersonsPhone("Sachin", 8877442233L, IOService.REST_IO);
		Contact contact = addService.getContact("Sachin");
		String contactJson = new Gson().toJson(contact);
		RequestSpecification request = RestAssured.given();
		request.header("Content-Type", "application/json");
		request.body(contactJson);
		Response response = request.put("/contact/" + contact.id);
		int statusCode = response.getStatusCode();
		assertEquals(200, statusCode);
	}

	@Test
	public void givenContactToDelete_WhenDeleted_ShouldMatch200ResponseAndCount()
			throws DatabaseException, SQLException {
		Contact[] arrayOfContact = getContactList();
		AddressBookService addService = new AddressBookService(Arrays.asList(arrayOfContact));
		Contact contact = addService.getContact("Virat");
		RequestSpecification request = RestAssured.given();
		request.header("Content-Type", "application/json");
		Response response = request.delete("/contact/" + contact.id);
		int statusCode = response.getStatusCode();
		assertEquals(200, statusCode);
		addService.deleteContactFromAddressBook(contact.firstName, IOService.REST_IO);
		long count = addService.countEntries(IOService.REST_IO);
		assertEquals(1, count);
	}
}