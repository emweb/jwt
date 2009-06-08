package eu.webtoolkit.jwt.examples.composer;

import java.io.File;
import java.util.ArrayList;

import eu.webtoolkit.jwt.Signal;
import eu.webtoolkit.jwt.TextFormat;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WBreak;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WText;

/**
 * Main widget of the %Composer example.
 */
public class ComposeExample extends WContainerWidget
{
	/**
	 * create a new Composer example.
	 */
	public ComposeExample(WContainerWidget parent)
	{
		super(parent);
		composer_ = new Composer(this);

		ArrayList<Contact> addressBook = new ArrayList<Contact>();
		addressBook.add(new Contact("Koen Deforche", "koen.deforche@gmail.com"));
		addressBook.add(new Contact("Koen alias1", "koen.alias1@yahoo.com"));
		addressBook.add(new Contact("Koen alias2", "koen.alias2@yahoo.com"));
		addressBook.add(new Contact("Koen alias3", "koen.alias3@yahoo.com"));
		addressBook.add(new Contact("Bartje", "jafar@hotmail.com"));
		composer_.setAddressBook(addressBook);

		ArrayList<Contact> contacts = new ArrayList<Contact>();
		contacts.add(new Contact("Koen Deforche", "koen.deforche@gmail.com"));

		composer_.setTo(contacts);
		composer_.setSubject("That's cool! Want to start your own google?");

		composer_.send.addListener(this, new Signal.Listener()
		{
			public void trigger()
			{
				send();
			}
		});
		composer_.discard.addListener(this, new Signal.Listener()
		{
			public void trigger()
			{
				discard();
			}
		});

		details_ = new WContainerWidget(this);

		new WText(tr("example.info"), details_);
	}

	public ComposeExample()
	{
		this(null);
	}

	private Composer composer_;

	private WContainerWidget details_;

	private void send()
	{
		WContainerWidget feedback = new WContainerWidget(this);
		feedback.setStyleClass("feedback");

		WContainerWidget horiz = new WContainerWidget(feedback);
		new WText("<p>We could have, but did not send the following email:</p>", horiz);

		ArrayList<Contact> contacts = composer_.to();
		if (!contacts.isEmpty())
			horiz = new WContainerWidget(feedback);
		for (int i = 0; i < contacts.size(); ++i)
		{
			WText t = new WText("To: \"" + contacts.get(i).name + "\" <" + contacts.get(i).email + ">", horiz);
			t.setTextFormat(TextFormat.PlainText);
			new WBreak(horiz);
		}

		contacts = composer_.cc();
		if (!contacts.isEmpty())
			horiz = new WContainerWidget(feedback);
		for (int i = 0; i < contacts.size(); ++i)
		{
			WText t = new WText("Cc: \"" + contacts.get(i).name + "\" <" + contacts.get(i).email + ">", horiz);
			t.setTextFormat(TextFormat.PlainText);
			new WBreak(horiz);
		}

		contacts = composer_.bcc();
		if (!contacts.isEmpty())
			horiz = new WContainerWidget(feedback);
		for (int i = 0; i < contacts.size(); ++i)
		{
			WText t = new WText("Bcc: \"" + contacts.get(i).name + "\" <" + contacts.get(i).email + ">", horiz);
			t.setTextFormat(TextFormat.PlainText);
			new WBreak(horiz);
		}

		horiz = new WContainerWidget(feedback);
		WText t = new WText("Subject: \"" + composer_.subject() + "\"", horiz);
		t.setTextFormat(TextFormat.PlainText);

		ArrayList<Attachment> attachments = composer_.attachments();
		if (!attachments.isEmpty())
			horiz = new WContainerWidget(feedback);
		for (int i = 0; i < attachments.size(); ++i)
		{
			WText text = new WText("Attachment: \"" + attachments.get(i).fileName + "\" ("
					+ attachments.get(i).contentDescription + ")", horiz);
			t.setTextFormat(TextFormat.PlainText);

			new File(attachments.get(i).spoolFileName).delete();

			text = new WText(", was in spool file: " + attachments.get(i).spoolFileName, horiz);
			new WBreak(horiz);
		}

		String message = composer_.message();

		horiz = new WContainerWidget(feedback);
		t = new WText("Message body: ", horiz);
		new WBreak(horiz);

		if (message.length() != 0)
		{
			t = new WText(message, horiz);
			t.setTextFormat(TextFormat.PlainText);
		}
		else
			t = new WText("<i>(empty)</i>", horiz);

		composer_.remove();
		details_.remove();

		WApplication.instance().quit();
	}

	private void discard()
	{
		WContainerWidget feedback = new WContainerWidget(this);
		feedback.setStyleClass("feedback");

		WContainerWidget horiz = new WContainerWidget(feedback);
		new WText("<p>Wise decision! Everyone's mailbox is already full anyway.</p>", horiz);

		composer_.remove();
		details_.remove();

		WApplication.instance().quit();
	}
}
