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

import org.junit.Test;

import com.capg.addressbook.AddressBookService;
import com.capg.addressbook.AddressBookService.IOService;
import com.capg.addressbook.Contact;

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
		addressBookService.updatePersonsPhone("Tushar Patil", 9876543210L);
		addressBookService.readContactData(IOService.DB_IO);
		boolean result = addressBookService.checkContactDataSync("Aditya Kharade");
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
				9856743210L, "mukesh@gmail.com", LocalDate.of(2021, 01, 02), 1, "AddressBook1", "family");
		boolean result = addressBookService.checkContactDataSync("Aniket");
		assertEquals(true, result);
	}

	@Test
	public void geiven2Contacts_WhenAddedToDB_ShouldMatchContactEntries() throws DatabaseException {
		Contact[] contactArray = {
				new Contact("Jeff", "Bezos", "Ichalkaranji", "Kolhapur", "Maharashtra", 416115L, 9850273350L,
						"jeff@gmail.com", LocalDate.of(2021, 01, 02), 2, "AddressBook2", "friend"),
				new Contact("Elon", "Musk", "Ichalkaranji", "Kolhapur", "Maharashtra", 416115L, 9887483853L,
						"elon@gmail.com", LocalDate.of(2021, 01, 01), 2, "AddressBook2", "friend") };
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
	public void geiven2Persons_WhenUpdatedPhoneNumer_ShouldSyncWithDB() throws DatabaseException {
		Map<String, Long> contactMap = new HashMap<>();
		contactMap.put("Jeff Bezos", 9788996633L);
		contactMap.put("Elon Musk", 9455776699L);
		AddressBookService addressBookService = new AddressBookService();
		addressBookService.readContactData(IOService.DB_IO);
		Instant start = Instant.now();
		addressBookService.updatePhoneNumber(contactMap);
		Instant end = Instant.now();
		System.out.println("Duration with Thread: " + Duration.between(start, end));
		boolean result = addressBookService.checkContactInSyncWithDB(Arrays.asList("Aniket Sarap"));
		assertEquals(true, result);
	}
}