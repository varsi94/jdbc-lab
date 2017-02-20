package helpers;

public interface IDalWrapper {
	String search(String keyword);
	
	void connect(String username, String password);
	
	String getStatistics();
	
	void disconnect();
}
