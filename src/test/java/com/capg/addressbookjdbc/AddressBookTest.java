package com.capg.addressbookjdbc;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import org.junit.Test;

import com.capg.addressbook.AddressBookService;
import com.capg.addressbook.AddressBookService.IOService;
import com.capg.addressbook.Contact;

public class AddressBookTest {
	/**
	 * UC16: Retrieve data from the database
	 * 
	 * @throws DatabaseException
	 */
	@Test
	public void givenContactDataInDB_WhenRetrieved_ShouldMatchContactCount() throws DatabaseException {
		AddressBookService addressBookService = new AddressBookService();
		List<Contact> contactData = addressBookService.readContactData(IOService.DB_IO);
		assertEquals(4, contactData.size());
	}

	/**
	 * UC17: Updating phone number of a persons in contacts table
	 * 
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	@Test
	public void givenNewDataForContact_WhenUpdated_ShouldBeInSync() throws DatabaseException, SQLException {
		AddressBookService addressBookService = new AddressBookService();
		@SuppressWarnings("unused")
		List<Contact> contactData = addressBookService.readContactData(IOService.DB_IO);
		addressBookService.updatePersonsPhone("Aditya", "8850273350");
		addressBookService.readContactData(IOService.DB_IO);
		boolean result = addressBookService.checkContactDataSync("Aditya");
		assertEquals(true, result);
	}

	/**
	 * UC18: retrieving data from the table between data range
	 * 
	 * @throws DatabaseException
	 */
	@Test
	public void givenContactInDB_WhenRetrievedForDateRange_ShouldMatchContactCount() throws DatabaseException {
		AddressBookService addressBookService = new AddressBookService();
		@SuppressWarnings("unused")
		List<Contact> contactData = addressBookService.readContactData(IOService.DB_IO);
		List<Contact> resultList = addressBookService.getContactForDateRange(LocalDate.of(2018, 01, 02),
				LocalDate.of(2020, 01, 02));
		assertEquals(1, resultList.size());
	}

	/**
	 * UC19: retrieving data from table by state or city name
	 * 
	 * @throws DatabaseException
	 */
	@Test
	public void givenContactInDB_WhenRetrievedForCityAndState_ShouldMatchContactCount() throws DatabaseException {
		AddressBookService addressBookService = new AddressBookService();
		@SuppressWarnings("unused")
		List<Contact> contactData = addressBookService.readContactData(IOService.DB_IO);
		List<Contact> resultList = addressBookService.getContactForCityAndState("Akola", "Maharashta");
		assertEquals(2, resultList.size());
	}
}
