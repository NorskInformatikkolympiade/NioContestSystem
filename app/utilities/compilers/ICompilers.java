package utilities.compilers;

import models.Language;
import models.compilers.CompileResult;

public interface ICompilers {
	CompileResult compile(Language language, String source, String folder, String fileName);
}
