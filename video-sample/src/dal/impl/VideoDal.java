package dal.impl;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import dal.ActionResult;
import dal.DataAccessLayer;
import dal.exceptions.CouldNotConnectException;
import dal.exceptions.EntityNotFoundException;
import dal.exceptions.NotConnectedException;
import dal.exceptions.ValidationException;
import model.Member;
import model.Video;

public class VideoDal implements DataAccessLayer<Video, Member> {
	private Connection connection;
	protected static final String driverName = "oracle.jdbc.driver.OracleDriver";
	protected static final String databaseUrl = "jdbc:oracle:thin:@rapid.eik.bme.hu:1521:szglab";

	private void checkConnected() throws NotConnectedException {
		if (connection == null) {
			throw new NotConnectedException();
		}
	}

	@Override
	public void connect(String username, String password) throws CouldNotConnectException, ClassNotFoundException {
		try {
			if (connection == null || !connection.isValid(30)) {
				if (connection == null) {
					// Load the specified database driver
					Class.forName(driverName);

					// Driver is for Oracle 12cR1 (certified with JDK 7 and JDK
					// 8)
					if (java.lang.System.getProperty("java.vendor").equals("Microsoft Corp.")) {
						DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
					}
				} else {
					connection.close();
				}

				// Create new connection and get metadata
				connection = DriverManager.getConnection(databaseUrl, username, password);
			}
		} catch (SQLException e) {
			throw new CouldNotConnectException();
		}
	}

	@Override
	public List<Video> search(String keyword) throws NotConnectedException {
		checkConnected();
		try {
			keyword = keyword.replace("_", "\\_").replace("%", "\\%").toLowerCase();
			PreparedStatement stmt = connection.prepareStatement(
					"SELECT videoid, title, director FROM videos WHERE LOWER(title) LIKE '%' || ? || '%' ESCAPE '\\'");
			stmt.setString(1, keyword);
			ResultSet resultSet = stmt.executeQuery();
			List<Video> result = new ArrayList<Video>();
			while (resultSet.next()) {
				Video v = new Video();
				v.setVideoId(resultSet.getInt(1));
				v.setTitle(resultSet.getString(2));
				v.setDirector(resultSet.getString(3));
				result.add(v);
			}
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public List<Member> getStatistics() throws NotConnectedException {
		checkConnected();
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement("SELECT members.NAME, members.ADDRESS FROM videos, borrows, members "
					+ "WHERE videos.videoid = borrows.video AND borrows.member = members.memberid AND "
					+ "COMMENTLINE LIKE 'Oscar%' AND "
					+ "(MONTHS_BETWEEN(borrows.DATEOFCREATION, members.DATEOFBIRTH) / 12) < 25");
			ResultSet resultSet = stmt.executeQuery();
			List<Member> members = new ArrayList<>();
			while (resultSet.next()) {
				Member m = new Member();
				m.setName(resultSet.getString(1));
				m.setAddress(resultSet.getString(2));
				members.add(m);
			}
			return members;
		} catch (SQLException e) {
			return null;
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	@Override
	public boolean commit() throws NotConnectedException {
		checkConnected();
		try {
			connection.commit();
			return true;
		} catch (SQLException e) {
			return false;
		}
	}

	@Override
	public boolean rollback() throws NotConnectedException {
		checkConnected();
		try {
			connection.rollback();
			return true;
		} catch (SQLException e) {
			return false;
		}
	}

	@Override
	public ActionResult insertOrUpdate(Video entity, Integer foreignKey) throws NotConnectedException, EntityNotFoundException {
		checkConnected();
		PreparedStatement selectStmt = null;
		PreparedStatement modifyStmt = null;
		PreparedStatement foreignStmt = null;
		ActionResult actionResult = null;
		try {
			connection.setAutoCommit(false);
			selectStmt = connection.prepareStatement("SELECT COUNT(videoid) FROM videos WHERE videoid = ?");
			selectStmt.setInt(1, entity.getVideoId());
			ResultSet result = selectStmt.executeQuery();
			result.next();
			int count = result.getInt(1);
			if (count > 0) {
				// Update
				actionResult = ActionResult.UpdateOccurred;
				modifyStmt = connection.prepareStatement("UPDATE videos "
						+ "SET title = ?, appearance = ?, director = ?, duration = ?, fee = ?, commentline = ?, type = ? "
						+ "WHERE videoid = ?");
			} else {
				// Insert
				actionResult = ActionResult.InsertOccurred;
				modifyStmt = connection.prepareStatement("INSERT INTO videos "
						+ "(title, appearance, director, duration, fee, commentline, type, videoid) "
						+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
			}

			modifyStmt.setString(1, entity.getTitle());
			modifyStmt.setDate(2, Date.valueOf(entity.getAppearance()));
			modifyStmt.setString(3, entity.getDirector());
			modifyStmt.setInt(4, entity.getDuration());
			modifyStmt.setInt(5, entity.getFee());
			modifyStmt.setString(6, entity.getCommentLine());
			modifyStmt.setString(7, entity.getType());
			modifyStmt.setInt(8, entity.getVideoId());
			modifyStmt.executeUpdate();
		} catch (SQLException e) {
			return ActionResult.ErrorOccurred;
		} finally {
			try {
				if (selectStmt != null) {
					selectStmt.close();
				}

				if (modifyStmt != null) {
					modifyStmt.close();
				}
			} catch (SQLException e) {
				rollback();
				return ActionResult.ErrorOccurred;
			}
		}
		
		try {
			if (foreignKey != null) {
				foreignStmt = connection
						.prepareStatement("INSERT INTO borrows (video, member, dateofcreation) VALUES (?, ?, ?)");
				foreignStmt.setInt(1, entity.getVideoId());
				foreignStmt.setInt(2, foreignKey);
				foreignStmt.setDate(3, Date.valueOf(LocalDate.now()));
				foreignStmt.executeUpdate();
			}
			return actionResult;
		} catch (SQLException e) {
			rollback();
			throw new EntityNotFoundException();
		} finally {
			try {
				if (foreignStmt != null) {
					foreignStmt.close();
				}
			} catch (SQLException e) {
				return ActionResult.ErrorOccurred;
			}
		}
	}

	@Override
	public void validate(Video input) throws ValidationException {
		ValidationException ex = new ValidationException();
		if (input.getAppearance() != null && input.getAppearance().isBefore(LocalDate.of(1948, 3, 15))) {
			ex.addField("appearance", "Appearance must be after 1948-03-15!");
		}

		if (input.getVideoId() == null) {
			ex.addField("videoid", "Video id is mandatory!");
		}

		if (input.getDuration() == null || input.getDuration() < 15 || input.getDuration() > 280) {
			ex.addField("duration", "Duration is mandatory and must be between 15 and 280 minutes!");
		}

		if (input.getFee() == null || input.getFee() < 50) {
			ex.addField("fee", "Fee is mandatory and must be greater than 50!");
		}

		if (input.getTitle() == null || "".equals(input.getTitle())) {
			ex.addField("title", "Title is mandatory!");
		}

		if (input.getDirector() == null || "".equals(input.getDirector())) {
			ex.addField("director", "Director is mandatory!");
		}

		if (input.getType() == null || "".equals(input.getType())) {
			ex.addField("type", "Type is mandatory!");
		}

		if (ex.size() > 0) {
			throw ex;
		}
	}

	@Override
	public boolean setAutoCommit(boolean value) throws NotConnectedException {
		checkConnected();
		try {
			connection.setAutoCommit(value);
			return true;
		} catch (SQLException e) {
			return false;
		}
	}
}
