package pl.com.knopers.chapi.engine2;

public interface Receiver
{
	void OnReceived(String cmd, String ... args);
}
