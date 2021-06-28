package com.app.defend.model;

public class Transaction {
	String UID, fromUID, toUID, msgUID;
	boolean sent, delivered, read;


	public String getUID() {
		return UID;
	}

	public void setUID(String UID) {
		this.UID = UID;
	}

	public String getFromUID() {
		return fromUID;
	}

	public void setFromUID(String fromUID) {
		this.fromUID = fromUID;
	}

	public String getToUID() {
		return toUID;
	}

	public void setToUID(String toUID) {
		this.toUID = toUID;
	}

	public String getMsgUID() {
		return msgUID;
	}

	public void setMsgUID(String msgUID) {
		this.msgUID = msgUID;
	}

	public boolean isSent() {
		return sent;
	}

	public void setSent(boolean sent) {
		this.sent = sent;
	}

	public boolean isDelivered() {
		return delivered;
	}

	public void setDelivered(boolean delivered) {
		this.delivered = delivered;
	}

	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}
}