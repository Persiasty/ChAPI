package pl.com.knopers.chapi.chatango.listeners;

import pl.com.knopers.chapi.chatango.model.RoomMessage;

public interface MessageListener
{
	public void onMessage(RoomMessage msg);
	public default void onUnknown(String cmd, String ... args) { }
}
