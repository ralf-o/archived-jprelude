package org.jprelude.common.util;

public enum LineSeparator {
    SYSTEM(System.lineSeparator()),
    LF("\n"),
    CR("\r"),
    CRLF("\r\n");
    
    final String value;
    
    private LineSeparator(final String value) {
        this.value = value;
    }
    
    public final String getSeparator() {
        return this.value;
    }
}
