package com.capg.addressbook;

import java.time.LocalDate;
import java.util.Objects;

public class Contact {
	public String firstName;
	public String lastName;
	public String address;
	public String city;
	public String state;
	public long zip;
	public long phoneNumber;
	public String email;
	public int addId;
	public String addName;
	public LocalDate date;
	public String type;

	public Contact(String firstName, String lastName, String address, String city, String state, long zip,
			long phoneNumber, String email) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.address = address;
		this.city = city;
		this.state = state;
		this.zip = zip;
		this.phoneNumber = phoneNumber;
		this.email = email;
	}

	public Contact(String firstName, String lastName, String address, String city, String state, long zip,
			long phoneNumber, String email, LocalDate date, int addressBookId, String addName, String type) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.address = address;
		this.city = city;
		this.state = state;
		this.zip = zip;
		this.phoneNumber = phoneNumber;
		this.email = email;
		this.date = date;
		this.addId = addressBookId;
		this.addName = addName;
		this.type = type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(firstName, lastName, address, phoneNumber);
	}

	@Override
	public boolean equals(Object o) {
		boolean result = false;
		if (o == this) {
			return true;
		}
		Contact c = (Contact) o;
		if (c.firstName.equals(this.firstName) && c.lastName.equals(this.lastName)) {
			result = true;
		}
		return result;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public long getZip() {
		return zip;
	}

	public void setZip(long zip) {
		this.zip = zip;
	}

	public long getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(long phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String toString() {
		return this.getFirstName() + ", " + this.getLastName() + ", " + this.getAddress() + ", " + this.getCity() + ", "
				+ this.getState() + ", " + this.getZip() + ", " + this.getPhoneNumber() + ", " + this.getEmail() + "\n";
	}
}