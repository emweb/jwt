package eu.webtoolkit.jwt.examples.googlemap;

import java.util.ArrayList;
import java.util.List;

import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.WColor;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WGoogleMap;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WTable;
import eu.webtoolkit.jwt.WText;

public class GoogleMapExample extends WContainerWidget {
	private WGoogleMap map;
	public GoogleMapExample() {
		  WTable layout = new WTable(this);
		  map = new WGoogleMap(layout.getElementAt(0,0));
		  map.resize(700, 500);

		  map.setMapTypeControl(WGoogleMap.MapTypeControl.DefaultControl);
		  map.enableScrollWheelZoom();

		  layout.getElementAt(0,1).setPadding(new WLength(3));

		  WContainerWidget zoomContainer = 
		    new WContainerWidget(layout.getElementAt(0,1));
		  new WText("Zoom: ", zoomContainer);
		  WPushButton zoomIn = new WPushButton("+", zoomContainer);
		  zoomIn.clicked().addListener(this, new Signal1.Listener<WMouseEvent>(){
			public void trigger(WMouseEvent arg) {
				map.zoomIn();
			}
		  });
		  WPushButton zoomOut = new WPushButton("-", zoomContainer);
		  zoomOut.clicked().addListener(this, new Signal1.Listener<WMouseEvent>(){
				public void trigger(WMouseEvent arg) {
					map.zoomOut();
				}
		  });

		  List<WGoogleMap.Coordinate> road = new ArrayList<WGoogleMap.Coordinate>();
		  roadDescription(road);
		  map.addPolyline(road, new WColor(0, 191, 255));

		  map.setCenter(road.get(road.size()-1));

		  map.openInfoWindow(road.get(0), 
		  		      "<img src=\"http://www.emweb.be/css/emweb_small.jpg\" />" +
		  		      "<br/>" +
		  		      "<b>Emweb office</b>");
	}
	
