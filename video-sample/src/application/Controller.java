package application;

import java.net.URL;
import java.util.List;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import dal.ActionResult;
import dal.DataAccessLayer;
import dal.exceptions.CouldNotConnectException;
import dal.exceptions.EntityNotFoundException;
import dal.exceptions.NotConnectedException;
import dal.exceptions.ValidationException;
import dal.impl.VideoDal;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import model.Member;
import model.Video;

public class Controller implements Initializable {
	private DataAccessLayer<Video, Member> dal;
	@FXML
	TextField usernameField;
	@FXML
	HBox connectionLayout;
	@FXML
	VBox rootLayout;
	@FXML
	TextField passwordField;
	@FXML
	Button connectButton;
	@FXML
	Label connectionStateLabel;
	@FXML
	Tab searchTab;
	@FXML
	TableView<Video> searchTable;
	@FXML
	Tab editTab;
	@FXML
	TextField searchTextField;
	@FXML
	TableColumn<Video, Integer> idColumn;
	@FXML
	TableColumn<Video, String> titleColumn;
	@FXML
	TableColumn<Video, String> directorColumn;
	@FXML
	TableColumn<Member, String> memberNameColumn;
	@FXML
	TableColumn<Member, String> memberAddressColumn;
	@FXML
	TableView<Member> statisticsTable;
	@FXML
	TextField titleTextField;
	@FXML
	TextField idTextField;
	@FXML
	TextField commentTextField;
	@FXML
	ComboBox<ComboBoxItem> typeComboBox;
	@FXML
	TextField durationTextField;
	@FXML
	TextField directorTextField;
	@FXML
	DatePicker appearanceDatePicker;
	@FXML
	TextField feeTextField;
	@FXML TextField memberIdTextField;
	@FXML Button commitBtn;

	public Controller() {
		dal = new VideoDal();
	}

	private Integer strToInt(String s) {
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private void showNotConnected() {
		new Alert(AlertType.ERROR, "You are not connected to the database!", ButtonType.OK).show();
	}

	@FXML
	public void connectEventHandler(ActionEvent event) {
		String username = usernameField.getText();
		String password = passwordField.getText();

		try {
			dal.connect(username, password);
			connectionStateLabel.setText("Connection created!");
			connectionStateLabel.setTextFill(Paint.valueOf("green"));
		} catch (ClassNotFoundException e) {
			connectionStateLabel.setText("JDBC driver not found!");
			connectionStateLabel.setTextFill(Paint.valueOf("red"));
		} catch (CouldNotConnectException e) {
			connectionStateLabel.setText("Could not estabilish connection to the server!");
			connectionStateLabel.setTextFill(Paint.valueOf("red"));
		}
	}

	@FXML
	public void searchEventHandler() {
		try {
			String keyword = this.searchTextField.getText();
			List<Video> result;
			result = dal.search(keyword);
			ObservableList<Video> data = FXCollections.observableArrayList(result);
			searchTable.setItems(data);
		} catch (NotConnectedException e) {
			showNotConnected();
		}
	}

	@FXML
	public void commitEventHandler() {
		try {
			dal.commit();
			commitBtn.setDisable(true);
		} catch (NotConnectedException e) {
			showNotConnected();
		}
	}

	@FXML
	public void editEventHandler() {
		Video v = new Video();
		v.setVideoId(strToInt(idTextField.getText()));
		v.setTitle(titleTextField.getText());
		v.setDirector(directorTextField.getText());
		v.setCommentLine(commentTextField.getText());
		v.setAppearance(appearanceDatePicker.getValue());
		v.setFee(strToInt(feeTextField.getText()));
		v.setDuration(strToInt(durationTextField.getText()));
		v.setType(typeComboBox.getValue().getValue());
		ActionResult result;
		try {
			dal.validate(v);

			result = dal.insertOrUpdate(v, strToInt(memberIdTextField.getText()));
			switch (result) {
			case ErrorOccurred:
				new Alert(AlertType.ERROR, "Error occured during save!", ButtonType.OK).show();
				break;
			case InsertOccurred:
				commitBtn.setDisable(false);
				new Alert(AlertType.INFORMATION, "New video added!", ButtonType.OK).show();
				break;
			case UpdateOccurred:
				commitBtn.setDisable(false);
				new Alert(AlertType.INFORMATION, "Video updated!", ButtonType.OK).show();
				break;
			}
		} catch (NotConnectedException e) {
			showNotConnected();
		} catch (ValidationException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("Validation error(s):\n");
			for (Entry<String, String> entry : e) {
				sb.append(entry.getKey() + ": " + entry.getValue() + "\n");
			}
			new Alert(AlertType.ERROR, sb.toString(), ButtonType.OK).show();
		} catch (EntityNotFoundException e) {
			new Alert(AlertType.ERROR, "Member not found!", ButtonType.OK).show();
		}
	}

	@FXML
	public void statisticsEventHandler() {
		List<Member> result;
		try {
			result = dal.getStatistics();
			ObservableList<Member> data = FXCollections.observableArrayList(result);
			statisticsTable.setItems(data);
		} catch (NotConnectedException e) {
			showNotConnected();
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		idColumn.setCellValueFactory(new PropertyValueFactory<Video, Integer>("videoId"));
		titleColumn.setCellValueFactory(new PropertyValueFactory<Video, String>("title"));
		directorColumn.setCellValueFactory(new PropertyValueFactory<Video, String>("director"));

		memberNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
		memberAddressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
		ObservableList<ComboBoxItem> items = FXCollections.observableArrayList(new ComboBoxItem("DVD", "D"),
				new ComboBoxItem("Blu-ray", "B"));
		typeComboBox.setItems(items);
		typeComboBox.getSelectionModel().selectFirst();
	}

	public void disconnect() {
		try {
			dal.commit();
		} catch (NotConnectedException e) {
			//Nothing to deal with
		}
	}

}
