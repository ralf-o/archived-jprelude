package org.jprelude.webui.base;

import org.jprelude.webui.data.PagingPosition;
import org.jprelude.webui.widget.Paginator;

import rx.subjects.BehaviorSubject;

import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Theme("webui")
public class WebUI extends UI {
    @Override
    protected void init(final VaadinRequest request) {
        // Create the content root layout for the UI
        final VerticalLayout content = new VerticalLayout();
        this.setContent(content);

        final BehaviorSubject<PagingPosition> subj =
        		BehaviorSubject.create(new PagingPosition(1,25, 100));

        // Display the greeting
        content.addComponent(new Label("Hello World!"));
        content.addComponent(Paginator.builder()
        		.positionEvents(subj)
        		.onMoveToPage(n -> subj.onNext(new PagingPosition(n, 25, 100)))
        		.build().getComponent());
        // Have a clickable button
        content.addComponent(new Button("Push Me!",
            new ClickListener() {
                @Override
                public void buttonClick(final ClickEvent e) {
                    Notification.show("Pushed!");
                }
            }));
    }
}
