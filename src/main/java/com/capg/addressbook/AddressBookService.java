package com.capg.addressbook;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.capg.addressbookjdbc.AddressBookDB;
import com.capg.addressbookjdbc.DatabaseException;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonStreamParser;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

public class AddressBookService {
	public static String FILE_NAME = "AddressBook.txt";
	public static String CSV_FILE = "AddressBook.csv";
	public static String JSON_FILE = "AddressBook.json";

	public enum IOService {
		CONSOLE_IO, FILE_IO, DB_IO, REST_IO
	};

	private List<Contact> contactList = new ArrayList<>();
	private AddressBookDB addressBookDB;

	public AddressBookService() {
		addressBookDB = AddressBookDB.getInstance();
	}

	public AddressBookService(List<Contact> list) {
		this();
		this.contactList = new ArrayList<>(list);
	}

	public void writeData(Map<String, AddressBook> cityBookMap) throws AddressBookException {
		StringBuffer employeeBuffer = new StringBuffer();
		for (Map.Entry<String, AddressBook> entry : cityBookMap.entrySet()) {
			entry.getValue().getContactList().forEach(contact -> {
				String empString = contact.toString().concat("\n");
				employeeBuffer.append(empString);
			});
		}
		try {
			Files.write(Paths.get(FILE_NAME), employeeBuffer.toString().getBytes());
		} catch (IOException e) {
			throw new AddressBookException("Unable to write data to the text file");
		}
	}

	public void readData() throws AddressBookException {
		try {
			Files.lines(new File(FILE_NAME).toPath()).forEach(System.out::println);
		} catch (IOException e) {
			throw new AddressBookException("Unable to read data from the text file");
		}
	}

	/**
	 * UC14 For Writing the data to CSV File
	 * 
	 * @param cityBookMap
	 * @throws AddressBookException
	 */
	public void writeDataToCSV(Map<String, AddressBook> cityBookMap) throws AddressBookException {
		Path path = Paths.get(CSV_FILE);
		try {
			FileWriter outputfile = new FileWriter(path.toFile());
			CSVWriter writer = new CSVWriter(outputfile);
			for (Map.Entry<String, AddressBook> entry : cityBookMap.entrySet()) {
				entry.getValue().getContactList().forEach(contact -> {
					String[] data = contact.toString().split(",");
					writer.writeNext(data);
				});
			}
			writer.close();
		} catch (IOException exception) {
			throw new AddressBookException("Unable to write data to the csv file");
		}
	}

	/**
	 * Reading data from the CSV file
	 * 
	 * @throws AddressBookException
	 */
	public void readDataFromCSV() throws AddressBookException {
		try {
			Reader fileReader = Files.newBufferedReader(Paths.get(CSV_FILE));
			@SuppressWarnings("resource")
			CSVReader csvReader = new CSVReader(fileReader);
			String[] data;
			while ((data = csvReader.readNext()) != null) {
				System.out.println("First Name: " + data[0] + " Last Name: " + data[1] + " Address: " + data[2]
						+ " City: " + data[3] + " State: " + data[4] + " ZIP: " + data[5] + " Phone: " + data[6]
						+ " Email: " + data[7]);
			}
		} catch (IOException e) {
			throw new AddressBookException("Unable to read data from the csv file");
		}
	}

	/**
	 * UC15 using GSON writing data to JSON file
	 * 
	 * @param cityBookMap
	 * @throws IOException
	 */
	public void writeDataToJSON(Map<String, AddressBook> cityBookMap) throws IOException {
		Gson gson = new Gson();
		Path path = Paths.get(JSON_FILE);
		FileWriter writer = new FileWriter(path.toFile());
		for (Map.Entry<String, AddressBook> entry : cityBookMap.entrySet()) {
			entry.getValue().getContactList().forEach(contact -> {
				String json = gson.toJson(contact);
				try {
					writer.write(json);
				} catch (IOException e) {
					System.out.println(e);
				}
			});
		}
		writer.close();
	}

