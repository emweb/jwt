package eu.webtoolkit.jwt.examples.planner.calendar;

import java.util.List;

import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WDate;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.examples.planner.data.Entry;
import eu.webtoolkit.jwt.examples.planner.data.UserAccount;
import eu.webtoolkit.jwt.examples.planner.jpa.JpaUtil;

public class CalendarCell extends WContainerWidget {
	private WDate date;
	private UserAccount user;
	
	public CalendarCell() {
		//resize the cell
		this.resize(100, 120);
		
		//when clicking the cell show the EntryDialog, 
		//which allows the user to add new entries
		clicked().addListener(this, new Signal1.Listener<WMouseEvent>(){
			public void trigger(WMouseEvent arg) {
				EntryDialog ed = new EntryDialog(CalendarCell.this);
				ed.show();
			}
		});

		//set the css style class
		this.setStyleClass("cell");
		
		//set a tooltip,
		//explaining the user new entries can be added by clicking the cell
		this.setToolTip(tr("calendar.cell.tooltip"));
	}
	
	public void update(UserAccount user, WDate date) {
		this.date = date;
		this.user = user;
		
		//clear all widgets from this container
		clear();
		
		//create the header
		String day = date.getDay() + "";
		if (date.getDay() == 1)
			day += " " + WDate.getLongMonthName(date.getMonth());
		WText header = new WText(day);
		header.setStyleClass("cell-header");
		addWidget(header);

		//fetch all entries from the database
		List<Entry> entries = user.getEntriesInRange(JpaUtil.getEntityManager(), date.getDate(), date.addDays(1).getDate());
		
		//add the first 4 entries to the database
		final int maxEntries = 4;
		for (int i = 0; i < entries.size(); i++) {
			if (i == maxEntries) {
				WText extra = new WText(tr("calendar.cell.extra").arg(entries.size() - maxEntries));
				extra.setStyleClass("cell-extra");
				addWidget(extra);
				
				//prevents the default browser action
				extra.clicked().preventDefaultAction(true);
				//clicking this label will open a dialog
				//providing an overview of all entries for the cell's date
				extra.clicked().addListener(this, new Signal1.Listener<WMouseEvent>(){
					public void trigger(WMouseEvent me) {
						AllEntriesDialog dialog = new AllEntriesDialog(CalendarCell.this);
						dialog.show();
					}
				});
				
				break;
			}
			//add the entry's summary with start and end date to the cell
			Entry e = entries.get(i);
			addWidget(new WText(EntryDialog.timeFormat.format(e.getStart()) + "-" + EntryDialog.timeFormat.format(e.getStop()) + ": " + e.getSummary()));
		}
	}
	
	public WDate getDate() {
		return date;
	}

	public UserAccount getUser() {
		return user;
	}
}
