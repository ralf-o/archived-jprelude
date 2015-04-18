package org.jprelude.core.io.function;

import java.io.IOException;
import org.jprelude.core.util.function.CheckedCommand;

@FunctionalInterface
public interface IOCommand extends CheckedCommand {
    @Override
    void execute() throws IOException;
}
