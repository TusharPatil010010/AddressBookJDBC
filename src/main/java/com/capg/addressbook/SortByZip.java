package com.capg.addressbook;

import java.util.Comparator;

public class SortByZip implements Comparator<Contact> {
	public int compare(Contact a, Contact b) {
		return (int) (a.getZip() - b.getZip());
	}
}
