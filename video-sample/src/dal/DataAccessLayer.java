package dal;

import java.util.List;

import dal.exceptions.CouldNotConnectException;
import dal.exceptions.EntityNotFoundException;
import dal.exceptions.NotConnectedException;
import dal.exceptions.ValidationException;

public interface DataAccessLayer<T1, T2> {
	void connect(String username, String password) throws CouldNotConnectException, ClassNotFoundException;
	
	List<T1> search(String keyword) throws NotConnectedException;
	
	List<T2> getStatistics() throws NotConnectedException;
	
	ActionResult insertOrUpdate(T1 entity, Integer foreignKey) throws NotConnectedException, EntityNotFoundException;
	
	boolean commit() throws NotConnectedException;
	
	boolean rollback() throws NotConnectedException;
	
	void validate(T1 input) throws ValidationException;
	
	boolean setAutoCommit(boolean value) throws NotConnectedException;
}
