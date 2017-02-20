package helpers;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import dal.DataAccessLayer;
import dal.exceptions.CouldNotConnectException;
import dal.exceptions.NotConnectedException;

public class DalWrapper<T1, T2> implements IDalWrapper {
	private final DataAccessLayer<T1, T2> dal;
	private final Class<T1> class1;
	private final Class<T2> class2;
	private final String[] fieldsForTask1;
	
	private String getGetterMethodName(String field) {
		return "get" + Character.toUpperCase(field.charAt(0)) + field.substring(1);
	}

	public DalWrapper(DataAccessLayer<T1, T2> dal, Class<T1> class1, Class<T2> class2, String[] fieldsForTask1) {
		this.dal = dal;
		this.class1 = class1;
		this.class2 = class2;
		this.fieldsForTask1 = fieldsForTask1;
	}

	@Override
	public String search(String keyword) {
		try {
			StringBuilder builder = new StringBuilder();
			List<T1> result = dal.search(keyword);
			for (T1 t1 : result) {
				for (String fielName : fieldsForTask1) {
					String methodName = getGetterMethodName(fielName);
					builder.append(class1.getDeclaredMethod(methodName).invoke(t1) + ";");
				}
				builder.append("\n");
			}
			return builder.toString();
		} catch (NotConnectedException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void connect(String username, String password) {
		try {
			dal.connect(username, password);
		} catch (ClassNotFoundException | CouldNotConnectException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getStatistics() {
		try {
			StringBuilder builder = new StringBuilder();
			List<T2> result = dal.getStatistics();
			for (T2 item : result) {
				for (Field field : class2.getDeclaredFields()) {
					Method m = class2.getDeclaredMethod(getGetterMethodName(field.getName()));
					builder.append(m.invoke(item) + ";");
				}
				builder.append("\n");
			}
			return builder.toString();
		} catch (NotConnectedException | IllegalArgumentException | NoSuchMethodException | SecurityException
				| IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void disconnect() {
		dal.disconnect();
	}
}
