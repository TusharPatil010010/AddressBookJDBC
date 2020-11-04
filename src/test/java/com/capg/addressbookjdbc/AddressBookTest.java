package com.capg.addressbookjdbc;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.capg.addressbook.AddressBookService;
import com.capg.addressbook.AddressBookService.IOService;
import com.capg.addressbook.Contact;
import com.capg.addressbook.*;
import com.capg.addressbookjdbc.*;

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
}
