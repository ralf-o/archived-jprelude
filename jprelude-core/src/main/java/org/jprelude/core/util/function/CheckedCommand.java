package org.jprelude.core.util.function;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

@FunctionalInterface
public interface CheckedCommand {
    void execute() throws Exception;
    
    default Command unchecked() {
        return () -> {
            try {
                CheckedCommand.this.execute();
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            } catch (final RuntimeException e) {
                throw e;
            } catch (final Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        };
    }
    
    static Command unchecked(final CheckedCommand command) {
        Objects.requireNonNull(command);
        
        return command.unchecked();
    }
}
