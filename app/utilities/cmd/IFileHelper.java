package utilities.cmd;

import java.io.IOException;

public interface IFileHelper {
	byte[] readAllBytes(String filePath) throws IOException;
	String readAllAsString(String filePath) throws IOException;
}
