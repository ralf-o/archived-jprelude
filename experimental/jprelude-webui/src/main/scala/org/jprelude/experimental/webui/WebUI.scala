package org.jprelude.experimental.webui

;

import com.vaadin.annotations.Theme
import com.vaadin.server.{FontAwesome, VaadinRequest}
import com.vaadin.ui.UI
import org.jprelude.experimental.webui.data.PageFetchResult.Success
import org.jprelude.experimental.webui.data.{PageableDatasource, PagingPosition}
import org.jprelude.experimental.webui.widget._


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

    this.setContent(new DataNavigator[String](
      dataSource = ds,
      actions = GroupedActions(
        Actions(
          GeneralAction(
            caption = "New",
            icon = Option(FontAwesome.FILE_O)),
          SingleSelectAction(
            caption = "Edit",
            icon = Option(FontAwesome.EDIT)),
          SingleSelectAction(
            caption = "Delete",
            icon = Option(FontAwesome.TRASH_O))),
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
          columns = List(
            TableColumn(
              title = "spalte1",
              render = s => "A" + s),
            TableColumn(
              title = "spalte2",
              render = s => "B" + s))),
        ColumnGroup(
          title = "Meta2",
          columns = List(
            TableColumn(
              title = "spalte3",
              render = s => "C" + s),
            TableColumn(
              title = "spalte4",
              render = s => "D" + s))))
    ).render())
  }
}
 