package jsak.io.file;

import jsak.io.bytes.Bytes;

import java.nio.file.Path;

public interface FileContentListener
{
	void onFileContentChange(final Path path, final Bytes content);
}
