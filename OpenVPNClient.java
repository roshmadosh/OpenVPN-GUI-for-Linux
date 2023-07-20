import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.nio.file.Paths;
import java.time.LocalDate;

public class OpenVPNClient extends JFrame {
	private final JComboBox<String> configDropDown;
	private final JTextField usernameField;
	private final JTextField passwordField;
	private final JButton connectButton;
	private final JButton disconnectButton;
	private final ClientLogger clientLogger;

	public OpenVPNClient() {
		super("OpenVPN GUI");
		this.clientLogger = new ClientLogger(Paths.get("logs", LocalDate.now().toString()));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new FlowLayout());
		configDropDown = new JComboBox<>();
		usernameField = new JTextField(10);
		passwordField = new JPasswordField(10);
		connectButton = new JButton("Connect");
		disconnectButton = new JButton("Disconnect");
		disconnectButton.setEnabled(false);

        connectButton.addActionListener(actionEvent -> connectToVpn(usernameField.getText()));

		add(new JLabel("Select Config:"));
		add(configDropDown);
		add(new JLabel("Username:"));
		add(usernameField);
		add(new JLabel("Password:"));
		add(passwordField);
		add(connectButton);
		add(disconnectButton);

		loadConfigurations();

		// all element sizes are set to fit-content
		pack();
		setVisible(true);
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
					configDropDown.addItem(line.substring(0, line.indexOf(" ")));
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

