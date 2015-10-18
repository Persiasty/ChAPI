package pl.com.knopers.chapi.chatango.model;


public class UserProfileInfo
{
	public enum Gender
	{
		Male, Female;
	}
	
	public String Description;
	public Gender Gender;
	public int Age;
	public String Location;
	public String About; 
}
