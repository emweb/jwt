package eu.webtoolkit.jwt.examples.planner.calendar;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.EntityManager;

import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.WDate;
import eu.webtoolkit.jwt.WDialog;
import eu.webtoolkit.jwt.WLineEdit;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WRegExpValidator;
import eu.webtoolkit.jwt.WTemplate;
import eu.webtoolkit.jwt.WTextArea;
import eu.webtoolkit.jwt.WValidator;
import eu.webtoolkit.jwt.examples.planner.data.Entry;
import eu.webtoolkit.jwt.examples.planner.jpa.JpaUtil;

public class EntryDialog extends WDialog {
	public static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
	
	private WTextArea description;
	
	//a regular expression validator, 
	//validating for timestamps between 00:00-23:59 
	private static WValidator timeValidator = 
		new WRegExpValidator("^([0-1][0-9]|[2][0-3]):([0-5][0-9])$");
	
	public EntryDialog(final CalendarCell cell) {
		super(tr("calendar.entry.title").arg(cell.getDate().toString("EEE, d MMM yyyy")));
		
		//constructing a WTemplate, 
		//using the dialog's contents as parent
		WTemplate t = new WTemplate(this.getContents());
		
		//set the template's text, fetched from the calendar.xml resource bundle
		t.setTemplateText(tr("calendar.entry"));
		
		//construct a summary line edit
		final WLineEdit summary = new WLineEdit();
		//set a validator, to invalidate an empty text
		summary.setValidator(new WValidator());
		//bind the line edit to the template
		t.bindWidget("summary", summary);
		
		//construct a start line edit
		final WLineEdit start = new WLineEdit();
		//limit the line edit's width
		start.setTextSize(5);
		//set the timeValidator 
		start.setValidator(timeValidator);
		//bind the line edit to the template
		t.bindWidget("start", start);
		
		//construct a stop line edit, 
		//and use the same initialization as for the start line edit
		final WLineEdit stop = new WLineEdit();
		stop.setTextSize(5);
		stop.setValidator(timeValidator);
		t.bindWidget("stop", stop);
		
		//construct a description text are
		description = new WTextArea();
		//bind the text are to the template
		t.bindWidget("description", description);
		
		//construct a TimeSuggestions suggestion popup,
		//and connect it to the start and stop line edits
		TimeSuggestionPopup suggestions = new TimeSuggestionPopup(getContents());
		suggestions.forEdit(start);
		suggestions.forEdit(stop);
		
		//construct an OK button
		WPushButton ok = new WPushButton(tr("calendar.entry.ok"));
		t.bindWidget("ok", ok);
		ok.clicked().addListener(this, new Signal1.Listener<WMouseEvent>(){
			public void trigger(WMouseEvent arg) {
				//add a new entry and store it in the database 
				EntityManager em = JpaUtil.getEntityManager();
				Entry e = new Entry();
				e.setStart(getTimeStamp(start.getText(), cell.getDate()));
				e.setStop(getTimeStamp(stop.getText(), cell.getDate()));
				e.setSummary(summary.getText());
				e.setBody(getDescription());
				cell.getUser().addEntry(em, e);
				
				//update the cell with this new entry
				cell.update(cell.getUser(), cell.getDate());
				
				//hide the entry dialog
				EntryDialog.this.hide();
			}
		});		
		
		//construct a cancel button
		WPushButton cancel = new WPushButton(tr("calendar.entry.cancel"));
		t.bindWidget("cancel", cancel);
		cancel.clicked().addListener(this, new Signal1.Listener<WMouseEvent>(){
			public void trigger(WMouseEvent arg) {
				//hide the entry dialog
				EntryDialog.this.hide();
			}
		});
	}
	
	private Date getTimeStamp(String time, WDate day) {
		String timeStamp = day.toString("dd/MM/yyyy ");
		timeStamp += time;
		return WDate.fromString(timeStamp, "dd/MM/yyyy " + timeFormat.toPattern()).getDate();
	}
	
	private String getDescription() {
		String s = description.getText().trim();
		return s.equals("")?null:s;
	}
}
