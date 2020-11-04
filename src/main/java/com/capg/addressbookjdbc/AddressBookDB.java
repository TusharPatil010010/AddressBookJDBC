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
	private static AddressBookDB addressBookDB;
	private static PreparedStatement contactStatement;

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

	/**
	 * UC16 : reads data from database
	 * 
	 * @return
	 * @throws DatabaseException
	 */
	public List<Contact> readData() throws DatabaseException {
		String sql = " select contact_table.contact_id, contact_table.firstname, contact_table.lastname, contact_table.address, contact_table.zip, "
				+ "contact_table.city, contact_table.state, contact_table.phone, contact_table.email, addressBook.addName, addressBook.type "
				+ "from contact_table inner join addressBook on contact_table.contact_id = addressBook.contacts_id; ";
		return this.getContactData(sql);
	}

	List<Contact> getContactData(String sql) throws DatabaseException {
		List<Contact> contactList = new ArrayList<>();
		try (Connection connection = this.getConnection()) {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			contactList = this.getContactData(resultSet);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return contactList;
	}

	/**
	 * UC17 : updates data in database
	 * 
	 * @param name
	 * @param phone
	 * @return
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	public int updatePersonsData(String name, String phone) throws DatabaseException, SQLException {
		return this.updatePersonsDataUsingStatement(name, phone);
	}

	@SuppressWarnings("static-access")
	private int updatePersonsDataUsingStatement(String name, String phone) throws DatabaseException, SQLException {
		String sql = "Update contact_table set phone = ? where firstname = ?";
		int result = 0;
		@SuppressWarnings("unused")
		List<Contact> contactList = null;
		if (this.contactStatement == null) {
			Connection connection = this.getConnection();
			contactStatement = connection.prepareStatement(sql);
		}
		try {
			contactStatement.setLong(1, Long.parseLong(phone));
			contactStatement.setString(2, name);
			result = contactStatement.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private List<Contact> getContactData(ResultSet resultSet) throws SQLException {
		List<Contact> contactList = new ArrayList<>();
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
		return contactList;
	}

	public List<Contact> getContactFromData(String name) throws DatabaseException {
		String sql = String.format("SELECT * FROM contact_table WHERE firstname = '%s'", name);
		List<Contact> contactList = new ArrayList<>();
		try (Connection connection = this.getConnection()) {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			contactList = this.getContactData(resultSet);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return contactList;
	}

	/**
	 * UC18: retrieve data for given date range
	 * 
	 * @param start
	 * @param end
	 * @return
	 * @throws DatabaseException
	 */
	public List<Contact> getEmployeeForDateRange(LocalDate start, LocalDate end) throws DatabaseException {
		String sql = String.format(
				"select contact_table.contact_id, contact_table.fname,contact_table.lname,contact_table.address,contact_table.zip, contact_table.city, contact_table.state, contact_table.phone,contact_table.email,contact_table.date, addressBook.addName, addressBook.type from contact_table inner join addressBook on contact_table.contact_id = addressBook.contacts_id where date between '%s' and '%s'",
				Date.valueOf(start), Date.valueOf(end));
		return this.getContactData(sql);
	}

	/**
	 * UC19 : retrieve contact from DB by city or state
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
	 * UC20 : adds data to database
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
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	public Contact addContact(String fname, String lname, String address, String zip, String city, String state,
			String phone, String email, LocalDate date, String addName, String type)
			throws DatabaseException, SQLException {
		int contactId = -1;
		Connection connection = null;
		Contact contact = null;
		try {
			connection = this.getConnection();
			connection.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try (Statement statement = connection.createStatement()) {
			String sql = String.format(
					"INSERT INTO contact_table (fname, lname, address,zip,city,state,phone,email,date) "
							+ "VALUES ('%s','%s','%s','%s','%s','%s','%s','%s','%s')",
					fname, lname, address, Long.parseLong(zip), city, state, Long.parseLong(phone), email, date);
			@SuppressWarnings("static-access")
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
			String sql = String.format("INSERT INTO addressBook (contact_id,addName,type) " + "VALUES ('%s','%s','%s')",
					contactId, addName, type);
			int rowAffected = statement.executeUpdate(sql);
			if (rowAffected == 1) {
				contact = new Contact(fname, lname, address, city, state, Long.parseLong(zip), Long.parseLong(phone),
						email);
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
