package eu.webtoolkit.jwt.examples.planner.calendar;

import java.util.List;

import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WDialog;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WTemplate;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.examples.planner.data.Entry;
import eu.webtoolkit.jwt.examples.planner.jpa.JpaUtil;

public class AllEntriesDialog extends WDialog {
	public AllEntriesDialog(CalendarCell cell) {
		super(tr("calendar.cell.all-entries.title").arg(cell.getDate().toString("EEE, d MMM yyyy")));
		
		WTemplate t = new WTemplate(tr("calendar.all-entries"), getContents());
		WContainerWidget wc = new WContainerWidget();
		t.bindWidget("entries", wc);
		
		List<Entry> entries = 
			cell.getUser().getEntriesInRange(JpaUtil.getEntityManager(), cell.getDate().getDate(), cell.getDate().addDays(1).getDate());

		for (int i = 0; i < entries.size(); i++) {
			Entry e = entries.get(i);
			wc.addWidget(new WText(EntryDialog.timeFormat.format(e.getStart()) + "-" + EntryDialog.timeFormat.format(e.getStop()) + ": " + e.getSummary()));
		}
		
		WPushButton button = new WPushButton(tr("calendar.cell.all-entries.close"));
		t.bindWidget("close", button);
		button.clicked().addListener(this, new Signal1.Listener<WMouseEvent>(){
			public void trigger(WMouseEvent arg) {
				AllEntriesDialog.this.hide();
			}
		});
	}
}
