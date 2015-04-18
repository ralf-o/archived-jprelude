package org.jprelude.core.io.function;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import org.jprelude.core.util.function.Command;

public interface IOCommand {
    void execute() throws IOException;
    
    default Command unchecked() {
        return () -> {
            try {
                IOCommand.this.execute();
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }
    
    static Command unchecked(final IOCommand ioCommand) {
        Objects.requireNonNull(ioCommand);
        
        return ioCommand.unchecked();
    }
}
