package cleoni.adv;

import java.util.Date;

/*
 * 1.1 - aggiunto campo ID e privatizzati campi
 * 1.0 - prima release
 *
 */
/** Represent a message sent by a CHARACTER or a Player */
public class Message {
	private String from;
	private String senderName;
	private Date date;
	private String text;
	private String id = null;

public String getId() {
	return id;
}

public String getSenderName() {
	return senderName;
}
public String getText() {
	return text;
}
protected void setId(String anId) {
	id = anId;
}
public String toString() {
	return "from: " + senderName + " (" + from + "): '" + text + "'";
}

/**
 * Message constructor comment.
 */
public Message(DimxObject sender, String toId, String aText) {
	super();
	from = sender.id;
	senderName = sender.getName();
	text = aText;
	date = new Date();
}
}