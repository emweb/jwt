package eu.webtoolkit.jwt.examples.planner.calendar;

import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WSuggestionPopup;

public class TimeSuggestionPopup extends WSuggestionPopup {
	private static WSuggestionPopup.Options timeOptions = new WSuggestionPopup.Options();

    static {
        timeOptions.highlightBeginTag = "<b>";
        timeOptions.highlightEndTag = "</b>";
        timeOptions.listSeparator = 0;
        timeOptions.whitespace = " \\n";
        timeOptions.wordSeparators = "0";
        timeOptions.appendReplacedText = "";
    }
    
    public TimeSuggestionPopup() {
        super(WSuggestionPopup.generateMatcherJS(timeOptions),
              WSuggestionPopup.generateReplacerJS(timeOptions));
        
        for (int i = 0; i < 24; i++) {
        	String h = String.format("%02d", i);
        	addSuggestion(h + ":00");
        	addSuggestion(h + ":30");
        }
	}
    
    private void addSuggestion(String suggestion) {
    	addSuggestion(suggestion, suggestion);
    }
}
