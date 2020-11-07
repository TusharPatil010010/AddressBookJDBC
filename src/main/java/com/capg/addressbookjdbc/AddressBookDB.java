package com.capg.addressbookjdbc;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.capg.addressbook.Contact;

public class AddressBookDB {
	private static PreparedStatement contactStatement;
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
		String jdbcURL = "jdbc:mysql://localhost:3306/addressBookService?useSSL=false";
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

	/**
	 * UC16: Retrieve data from the database Refactored the query according to
	 * new table structure
	 * 
	 * @throws DatabaseException
	 */
	public List<Contact> readData() throws DatabaseException {
		String sql = "select * from contact_table inner join addressbookjoin using (contact_id) inner join addressbook using(add_id);";
		return this.getContactData(sql);
	}

	List<Contact> getContactData(String sql) throws DatabaseException {
		List<Contact> contactList = new ArrayList<>();
		try (Connection connection = this.getConnection()) {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			contactList = this.getContactData(resultSet);
			return contactList;
		} catch (SQLException e) {
			throw new DatabaseException("Unable to retrieve data");
		}
	}

	/**
	 * UC17: Updating phone number of a persons in contact table
	 * 
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	public int updatePersonsData(String name, long phone) throws DatabaseException, SQLException {
		return this.updatePersonsDataUsingStatement(name, phone);
	}

	private int updatePersonsDataUsingStatement(String name, long phone) throws DatabaseException, SQLException {
		String sql = "Update contact_table set phone = ? where fname = ?";
		int result = 0;
		if (this.contactStatement == null) {
			Connection connection = this.getConnection();
			contactStatement = connection.prepareStatement(sql);
		}
		try {
			contactStatement.setLong(1, phone);
			contactStatement.setString(2, name);
			result = contactStatement.executeUpdate();
		} catch (Exception e) {
			throw new DatabaseException("Unable to execute the update query on contact_table");
		}
		return result;
	}

	private List<Contact> getContactData(ResultSet resultSet) throws SQLException {
		List<Contact> contactList = new ArrayList<>();
		while (resultSet.next()) {
			String fname = resultSet.getString("fname");
			String lname = resultSet.getString("lname");
			String address = resultSet.getString("address");
			long zip = resultSet.getLong("zip");
			String city = resultSet.getString("city");
			String state = resultSet.getString("state");
			long phoneNumber = resultSet.getLong("phone");
			String email = resultSet.getString("email");
			contactList.add(new Contact(fname, lname, address, state, city, zip, phoneNumber, email));
		}
		return contactList;
	}

	public List<Contact> getContactFromData(String name) throws DatabaseException {
		String sql = String.format("SELECT * FROM contact_table WHERE fname = '%s'", name);
		List<Contact> contactList = new ArrayList<>();
		try (Connection connection = this.getConnection()) {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			contactList = this.getContactData(resultSet);
		} catch (SQLException e) {
			throw new DatabaseException("Unable to retrieve data from the contact_table");
		}
		return contactList;
	}

	/**
	 * UC18: retrieving data between the data range Refactored the query
	 * according to new table structure
	 * 
	 * @param start
	 * @param end
	 * @return
	 * @throws DatabaseException
	 */
	public List<Contact> getContactForDateRange(LocalDate start, LocalDate end) throws DatabaseException {
		String sql = String.format(
				"select * from contact_table inner join addressbookjoin using (contact_id) inner join addressbook "
						+ "using(add_id) where date between '%s' and '%s'",
				Date.valueOf(start), Date.valueOf(end));
		return this.getContactData(sql);
	}

	/**
	 * UC19: Retrieving data for the city and state
	 * 
	 * @param city
	 * @param state
	 * @return
	 * @throws DatabaseException
	 */
	public List<Contact> getContactForCityAndState(String city, String state) throws DatabaseException {
		String sql = String.format("select * from contact_table where city = 'Akola' order by fname,lname;", city,
				state);
		return this.getContactData(sql);
	}

	/**
	 * UC20: Inserting data into the tables in a single transaction Refactored
	 * the query according to new table structure
	 * 
	 * @param fname
	 * @param lname
	 * @param address
	 * @param zip
	 * @param city
	 * @param state
	 * @param phone
	 * @param email
	 * @param date
	 * @param addName
	 * @param type
	 * @return
	 * @throws com.capgemini.addressbookdb.DatabaseException
	 * @throws SQLException
	 */
	public Contact addContact(String fname, String lname, String address, long zip, String city, String state,
			long phone, String email, LocalDate date, int addId) throws DatabaseException, SQLException {
		int contactId = -1;
		Connection connection = null;
		Contact contact = null;
		try {
			connection = this.getConnection();
			connection.setAutoCommit(false);
		} catch (SQLException e) {
			throw new DatabaseException("Unable to establish connection with database");
		}
		try (Statement statement = connection.createStatement()) {
			String sql = String.format(
					"INSERT INTO contact_table (fname, lname, address,zip,city,state,phone,email,date) "
							+ "VALUES ('%s','%s','%s','%s','%s','%s','%s','%s','%s')",
					fname, lname, address, zip, city, state, phone, email, date);
			int rowAffected = statement.executeUpdate(sql, statement.RETURN_GENERATED_KEYS);
			if (rowAffected == 1) {
				ResultSet resultSet = statement.getGeneratedKeys();
				if (resultSet.next())
					contactId = resultSet.getInt(1);
			}
		} catch (SQLException e) {
			try {
				connection.rollback();
			} catch (SQLException exception) {
				exception.printStackTrace();
			}
			throw new DatabaseException("Unable to add new contact");
		}
		try (Statement statement = connection.createStatement()) {
			String sql = String.format("INSERT INTO addressbookjoin (contact_id, add_id) " + "VALUES ('%s','%s')",
					contactId, addId);
			int rowAffected = statement.executeUpdate(sql);
			if (rowAffected == 1) {
				contact = new Contact(contactId, fname, lname, address, city, state, zip, phone, email, date);
			}
		} catch (SQLException e) {
			try {
				connection.rollback();
			} catch (SQLException exception) {
				exception.printStackTrace();
			}
			throw new DatabaseException("Unable to add addressBook details");
		}
		try {
			connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
		return contact;
	}
}