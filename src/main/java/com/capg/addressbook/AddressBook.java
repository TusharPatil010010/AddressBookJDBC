package com.capg.addressbook;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AddressBook {
	Scanner sc = new Scanner(System.in);
	public List<Contact> contactList = new ArrayList<Contact>();
	public String city;

	public AddressBook(String city) {
		this.city = city;
	}

	public List<Contact> getContactList() {
		return contactList;
	}

	public void addContact(Contact c) {
		for (int i = 0; i < contactList.size(); i++) {
			if (contactList.get(i).equals(c)) {
				System.out.println("The person already exists!!!");
				return;
			}
		}
		contactList.add(c);
	}

	public void editContact(String name) {
		String x = "";
		int i = 0;
		for (Contact c : contactList) {
			x = c.getFirstName() + " " + c.getLastName();
			if (name.equals(x)) {
				while (true) {
					System.out.println("1.Change the phone number");
					System.out.println("2.Change the Email id");
					System.out.println("3.exit");
					int choice = sc.nextInt();
					sc.nextLine();
					switch (choice) {
					case 1:
						System.out.println("Enter new phone number");
						long phone = sc.nextLong();
						sc.nextLine();
						c.setPhoneNumber(phone);
						break;
					case 2:
						System.out.println("Enter new Email id");
						String email = sc.nextLine();
						c.setEmail(email);
						break;
					case 3:
						break;
					}
					if (choice == 3) {
						break;
					}
				}

			} else {
				System.out.println("Enter correct name");
			}
			contactList.set(i, c);
			i++;
		}
	}

	public void deleteContact(String name) {
		String x = "";
		for (Contact c : contactList) {
			x = c.getFirstName() + " " + c.getLastName();
			if (name.equals(x)) {
				contactList.remove(c);
			}
		}
	}

	public void viewList() {

		for (Contact c : contactList) {
			System.out.println("First Name : " + c.getFirstName() + " Last Name : " + c.getLastName() + " Address : "
					+ c.getAddress() + " City : " + c.getCity() + " State : " + c.getState() + " ZIP : " + c.getZip()
					+ " Phone Number : " + c.getPhoneNumber() + " Email ID : " + c.getEmail() + "\n");
		}
	}
}