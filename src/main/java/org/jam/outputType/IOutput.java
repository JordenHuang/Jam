package org.jam.outputType;

public abstract class IOutput {
    public abstract void write(byte[] content);
    void close() {}
}
