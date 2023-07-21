import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Timer;
import java.util.TimerTask;


public class OpenVPNClient extends Application {
	private ComboBox<String> configDropDown;
	private TextField usernameField;
	private TextField passwordField;
	private Button connectButton;
	private Button disconnectButton;
	private ClientLogger clientLogger;
	private Label statusLabel;
	private boolean isConnected;

	@Override
	public void start(Stage stage) throws Exception {
		this.clientLogger = new ClientLogger(Paths.get("logs", LocalDate.now().toString()));
		this.configDropDown = new ComboBox<>();
		this.usernameField = new TextField();
		this.passwordField = new PasswordField();
		this.connectButton = new Button("Connect");
		this.disconnectButton = new Button("Disconnect");
		this.disconnectButton.setDisable(true);
		this.isConnected = false;

		this.statusLabel = new Label("Disconnected");
		this.statusLabel.setTextFill(Color.RED);
		loadConfigurations();

		this.connectButton.setOnAction(actionEvent -> {
			this.connectButton.setDisable(true);
			this.statusLabel.setText("Connecting...");
			this.statusLabel.setTextFill(Color.DARKORANGE);
			String username = this.usernameField.getText();

			Platform.runLater(() -> {
				connectToVpn(username);
				pingStatus(3);
			});
		});

		HBox buttonsBox = new HBox(this.connectButton, this.disconnectButton);
		// Create a layout and add components to it
		VBox root = new VBox();
		root.getChildren().addAll(
				new Label("Select Config:"), configDropDown,
				new Label("Username:"), usernameField,
				new Label("Password:"), passwordField,
				buttonsBox,
				new Label("Status:"), statusLabel
		);

		Scene scene = new Scene(root, 300, 200);
		stage.setTitle("OpenVPN GUI");
		stage.setScene(scene);
		stage.show();
	}

	private void pingStatus(int i) {
		ProcessBuilder pb = new ProcessBuilder("openvpn3", "sessions-list");

		Timer timer = new Timer();
        timer.scheduleAtFixedRate(
                new TimerTask() {
                    private int count = 0;

                    @Override
                    public void run() {
                        try {
                            Process process = pb.start();
                            InputStream inputStream = process.getInputStream();

                            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                            String line;
                            while ((line = reader.readLine()) != null) {
                                if (line.contains("connected")) {
                                    Platform.runLater(() -> {
                                        disconnectButton.setDisable(false);
                                        statusLabel.setTextFill(Color.GREEN);
                                        statusLabel.setText("Connected");
                                        isConnected = true;
                                    });
                                }
                            }

                            process.waitFor();
                            reader.close();
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }

                        count++;
						if (count >= i) {
							if (!isConnected) {
								Platform.runLater(() -> {
									statusLabel.setText("Failed");
									statusLabel.setTextFill(Color.RED);
									connectButton.setDisable(false);
								});
							}
							timer.cancel();
                        }
                    }
                },
                0,
                1000);
	}

	// reads profile configurations from the output returned from executing the command "openvpn3 configs-list" in a Bash terminal
	private void loadConfigurations() {
		try {
			ProcessBuilder pb = new ProcessBuilder("openvpn3", "configs-list")
					.redirectErrorStream(true);
			Process process = pb.start();
			InputStream is = process.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));

			String line;
			while ((line = reader.readLine()) != null) {
				if (line.contains("profile"))
					configDropDown.getItems().add(line.substring(0, line.indexOf(" ")));
			}
			process.waitFor();
			reader.close();
		} catch (IOException | InterruptedException ie) {
			ie.printStackTrace();
		}
	}

	private void connectToVpn(String username) {
		try {
			ProcessBuilder pb = new ProcessBuilder("openvpn3-autoload",
					"--directory", "/home/hiroshin/.openvpn3/autoload").redirectErrorStream(true);
			Process process = pb.start();

			InputStream stdout = process.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));

			this.clientLogger.write("Attempted login by ".concat(username));
			String line;
			while ((line = reader.readLine()) != null) {
				this.clientLogger.write(line);
			}

			process.waitFor();
			reader.close();
		} catch (IOException | InterruptedException ie) {
			this.clientLogger.writeError(ie.getMessage());
		}
	}

}

