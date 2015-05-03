package org.jprelude.webui.widget;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jprelude.webui.data.PagingPosition;

import rx.Observable;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;

public class Paginator extends BaseWidget {
	private final Observable<PagingPosition> positionEvents;
	private final ViewType viewType;
	private final Consumer<Integer> onMoveToPage;
	private PagingPosition position;

	private Paginator(final Builder builder) {
		this.viewType = builder.viewType;
		this.positionEvents = builder.positionEvents;
		this.onMoveToPage = builder.onMoveToPage;
		this.position = new PagingPosition(-1, -1, -1);
	}

    @Override
    protected Component render() {
        return this.viewType.render(this);
    }

    public static enum ViewType {
        PAGINATION(Paginator::renderViewTypePagination),
        PAGE_SIZE_SELECTOR(Paginator::renderViewTypePagination),
        POSITION_INFO(Paginator::renderViewTypePagination);

        private final Function<Paginator, Component> renderer;

        private ViewType(final Function<Paginator, Component> renderer) {
        	assert renderer != null;
        	this.renderer = renderer;
        }

        private Component render(final Paginator paginator) {
        	assert paginator != null;
           	return this.renderer.apply(paginator);
        }
    }

    private void moveToPage(final int pageIdx) {
    	System.out.println(pageIdx);
    	this.onMoveToPage.accept(pageIdx);
    	System.out.println("new: " + this.position.getPageIdx());
    }

    private void moveToFirst() {
    	this.moveToPage(0);
    }

    private void moveToLast() {
    	final int pageSize = this.position.getPageSize();
    	final int totalItemCount = this.position.getTotalItemCount();

    	if (pageSize > 0 && totalItemCount > 0) {
    		this.moveToPage((totalItemCount - 1) / pageSize);
    	}
    }

    private void moveToPrevious() {
    	final int pageIdx = this.position.getPageIdx();
    	this.moveToPage(Math.max(pageIdx - 1, 0));
    }

    private void moveToNext() {
    	final int pageIdx = this.position.getPageIdx();
    	final int pageSize = this.position.getPageSize();
    	final int totalItemCount = this.position.getTotalItemCount();

    	if (pageIdx >= 0 && pageSize > 0 && totalItemCount > 0) {
    		final int maxPageIdx = ((totalItemCount - 1) / pageSize);
    		this.moveToPage(Math.min(pageIdx + 1, maxPageIdx));
    	}
    }

    private Component renderViewTypePagination() {
        final HorizontalLayout ret = new HorizontalLayout();
        ret.setSpacing(true);

        final Button moveToFirstButton = new Button();
        moveToFirstButton.setIcon(FontAwesome.ANGLE_DOUBLE_LEFT);
        moveToFirstButton.setStyleName("small");
        moveToFirstButton.addClickListener(ev -> this.moveToFirst());

        final Button moveToLastButton = new Button();
        moveToLastButton.setIcon(FontAwesome.ANGLE_DOUBLE_RIGHT);
        moveToLastButton.setStyleName("small");
        moveToLastButton.addClickListener(ev -> this.moveToLast());

        final Button moveToPreviousButton = new Button();
        moveToPreviousButton.setIcon(FontAwesome.ANGLE_LEFT);
        moveToPreviousButton.setStyleName("small");
        moveToPreviousButton.addClickListener(ev -> this.moveToPrevious());

        final Button moveToNextButton = new Button();
        moveToNextButton.setIcon(FontAwesome.ANGLE_RIGHT);
        moveToNextButton.setStyleName("small");
        moveToNextButton.addClickListener(ev -> this.moveToNext());

        final TextField pageNoTextField = new TextField();
        pageNoTextField.addStyleName("small");
        pageNoTextField.setWidth("8em");

        final Label pagePreLabel = new Label("Seite");
        final Label pagePostLabel = new Label("von 123");

        final CssLayout group1 = new CssLayout();
        group1.addStyleName("v-component-group");
        group1.addComponent(moveToFirstButton);
        group1.addComponent(moveToPreviousButton);

        final CssLayout group2 = new CssLayout();
        group2.addStyleName("v-component-group");
        group2.addComponent(moveToNextButton);
        group2.addComponent(moveToLastButton);

        ret.addComponent(group1);
        ret.addComponent(pagePreLabel);
        ret.addComponent(pageNoTextField);
        ret.addComponent(pagePostLabel);
        ret.addComponent(group2);

        for (final Component component : ret) {
            ret.setComponentAlignment(component, Alignment.MIDDLE_CENTER);
        }

        this.positionEvents.subscribe(position -> {
        	this.position = position;
            pageNoTextField.setValue(String.valueOf(position.getPageIdx() + 1));
        });

        return ret;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
    	private ViewType viewType;
    	private Observable<PagingPosition> positionEvents;
    	private Consumer<Integer> onMoveToPage;

        private Builder() {
        	this.viewType = ViewType.PAGINATION;
        	this.onMoveToPage = pageIdx -> {};
        }

        public Builder positionEvents(final Observable<PagingPosition> positionEvents) {
            Objects.requireNonNull(positionEvents);
            this.positionEvents = positionEvents;
            return this;
        }

        public Builder viewType(final ViewType viewType) {
        	Objects.requireNonNull(viewType);
        	this.viewType = viewType;
        	return this;
        }

        public Builder onMoveToPage(final Consumer<Integer> onMoveToPage) {
        	Objects.requireNonNull(this.viewType);

        	this.onMoveToPage = onMoveToPage;

        	return this;
        }

        public Paginator build() {
        	return new Paginator(this);
        }
    }
}
