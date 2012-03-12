package utilities.cmd;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileHelper implements IFileHelper {
	public byte[] readAllBytes(String filePath) throws IOException {
		return Files.readAllBytes(Paths.get(filePath));
	}
	
	public String readAllAsString(String filePath) throws IOException {
		byte[] bytes = readAllBytes(filePath);
		return new String(bytes, Charset.forName("utf-8"));
	}
}
