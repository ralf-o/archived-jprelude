package org.jprelude.experimental.webui.widget

import com.vaadin.server.FontAwesome
import com.vaadin.shared.ui.label.ContentMode
import com.vaadin.ui.Alignment
import com.vaadin.ui.Button
import com.vaadin.ui.CssLayout
import com.vaadin.ui.FormLayout
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.Label
import com.vaadin.ui.MenuBar
import com.vaadin.ui.TextField
import com.vaadin.ui.themes.ValoTheme

class ToolBar(
    
) extends Widget {
    def render() = {
      val content =  new HorizontalLayout
      content setWidth "100%"
      var hboxb = new HorizontalLayout
      hboxb setSpacing true
      content addComponent hboxb
      
       var headline = new Label()
      headline.setValue("<label class=\"v-label-h1\" style=\"margin-right: 2em\">Products</label>")
      headline.setContentMode(ContentMode.HTML)
      headline.setWidthUndefined()
      hboxb addComponent headline
      hboxb.setComponentAlignment(headline, Alignment.MIDDLE_LEFT)
        
      val hbox1 = new HorizontalLayout
      hbox1.setWidth("100%")
      hbox1 setSpacing true
      
      val hbox2 = new HorizontalLayout
      
      hboxb addComponent hbox1
      content addComponent hbox2
      
      hboxb.setComponentAlignment(hbox1, Alignment.MIDDLE_LEFT)
      content.setComponentAlignment(hbox2, Alignment.MIDDLE_RIGHT)
      
       val group1= new CssLayout;
   
        group1 addStyleName "v-component-group";
        hbox1 addComponent group1;
        
       
        val btnNew = new Button("New")
        btnNew.addStyleName("smalls")
        btnNew.setIcon(FontAwesome.FILE_O)
        group1 addComponent btnNew
        
        val btnEdit = new Button("Edit")
        btnEdit.addStyleName("smalls")
        btnEdit.setIcon(FontAwesome.EDIT)
        group1 addComponent btnEdit
        
        val btnDelete = new Button("Delete")
        btnDelete.addStyleName("smalls")
        btnDelete.setIcon(FontAwesome.TRASH_O)
        group1 addComponent btnDelete
        
        val split = new MenuBar();
        val dropdown = split.addItem("Export", null);
        dropdown setIcon FontAwesome.DOWNLOAD
        
        val submenu1 = dropdown.addItem("Export selected", null)
        submenu1.addItem("Export filtered to XML", null);
        submenu1.addItem("Export filtered to CSV", null);
        submenu1.addItem("Export filtered to ODT Spreadsheet", null);
        submenu1.addItem("Export filtered to Excel Spreadsheet", null);

        val submenu2 = dropdown.addItem("Export all", null)
        submenu2.addItem("Export all to XML", null);
        submenu2.addItem("Export all to CSV", null);
        submenu2.addItem("Export all to ODT Spreadsheet", null);
        submenu2.addItem("Export all to Excel Spreadsheet", null);

        hbox1 addComponent split

        val group = new CssLayout();
        group.addStyleName("v-component-group");
        hbox2.addComponent(group);
        
           val filter = new TextField
      filter.setInputPrompt("Search");
        filter.setIcon(FontAwesome.SEARCH);
        filter.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        group.addComponent(filter)
     
        val btnGo = new Button
        btnGo setIcon FontAwesome.ARROW_RIGHT
        group addComponent btnGo;
        
        hbox2.setComponentAlignment(group, Alignment.MIDDLE_RIGHT)

      
      
      content
    }
}