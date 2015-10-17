package pl.com.knopers.chapi.chatango.listeners;

import pl.com.knopers.chapi.chatango.model.ChatangoUser;

public interface JoinListener
{
	public void onJoin(ChatangoUser u);
	public void onLeave(ChatangoUser u);
}
