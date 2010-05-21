package eu.webtoolkit.jwt.examples.planner.calendar;

import eu.webtoolkit.jwt.SelectionMode;
import eu.webtoolkit.jwt.WCalendar;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WDate;
import eu.webtoolkit.jwt.WWidget;
import eu.webtoolkit.jwt.examples.planner.data.UserAccount;

public class PlannerCalendar extends WCalendar {
	private UserAccount user;
	
	public PlannerCalendar(WContainerWidget parent, UserAccount user) {
		super(parent);
		
		//set the css style class
		setStyleClass(this.getStyleClass() + " calendar");
		
		this.user = user;
		
		//disable the default selection 
		//(CalendarCell implements a proper selection action)
		setSelectionMode(SelectionMode.NoSelection);
	}

	@Override
	/*
	 * Override renderCell to create custom cells.
	 */
	protected WWidget renderCell(WWidget widget, WDate date) {
		//if no widget exists, create a new CalendarCell
		if (widget == null) 
			widget = new CalendarCell();
		
		//update the CalendarCell by passing the current user and date
		CalendarCell cc = (CalendarCell)widget;
		cc.update(user, date);
		
		//return the updated cell
		return cc;
	}
}
