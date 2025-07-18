package org.jam.outputType;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileOutput extends IOutput {
    private BufferedWriter writer;

    public FileOutput(String path) throws IOException {
        writer = Files.newBufferedWriter(Paths.get(path), StandardCharsets.UTF_8);
    }

    public void write(byte[] content) {
        try {
            if (content == null) {
                System.err.println("Content is null, writing empty content to file");
                return;
            }
            writer.write(new String(content, Charset.defaultCharset()));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    @Override
    void close() {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
