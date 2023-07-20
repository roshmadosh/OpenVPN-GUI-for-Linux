import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

public class ClientLogger {
    private final Path logPath;

    public ClientLogger(Path logPath) {
        this.logPath = logPath;
    }

    public void write(String message) throws IOException {
        String full = LocalDateTime.now().toString().concat(" ").concat(message).concat("\n");
        Files.write(logPath, full.getBytes(), StandardOpenOption.APPEND);
    }

    public void writeError(String message) {
        try {
            String full = "ERROR".concat(LocalDateTime.now().toString().concat(" ").concat(message).concat("\n"));
            Files.write(logPath, full.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