	/**
	 * UC15 using GSON reading from a JSON file
	 * 
	 * @throws AddressBookException
	 */
	public void readDataFromJSON() throws AddressBookException {
		Gson gson = new Gson();
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(Paths.get(JSON_FILE).toFile()));
			JsonStreamParser parser = new JsonStreamParser(bufferedReader);
			while (parser.hasNext()) {
				JsonElement jsonElement = parser.next();
				if (jsonElement.isJsonObject()) {
					Contact contact = gson.fromJson(jsonElement, Contact.class);
					System.out.println(contact);
				}
			}
		} catch (Exception exception) {
			throw new AddressBookException("Unable to read data from the json file");
		}
	}

	/**
	 * UC16: Retrieve data from the database
	 * 
	 * @throws DatabaseException
	 */
	public List<Contact> readContactData(IOService ioService) throws DatabaseException {
		if (ioService.equals(IOService.DB_IO)) {
			this.contactList = addressBookDB.readData();
		}
		return this.contactList;
	}

	/**
	 * UC17: Updating phone number of a persons in contact table
	 * 
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	public void updatePersonsPhone(String name, long phone, IOService ioService)
			throws DatabaseException, SQLException {
		if (ioService.equals(IOService.DB_IO)) {
			int result = addressBookDB.updatePersonsData(name, phone);
			if (result == 0)
				return;
		}
		Contact contact = this.getContact(name);
		if (contact != null)
			contact.phoneNumber = phone;
	}

	public Contact getContact(String name) {
		Contact contact = this.contactList.stream().filter(contactData -> contactData.firstName.equals(name))
				.findFirst().orElse(null);
		return contact;
	}

	public boolean checkContactDataSync(String name) throws DatabaseException {
		List<Contact> employeeList = addressBookDB.getContactFromData(name);
		return employeeList.get(0).equals(getContact(name));

	}

	/**
	 * UC18: retrieving data from the table between data range
	 * 
	 * @throws DatabaseException
	 */
	public List<Contact> getContactForDateRange(LocalDate start, LocalDate end) throws DatabaseException {
		return addressBookDB.getContactForDateRange(start, end);
	}

	/**
	 * UC19: retrieving data from the table for city and state
	 * 
	 * @throws DatabaseException
	 */
	public List<Contact> getContactForCityAndState(String city, String state) throws DatabaseException {
		return addressBookDB.getContactForCityAndState(city, state);
	}

	/**
	 * UC20: Insert data into database in a single transaction
	 * 
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	public void addContactInDatabase(String fname, String lname, String address, long zip, String city, String state,
			long phone, String email, LocalDate date, int addId) throws SQLException, DatabaseException {
		this.contactList
				.add(addressBookDB.addContact(fname, lname, address, zip, city, state, phone, email, date, addId));
	}

	/**
	 * UC21 : Adding multiple contacts in the table using multi threading
	 * 
	 * @param contactList
	 */
	public void addContactToDB(List<Contact> contactList) {
		contactList.forEach(contact -> {
			Runnable task = () -> {
				System.out.println("Contact Being Added: " + Thread.currentThread().getName());
				try {
					this.addContactDB(contact.firstName, contact.lastName, contact.address, contact.zip, contact.city,
							contact.state, contact.phoneNumber, contact.email, contact.date, contact.addId);
				} catch (SQLException | DatabaseException e) {
					e.printStackTrace();
				}
				System.out.println("Contact Added: " + Thread.currentThread().getName());
			};
			Thread thread = new Thread(task, contact.firstName);
			thread.start();
			try {
				thread.join();
			} catch (InterruptedException e) {
				System.out.println(Thread.currentThread().getName() + " is interrupted");
			}
		});
	}

	private void addContactDB(String fname, String lname, String address, long zip, String city, String state,
			long phone, String email, LocalDate date, int addId) throws DatabaseException, SQLException {
		this.contactList
				.add(addressBookDB.addContact(fname, lname, address, zip, city, state, phone, email, date, addId));
	}

	public long countEntries(IOService ioService) {
		return contactList.size();
	}

	/**
	 * UC21: Updating the table data using the multi threading
	 * 
	 * @param contactMap
	 */
	public void updatePhoneNumber(Map<String, Long> contactMap) {
		contactMap.forEach((k, v) -> {
			Runnable task = () -> {
				System.out.println("Contact Being Added: " + Thread.currentThread().getName());
				try {
					this.updatePersonsPhone(k, v, IOService.DB_IO);
				} catch (SQLException | DatabaseException e) {
					e.printStackTrace();
				}
				System.out.println("Contact Added: " + Thread.currentThread().getName());
			};
			Thread thread = new Thread(task, k);
			thread.start();
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
	}

	public boolean checkContactInSyncWithDB(List<String> nameList) {
		List<Boolean> resultList = new ArrayList<>();
		nameList.forEach(name -> {
			List<Contact> employeeList;
			try {
				employeeList = addressBookDB.getContactFromData(name);
				resultList.add(employeeList.get(0).equals(getContact(name)));
			} catch (DatabaseException e) {
				System.out.println("Unable to retrieve the data for " + name + " in database");
			}
		});
		if (resultList.contains(false)) {
			return false;
		}
		return true;
	}

	/**
	 * UC23 : Adding multiple contacts to JSON Server
	 * 
	 * @param contact
	 */
	public void addContactToAddressBook(Contact contact) {
		contactList.add(contact);
	}

	/**
	 * UC25 : delete contact from JSON Server
	 * 
	 * @param firstName
	 * @param ioService
	 */
	public void deleteContactFromAddressBook(String firstName, IOService ioService) {
		if (ioService.equals(IOService.REST_IO)) {
			Contact contact = this.getContact(firstName);
			contactList.remove(contact);
		}
	}
}