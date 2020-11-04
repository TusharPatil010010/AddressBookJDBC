package com.capg.addressbookjdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.capg.addressbook.Contact;

public class AddressBookDB {
	private static AddressBookDB addressBookDB;

	public AddressBookDB() {
	}

	public static AddressBookDB getInstance() {
		if (addressBookDB == null) {
			addressBookDB = new AddressBookDB();
		}
		return addressBookDB;
	}

	private Connection getConnection() throws DatabaseException {
		String jdbcURL = "jdbc:mysql://localhost:3306/addressbookservice?useSSL=false";
		String userName = "root";
		String password = "open";
		Connection connection = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection(jdbcURL, userName, password);
		} catch (Exception e) {
			throw new DatabaseException("Connection was unsuccessful");
		}
		return connection;

	}

	public List<Contact> readData() throws DatabaseException {
		String sql = " select contact_table.contact_id, contact_table.firstname, contact_table.lastname, contact_table.address, contact_table.zip, "
				+ "contact_table.city, contact_table.state, contact_table.phone, contact_table.email, addressBook.addName, addressBook.type "
				+ "from contact_table inner join addressBook on contact_table.contact_id = addressBook.contacts_id; ";
		return this.getContactData(sql);
	}

	private List<Contact> getContactData(String sql) throws DatabaseException {
		List<Contact> contactList = new ArrayList<>();
		try (Connection connection = this.getConnection()) {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				String fname = resultSet.getString("firstname");
				String lname = resultSet.getString("lastname");
				String address = resultSet.getString("address");
				long zip = resultSet.getLong("zip");
				String city = resultSet.getString("city");
				String state = resultSet.getString("state");
				long phoneNumber = resultSet.getLong("phone");
				String email = resultSet.getString("email");
				contactList.add(new Contact(fname, lname, address, state, city, zip, phoneNumber, email));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return contactList;
	}

}
