import javax.swing.*;

public class VpnGUIApplication {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new OpenVPNClient();
            }
        });
    }
}
