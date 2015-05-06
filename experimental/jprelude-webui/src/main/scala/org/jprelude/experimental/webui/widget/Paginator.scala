package org.jprelude.experimental.webui.widget

import com.vaadin.server.FontAwesome
import com.vaadin.ui.{Alignment, Button, ComboBox, CssLayout, GridLayout, HorizontalLayout, Label, TextField}

class Paginator extends Widget {
  override def render = {
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
    
    val btnPrevious = new Button
    btnPrevious setIconAlternateText "Previous"
    btnPrevious addStyleName "small"
    btnPrevious setIcon FontAwesome.ANGLE_LEFT
    
   val labPage = new Label("Seite")
   val labPagesTotal = new Label("von 114")
   // content addComponent labPage
    
    
    val txtPage = new TextField()
    txtPage addStyleName "small"
    txtPage.setWidth("6em")
    //txtPage.setValue("1 von 14")
    
    val labOfPages = new Label("von 8")
    
    val btnNext = new Button
    btnNext setIconAlternateText "Next"
    btnNext addStyleName "small"
    btnNext setIcon FontAwesome.ANGLE_RIGHT
    
    val btnLast = new Button
    btnFirst setIconAlternateText "Last"
    btnLast addStyleName "small"
    btnLast setIcon FontAwesome.ANGLE_DOUBLE_RIGHT
    
    
    
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
  
val labPageSize = new Label("Datensaetze pro Seite")
hbox2 addComponent labPageSize
hbox2.setComponentAlignment(labPageSize, Alignment.MIDDLE_CENTER)

val labPositionInfo = new Label("Datensaetze 26 - 50 von 134")
hbox3 addComponent labPositionInfo
hbox3.setComponentAlignment(labPositionInfo, Alignment.MIDDLE_RIGHT)
labPositionInfo.setWidthUndefined()
labPositionInfo.addStyleName("right")
 
content.setComponentAlignment(hbox1, Alignment.MIDDLE_LEFT)
content.setComponentAlignment(hbox2, Alignment.MIDDLE_LEFT)
content.setComponentAlignment(hbox3, Alignment.MIDDLE_RIGHT)

val combo = new ComboBox();
        combo.addItem("10");
        combo.addItem("25");
        combo.addItem("50");
        combo.addItem("100");
        combo.addItem("250");
        combo.addItem("500");
        combo.setTextInputAllowed(false);
        combo.setNullSelectionAllowed(false);
        combo.select("25");
        combo.setWidth("5.5em")
        hbox2.addComponent(combo);
        hbox2.setComponentAlignment(combo, Alignment.MIDDLE_CENTER)
        content.setWidth("100%")
    content
  }
}