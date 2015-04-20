package org.jprelude.core.util;

public enum LineSeparator {
    SYSTEM(System.lineSeparator()),
    LF("\n"),
    CR("\r"),
    CRLF("\r\n"),
    NEL("\u0025"),
    FF("\u000C"),
    LS("\u2028"),
    PS("\u2029"),    
    NONE("");
    
    final String value;
    
    private LineSeparator(final String value) {
        this.value = value;
    }
    
    public final String value() {
        return this.value;
    }
}
