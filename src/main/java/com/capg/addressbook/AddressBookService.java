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

	public void writeData(Map<String, AddressBook> cityBookMap) {
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
			e.printStackTrace();
		}
	}

	public void readData() {
		try {
			Files.lines(new File(FILE_NAME).toPath()).forEach(System.out::println);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * UC14 For Writing the data to CSV File
	 * 
	 * @param cityBookMap
	 */
	public void writeDataToCSV(Map<String, AddressBook> cityBookMap) {
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
			exception.printStackTrace();
		}
	}

	/**
	 * Reading data from the CSV file
	 */
	public void readDataFromCSV() {
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
			e.printStackTrace();
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
					e.printStackTrace();
				}
			});
		}
		writer.close();
	}

	/**
	 * UC15 using GSON reading from a JSON file
	 */
	public void readDataFromJSON() {
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
			exception.printStackTrace();
		}
	}

	public List<Contact> readContactData(IOService ioService) throws DatabaseException {
		if (ioService.equals(IOService.DB_IO)) {
			this.contactList = addressBookDB.readData();
		}
		return this.contactList;
	}

	public void updatePersonsPhone(String name, String phone) throws DatabaseException, SQLException {
		int result = addressBookDB.updatePersonsData(name, phone);
		if (result == 0)
			return;
		Contact contact = this.getContact(name);
		if (contact != null)
			contact.phoneNumber = Long.parseLong(phone);
	}

	private Contact getContact(String fname) {
		Contact contact = this.contactList.stream().filter(contactData -> contactData.firstName.equals(fname))
				.findFirst().orElse(null);
		return contact;
	}

	public boolean checkContactDataSync(String name) throws DatabaseException {
		List<Contact> employeeList = addressBookDB.getContactFromData(name);
		return employeeList.get(0).equals(getContact(name));

	}
}