	private void roadDescription(List<WGoogleMap.Coordinate> roadDescription) 
	{ 
	  roadDescription.add(new WGoogleMap.Coordinate(50.85342000000001, 4.7281));
	  roadDescription.add(new WGoogleMap.Coordinate(50.85377, 4.72573));
	  roadDescription.add(new WGoogleMap.Coordinate(50.85393, 4.72496));
	  roadDescription.add(new WGoogleMap.Coordinate(50.85393, 4.72496));
	  roadDescription.add(new WGoogleMap.Coordinate(50.85372, 4.72482));
	  roadDescription.add(new WGoogleMap.Coordinate(50.85304, 4.72421));
	  roadDescription.add(new WGoogleMap.Coordinate(50.8519, 4.72297));
	  roadDescription.add(new WGoogleMap.Coordinate(50.85154, 4.72251));
	  roadDescription.add(new WGoogleMap.Coordinate(50.85154, 4.72251));
	  roadDescription.add(new WGoogleMap.Coordinate(50.85153, 4.72205));
	  roadDescription.add(new WGoogleMap.Coordinate(50.85153, 4.72205));
	  roadDescription.add(new WGoogleMap.Coordinate(50.85752, 4.7186));
	  roadDescription.add(new WGoogleMap.Coordinate(50.85847, 4.71798));
	  roadDescription.add(new WGoogleMap.Coordinate(50.859, 4.71753));
	  roadDescription.add(new WGoogleMap.Coordinate(50.8593, 4.71709));
	  roadDescription.add(new WGoogleMap.Coordinate(50.85986999999999, 4.71589));
	  roadDescription.add(new WGoogleMap.Coordinate(50.8606, 4.7147));
	  roadDescription.add(new WGoogleMap.Coordinate(50.8611, 4.71327));
	  roadDescription.add(new WGoogleMap.Coordinate(50.86125999999999, 4.71293));
	  roadDescription.add(new WGoogleMap.Coordinate(50.86184000000001, 4.71217));
	  roadDescription.add(new WGoogleMap.Coordinate(50.86219, 4.71202));
	  roadDescription.add(new WGoogleMap.Coordinate(50.86346, 4.71178));
	  roadDescription.add(new WGoogleMap.Coordinate(50.86406, 4.71146));
	  roadDescription.add(new WGoogleMap.Coordinate(50.86478, 4.71126));
	  roadDescription.add(new WGoogleMap.Coordinate(50.86623000000001, 4.71111));
	  roadDescription.add(new WGoogleMap.Coordinate(50.86659999999999, 4.71101));
	  roadDescription.add(new WGoogleMap.Coordinate(50.8668, 4.71072));
	  roadDescription.add(new WGoogleMap.Coordinate(50.86709, 4.71018));
	  roadDescription.add(new WGoogleMap.Coordinate(50.86739, 4.70941));
	  roadDescription.add(new WGoogleMap.Coordinate(50.86751, 4.70921));
	  roadDescription.add(new WGoogleMap.Coordinate(50.86869, 4.70843));
	  roadDescription.add(new WGoogleMap.Coordinate(50.8691, 4.70798));
	  roadDescription.add(new WGoogleMap.Coordinate(50.8691, 4.70798));
	  roadDescription.add(new WGoogleMap.Coordinate(50.86936, 4.70763));
	  roadDescription.add(new WGoogleMap.Coordinate(50.86936, 4.70763));
	  roadDescription.add(new WGoogleMap.Coordinate(50.86874, 4.70469));
	  roadDescription.add(new WGoogleMap.Coordinate(50.86858, 4.70365));
	  roadDescription.add(new WGoogleMap.Coordinate(50.86845999999999, 4.70269));
	  roadDescription.add(new WGoogleMap.Coordinate(50.86839, 4.70152));
	  roadDescription.add(new WGoogleMap.Coordinate(50.86843, 4.70043));
	  roadDescription.add(new WGoogleMap.Coordinate(50.86851000000001, 4.69987));
	  roadDescription.add(new WGoogleMap.Coordinate(50.86881999999999, 4.69869));
	  roadDescription.add(new WGoogleMap.Coordinate(50.8689, 4.69827));
	  roadDescription.add(new WGoogleMap.Coordinate(50.87006, 4.6941));
	  roadDescription.add(new WGoogleMap.Coordinate(50.87006, 4.6941));
	  roadDescription.add(new WGoogleMap.Coordinate(50.87045999999999, 4.69348));
	  roadDescription.add(new WGoogleMap.Coordinate(50.87172, 4.69233));
	  roadDescription.add(new WGoogleMap.Coordinate(50.87229000000001, 4.69167));
	  roadDescription.add(new WGoogleMap.Coordinate(50.87229000000001, 4.69167));
	  roadDescription.add(new WGoogleMap.Coordinate(50.8725, 4.69123));
	  roadDescription.add(new WGoogleMap.Coordinate(50.8725, 4.69123));
	  roadDescription.add(new WGoogleMap.Coordinate(50.87408, 4.69142));
	  roadDescription.add(new WGoogleMap.Coordinate(50.87423, 4.69125));
	  roadDescription.add(new WGoogleMap.Coordinate(50.87464, 4.69116));
	  roadDescription.add(new WGoogleMap.Coordinate(50.87579999999999, 4.69061));
	  roadDescription.add(new WGoogleMap.Coordinate(50.87595, 4.69061));
	  roadDescription.add(new WGoogleMap.Coordinate(50.87733, 4.69073));
	  roadDescription.add(new WGoogleMap.Coordinate(50.87742, 4.69078));
	  roadDescription.add(new WGoogleMap.Coordinate(50.87784, 4.69131));
	  roadDescription.add(new WGoogleMap.Coordinate(50.87784, 4.69131));
	  roadDescription.add(new WGoogleMap.Coordinate(50.87759, 4.69267));
	  roadDescription.add(new WGoogleMap.Coordinate(50.8775, 4.6935));
	  roadDescription.add(new WGoogleMap.Coordinate(50.87751, 4.69395));
	  roadDescription.add(new WGoogleMap.Coordinate(50.87768, 4.69545));
	  roadDescription.add(new WGoogleMap.Coordinate(50.87769, 4.69666));
	  roadDescription.add(new WGoogleMap.Coordinate(50.87759, 4.69742));
	  roadDescription.add(new WGoogleMap.Coordinate(50.87734, 4.69823));
	  roadDescription.add(new WGoogleMap.Coordinate(50.87734, 4.69823));
	  roadDescription.add(new WGoogleMap.Coordinate(50.87790999999999, 4.69861));
	}
}
