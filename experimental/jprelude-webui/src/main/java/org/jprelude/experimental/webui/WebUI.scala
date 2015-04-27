package org.jprelude.experimental.webui;

import com.vaadin.annotations.Theme
import com.vaadin.ui.UI
import com.vaadin.ui.VerticalLayout
import com.vaadin.server.VaadinRequest
import com.vaadin.ui.Label
import com.vaadin.ui.Notification
import com.vaadin.ui.Button
import com.vaadin.ui.Button.ClickEvent
import java.util.Date
import com.vaadin.ui.Button.ClickListener
import com.vaadin.server.VaadinServlet
import com.vaadin.annotations.VaadinServletConfiguration
import javax.servlet.annotation.WebServlet

@Theme("webui")
class WebUI extends UI {
  override def init(request: VaadinRequest) = {
    val content: VerticalLayout = new VerticalLayout
    setContent(content)

    val label: Label = new Label("Web-UI")
    content addComponent label

    // Handle user interaction
    content addComponent new Button("Click Me!",
      new ClickListener  {
        override def buttonClick(event: ClickEvent) =
          Notification.show("The time is " + new Date)
      })
  }
}