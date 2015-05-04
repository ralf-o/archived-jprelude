package org.jprelude.experimental.webui;

import com.vaadin.annotations.Theme
import com.vaadin.server.VaadinRequest
import com.vaadin.ui.UI
import org.jprelude.experimental.webui.widget.DataNavigator


@Theme("webui")
class WebUI extends UI {
  override def init(request: VaadinRequest) = {
    this.setContent(new DataNavigator().render())
  }
}
 