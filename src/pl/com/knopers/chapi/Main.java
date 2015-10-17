package pl.com.knopers.chapi;

import pl.com.knopers.chapi.chatango.ChatangoRoom;
import pl.com.knopers.chapi.chatango.model.Credentials;

public class Main
{
	
	public static void main(String[] args) throws Exception
	{
		Credentials uc = new Credentials("Login", "Password");
		
		ChatangoRoom _room = new ChatangoRoom("room-name", uc);
		_room.addMessageListener(msg -> System.out.println(String.format("%s: %s", msg.getAuthorName(), msg.getText())));
		_room.connect();
		
		Thread.sleep(60000);
		
		_room.close();
		System.exit(0);
	}
}
