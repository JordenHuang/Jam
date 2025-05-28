package org.jam.outputType;

import java.nio.charset.Charset;

public class StandardOutput extends IOutput {
    public void write(byte[] content) {
        System.out.print(new String(content, Charset.defaultCharset()));
    }
}
