package org.jprelude.experimental.webui.widget

import com.vaadin.server.FontAwesome
import com.vaadin.ui._
import org.jprelude.experimental.webui.data.PagingPosition
import org.jprelude.experimental.webui.widget.Pagination.ViewType


object Pagination {

  object ViewType extends Enumeration {
    val Paginator, PageSizeSelector, PageInfo = Value
  }
}

class Pagination extends Widget {
  private var propViewType = ViewType.Paginator
  private var propPosition: PagingPosition = PagingPosition.Undefined()

  def viewType = this.propViewType

  def viewType_= (viewType: ViewType.Value) = {
    require(viewType != null)

    this.propViewType = viewType
    this.refresh()
  }

  def position = this.propPosition

  def position_= (position: PagingPosition) = {
    require(position != null)

    this.propPosition = position
    this.refresh()
  }

  override protected def render(): Component = {
    val ret = new HorizontalLayout
    ret setSpacing true

    this.render(ret)
    ret
  }

  private def refresh() = {
    if (this.isRendered) {
      this.render(this.getComponent.asInstanceOf[HorizontalLayout])
    }
  }

  private def render(container: HorizontalLayout) = {
    assert(container != null)

    this.viewType match {
      case ViewType.Paginator => this.renderPaginator(container)
      case ViewType.PageSizeSelector => this.renderPageSizeSelector(container)
      case ViewType.PageInfo => this.renderPageInfo(container)
    }
  }

  private def renderPaginator(container: HorizontalLayout): Unit = {
    assert(container != null)

    container.removeAllComponents()

    this.position match {
      case PagingPosition.Undefined() => {}

      case position: PagingPosition.Position => {
        val content = new GridLayout(3, 1)
        content.setWidth("100%")
        // content.setMargin(true)

        val hbox1 = new HorizontalLayout
        hbox1.setSpacing(true)
        content addComponent hbox1

        val hbox2 = new HorizontalLayout
        hbox2.setSpacing(true)
        content addComponent hbox2

        val hbox3 = new HorizontalLayout
        hbox3.setWidth("100%")
        hbox3.setSpacing(true)
        content addComponent hbox3

        val btnFirst = new Button
        btnFirst setIconAlternateText "First"
        btnFirst addStyleName "small"
        btnFirst setIcon FontAwesome.ANGLE_DOUBLE_LEFT
        btnFirst setEnabled !position.isFirstPage

        val btnPrevious = new Button
        btnPrevious setIconAlternateText "Previous"
        btnPrevious addStyleName "small"
        btnPrevious setIcon FontAwesome.ANGLE_LEFT
        btnPrevious setEnabled !position.isFirstPage

        val labPage = new Label("Page")
        val labPagesTotal = new Label(" of " + position.totalPageCount)
        // content addComponent labPage


        val txtPage = new TextField()
        txtPage addStyleName "small"
        txtPage.setWidth("6em")
        txtPage.setValue(String.valueOf(position.pageIdx + 1))

        val btnNext = new Button
        btnNext setIconAlternateText "Next"
        btnNext addStyleName "small"
        btnNext setIcon FontAwesome.ANGLE_RIGHT
        btnNext setEnabled !position.isLastPage

        val btnLast = new Button
        btnFirst setIconAlternateText "Last"
        btnLast addStyleName "small"
        btnLast setIcon FontAwesome.ANGLE_DOUBLE_RIGHT
        btnLast setEnabled !position.isLastPage


        val group1 = new CssLayout;

        group1 addStyleName "v-component-group";
        hbox1 addComponent group1;
        group1 addComponent btnFirst
        group1 addComponent btnPrevious

        //content setSpacing true


        hbox1 addComponent labPage
        hbox1 addComponent txtPage
        hbox1 addComponent labPagesTotal

        val group2 = new CssLayout;

        group2 addStyleName "v-component-group";
        hbox1 addComponent group2;
        group2 addComponent btnNext
        group2 addComponent btnLast

        //content.setMargin(new MarginInfo(true, true, true, true));
        hbox1.setComponentAlignment(labPagesTotal, Alignment.MIDDLE_CENTER)
        hbox1.setComponentAlignment(labPage, Alignment.MIDDLE_CENTER)
        hbox1.setComponentAlignment(txtPage, Alignment.MIDDLE_CENTER)
        hbox1.setComponentAlignment(group1, Alignment.MIDDLE_CENTER)
        hbox1.setComponentAlignment(group2, Alignment.MIDDLE_CENTER)

        container addComponent hbox1
      }
    }

  }

  private def renderPageSizeSelector(container: HorizontalLayout) = {
    assert(container != null)

    container.removeAllComponents()

    val combo = new ComboBox();
    combo addStyleName "small"
    combo.addItem("10")
    combo.addItem("25")
    combo.addItem("50")
    combo.addItem("100")
    combo.addItem("250")
    combo.addItem("500")
    combo.setTextInputAllowed(false)
    combo.setNullSelectionAllowed(false)
    combo.select("25")
    combo.setWidth("5.5em")
    container  addComponent new Label("Items per page")
    container addComponent combo

    container.setComponentAlignment(container getComponent 0 , Alignment.MIDDLE_CENTER)
    container.setComponentAlignment(container getComponent 0, Alignment.MIDDLE_CENTER)
  }

  private def renderPageInfo(container: HorizontalLayout) = {
    assert(container != null)

    container.removeAllComponents()

    this.position match {
      case position: PagingPosition.Position => {
        val first = position.offset + 1
        val last = Math.max(position.offset + position.pageSize, position.totalItemCount)
        val total = position.totalItemCount
        container.addComponent(new Label(s"Items $first - $last of $total"))
      }
      case _ => {}
    }
  }
}
