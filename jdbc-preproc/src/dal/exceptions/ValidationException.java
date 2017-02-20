package dal.exceptions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class ValidationException extends Exception implements Iterable<Map.Entry<String, String>> {
	private static final long serialVersionUID = 378883886879907289L;
	private Map<String, String> fields;

	public ValidationException() {
		fields = new HashMap<>();
	}

	public void addField(String fieldName, String errorMessage) {
		fields.put(fieldName, errorMessage);
	}

	@Override
	public Iterator<Entry<String, String>> iterator() {
		return fields.entrySet().iterator();
	}
	
	public int size() {
		return fields.size();
	}
}
