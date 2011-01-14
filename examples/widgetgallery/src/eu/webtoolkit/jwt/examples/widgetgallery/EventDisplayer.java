package eu.webtoolkit.jwt.examples.widgetgallery;

import eu.webtoolkit.jwt.AbstractSignal;
import eu.webtoolkit.jwt.Signal;
import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WString;
import eu.webtoolkit.jwt.WText;

public class EventDisplayer extends WContainerWidget {
	public EventDisplayer(WContainerWidget parent) {
		  super(parent);
		  eventCount_ = 0;
		  text_ = new WText("Events will be shown here.", this);
		  setStyleClass("events");
	}

	public void setStatus( CharSequence msg) {
		showEventImpl("Last status message: " + msg.toString());
	}

	public void showSignal(Signal1<WString> s, final String data) {
		s.addListener(this, new Signal1.Listener<WString>() {
			public void trigger(WString arg) {
				showWStringSignal(data, arg);
			}
		});
	}
	
	public void showSignal(AbstractSignal s, final String data) {
		s.addListener(this, new Signal.Listener(){
			public void trigger() {
				showSignalImpl(data);
			}
		});
	}
	
	public void showEvent(Signal1<WString> s, final String data) {
		s.addListener(this, new Signal1.Listener<WString>() {
			public void trigger(WString arg) {
				showWStringSignal(data, arg);
			}
		});
	}
	
	public void showEvent(AbstractSignal s, final CharSequence data) {
		s.addListener(this, new Signal.Listener(){
			public void trigger() {
				showSignalImpl(data);
			}
		});
	}

	private CharSequence lastEventStr_;
	private int eventCount_;
	private WText text_;

	private void showWStringSignal( String str,  WString wstr) {
		showEventImpl("Last activated signal: " + str + wstr.toString());
	}
	private void showSignalImpl( CharSequence str) {
		showEventImpl("Last activated signal: " + str.toString());
	}
	private void showEventImpl( CharSequence str) {
		  if (str == lastEventStr_) {
			    ++eventCount_;
			    text_.setText(str + " (" + eventCount_ + " times)");
			  } else {
			    lastEventStr_ = str;
			    eventCount_ = 1;
			    text_.setText(str);
			  }
	}
}
