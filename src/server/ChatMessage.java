package server;

import java.io.Serializable;

public class ChatMessage implements Serializable {
	public static final long serialVersionUID = 1;
	private String title;
	private String description;
	
	public ChatMessage(String title, String description) {
		this.title = title;
		this.description = description;
	}
	public String getTitle() {
		return title;
	}
	public String getDescription() {
		return description;
	}
}