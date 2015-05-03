package org.jprelude.webui.widget;

import com.vaadin.ui.Component;

public abstract class BaseWidget implements Widget {
    private Component component = null;

    protected abstract Component render();

    @Override
    public final Component getComponent() {
        if (this.component != null) {
            return this.component;
        } else {
            this.component = this.render();
            return this.component;
        }
    }
}
