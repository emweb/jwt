package eu.webtoolkit.jwt.examples.features.mediaplayer;

import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WLink;
import eu.webtoolkit.jwt.WMediaPlayer;
import eu.webtoolkit.jwt.WXmlLocalizedStrings;
import eu.webtoolkit.jwt.WMediaPlayer.Encoding;
import eu.webtoolkit.jwt.WMediaPlayer.MediaType;
import eu.webtoolkit.jwt.WString;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.WtServlet;

public class MediaPlayer extends WtServlet {
	@Override
	public WApplication createApplication(WEnvironment env) {
		  WApplication app = new WApplication(env);

		  WXmlLocalizedStrings resourceBundle = new WXmlLocalizedStrings();
		  resourceBundle.use("/eu/webtoolkit/jwt/examples/features/mediaplayer/text");
		  app.setLocalizedStrings(resourceBundle);

		  WLink ogvVideo =
			  new WLink("http://www.webtoolkit.eu/videos/sintel_trailer.ogv");
		  WLink mp4Video =
			  new WLink("http://www.webtoolkit.eu/videos/sintel_trailer.mp4");
		  WLink mp3Audio =
			  new WLink("http://www.webtoolkit.eu/audio/LaSera-NeverComeAround.mp3");

		  WLink poster = new WLink("sintel_trailer.jpg");
		  
		  new WText(WString.tr("intro"), app.getRoot());

		  new WText(WString.tr("video"), app.getRoot());

		  WMediaPlayer player = new WMediaPlayer(MediaType.Video, app.getRoot());

		  player.addSource(Encoding.M4V, mp4Video);
		  player.addSource(Encoding.OGV, ogvVideo);
		  player.addSource(Encoding.PosterImage, poster);
		  player.setTitle("<a href=\"http://durian.blender.org/\"target=\"_blank\">Sintel</a>, (c) copyright Blender Foundation");

		  new WText(WString.tr("audio"), app.getRoot());

		  player = new WMediaPlayer(MediaType.Audio, app.getRoot());

		  player.addSource(Encoding.MP3, mp3Audio);
		  player.setTitle("La Sera - Never Come Around");
		  
		  return app;
	}
}
