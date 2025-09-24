package com.example.cloudlabs.dto;

import lombok.Data;

import java.io.InputStream;
import java.util.function.Supplier;

@Data
public class StreamSegment {
    private final long start;
    private final long end;
    private final long total;
    private final long contentLength;
    private final String contentType;
    private final boolean partial;
    private final Supplier<InputStream> inputStreamSupplier;

    public StreamSegment(long start, long end, long total, String contentType, boolean partial, Supplier<InputStream> inputStreamSupplier) {
        this.start = start;
        this.end = end;
        this.total = total;
        this.contentLength = end - start + 1;
        this.contentType = contentType;
        this.partial = partial;
        this.inputStreamSupplier = inputStreamSupplier;
    }

    public InputStream openStream() { return inputStreamSupplier.get(); }
}
