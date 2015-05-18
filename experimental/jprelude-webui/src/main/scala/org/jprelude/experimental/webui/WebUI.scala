package org.jprelude.experimental.webui

;

import com.vaadin.annotations.Theme
import com.vaadin.server.{FontAwesome, VaadinRequest}
import com.vaadin.ui.{Notification, UI}
import org.jprelude.experimental.webui.data.PageFetchResult.Success
import org.jprelude.experimental.webui.data.{PageableDatasource, PagingPosition}
import org.jprelude.experimental.webui.widget._
import org.jprelude.experimental.webui.widget.form._


@Theme("webui")
class WebUI extends UI {
  override def init(request: VaadinRequest) = {
    val ds = new PageableDatasource[String]((pageIdx, pageSize) => {
      new Success[String](Range(0, pageSize).map(n => "X" + String.valueOf(n + pageIdx * pageSize + 1)), PagingPosition.Position(pageIdx = pageIdx, totalItemCount = 10000, pageSize = pageSize))
    })

    class Controller {
      def createNew(): Unit = {

      }

      def delete(items: Seq[String]): Unit = {

      }

      def edit(item: String): Unit = {

      }

      def whatever(item: String): Unit = {

      }

      def whatever2(item: Seq[String]): Unit = {

      }
    }

    val controller = new Controller()

    val dataNav = new DataNavigator[String](
      dataSource = ds,
      actions = GroupedActions(
        Actions(
          GeneralAction(
            caption = "New",
            icon = Option(FontAwesome.FILE_O),
            command = () => Notification.show("Clicked 'New'")),
          SingleSelectAction(
            caption = "Edit",
            icon = Option(FontAwesome.EDIT),
            command = selection =>Notification.show(selection)),
          MultiSelectAction(
            caption = "Delete",
            icon = Option(FontAwesome.TRASH_O),
            command = selection => Notification.show(selection.toString))),
        Actions(
          ActionMenu(
            caption = "Export",
            icon = Option(FontAwesome.DOWNLOAD),
            actions = Actions(
              SingleSelectAction(
                caption = "Export all"),
              ActionMenu(
                caption = "Export selected",
                actions = Actions(
                  GeneralAction(
                    caption = "X"),
                  ActionMenu(
                    caption = "Another sub menu",
                    actions = Actions(
                      SingleSelectAction(
                        caption = "Y"),
                      ActionMenu(
                        caption = "Yet another sub menu",
                        actions = Actions(
                          GeneralAction(
                            caption = "Z"))))))))))),
      columns = ColumnGroups(
        ColumnGroup(
          title = "Meta1",
          columns = Columns(
            Column(
              title = "spalte1",
              render = (s: String) => "A" + s),
            Column(
              title = "spalte2",
              render = (s: String) => "B" + s))),
        ColumnGroup(
          title = "Meta2",
          columns = Columns(
            Column(
              title = "spalte3",
              render = (s: String) => "C" + s),
            Column(
              title = "spalte4",
              render = (s: String) => "D" + s)))))

    val myForm = new DataForm(
      title = "Products",
      toolbar = Option(ToolBar(
        tools = Tools(
          Tools(
            ToolButton(
              caption = "Save"),
            ToolButton(
              caption = "Delete"),
            ToolButton(
              caption = "Cancel")),
          ToolButton(
            caption = "Export",
            menu = ToolMenuItems(
              ToolMenuCommand(
                caption = "Menu item 1"),
              ToolMenu(
                caption = "Menu item 2",
                items = ToolMenuItems(
                  ToolMenuCommand(
                    caption = "Menu item 3")))))))),
      sections = Sections(
        FieldSet(
          controls = Controls(
            TextInput(
              caption = "Product-No."),
            TextInput(
              caption = "Description"))),
        TabBox(
          pages = TabPages(
            TabPage(
              title = "Base data",
              sections = Sections(
                FieldSet(
                  controls = Controls(
                    TextInput(
                      caption = "Product-No."),
                    TextInput(
                      caption = "Description"))))),
            TabPage(
              title = "Variants")))))

    this.setContent(myForm.component)
  }
}
