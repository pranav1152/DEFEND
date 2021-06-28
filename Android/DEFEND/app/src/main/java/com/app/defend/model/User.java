package com.app.defend.model;

import java.util.ArrayList;
import java.util.List;

public class User {

	String UID, name, phoneNo, publicKey;
	int reportCount;
	List<String> chats;

	public String getUID() {
		return UID;
	}

	public void setUID(String UID) {
		this.UID = UID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhoneNo() {
		return phoneNo;
	}

	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public int getReportCount() {
		return reportCount;
	}

	public void setReportCount(int reportCount) {
		this.reportCount = reportCount;
	}

	public List<String> getChats() { return chats; }

	public void setChats(ArrayList<String> chats) {this.chats = chats; }
}