package utilities.cmd;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileHelper implements IFileHelper {
	public byte[] readAllBytes(String filePath) throws IOException {
		return Files.readAllBytes(Paths.get(filePath));
	}
}
