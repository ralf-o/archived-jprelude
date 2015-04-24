package org.jprelude.core.util.function;

import java.util.Objects;

@FunctionalInterface
public interface CheckedCommand<E extends Exception> {
    void execute() throws E;
    
    default Command unchecked() {
        return () -> {
            try {
                CheckedCommand.this.execute();
            } catch (final RuntimeException e) {
                throw e;
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
    
    static Command unchecked(final CheckedCommand command) {
        Objects.requireNonNull(command);
        
        return command.unchecked();
    }
}
