/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.io.StringWriter;
import java.util.EnumSet;

class SoundManager extends WObject {
	public SoundManager(WApplication app) {
		super(app);
		this.wApp_ = app;
		WFlashObject player_ = new WFlashObject(WApplication.getResourcesUrl()
				+ "WtSoundManager.swf", this.wApp_.getDomRoot());
		player_.setAlternativeContent((WWidget) null);
		player_.resize(new WLength(100), new WLength(100));
		player_.setPositionScheme(PositionScheme.Absolute);
		player_.setOffsets(new WLength(-900), EnumSet.of(Side.Left, Side.Top));
		player_.setFlashParameter("allowScriptAccess", "always");
		player_.setFlashParameter("quality", "high");
		player_.setFlashParameter("bgcolor", "#aaaaaa");
		player_.setFlashParameter("wmode", "");
		this.wApp_
				.doJavaScript(
						"WtSoundManager = {};WtSoundManager.initialized = false;WtSoundManager.queue = new Array();WtSoundManager.player = null;WtSoundManager.flashInitializedCB = function() {WtSoundManager.initialized = true;WtSoundManager.player = "
								+ player_.getJsFlashRef()
								+ ";var i, il;for (i = 0, il = WtSoundManager.queue.length; i < il; i++) {var action = WtSoundManager.queue[i].action;if (action == 'add') {WtSoundManager.add(WtSoundManager.queue[i].id, WtSoundManager.queue[i].url);} else if (action == 'remove') {WtSoundManager.remove(WtSoundManager.queue[i].id);} else if (action == 'play') {WtSoundManager.doPlay(WtSoundManager.queue[i].id, WtSoundManager.queue[i].loops);} else if (action == 'stop') {WtSoundManager.doStop(WtSoundManager.queue[i].id);} else {alert('WWtSoundManager internal error: action not found: ' + action);}}};WtSoundManager.onerror = function() {alert('WtSoundManager failed to start');};WtSoundManager.add = function(id, url) {if(WtSoundManager.initialized) {try {WtSoundManager.player.WtAdd(id, url);} catch (e) {}} else {WtSoundManager.queue.push({action: 'add', id: id, url: url});}};WtSoundManager.remove = function(id) {if (WtSoundManager.initialized) {try {WtSoundManager.player.WtRemove(id);} catch (e) {}} else {WtSoundManager.queue.push({action: 'remove', id: id});}};\nWtSoundManager.doPlay = function(id, loops) {\nif (WtSoundManager.initialized) {\ntry {WtSoundManager.player.WtPlay(id, loops);\n} catch (e) {}} else {\nWtSoundManager.queue.push({action: 'play', id: id, loops: loops});\n}\n};\nWtSoundManager.doStop = function(id) {if (WtSoundManager.initialized) {try {WtSoundManager.player.WtStop(id);} catch (e) {}} else {WtSoundManager.queue.push({action: 'stop', id: id});}};",
						false);
	}

	public void add(WSound sound) {
		StringWriter ss = new StringWriter();
		ss.append("WtSoundManager.add(\"").append(sound.getId()).append(
				"\", \"").append(sound.getUrl()).append("\");");
		this.wApp_.doJavaScript(ss.toString());
	}

	public void remove(WSound sound) {
		StringWriter ss = new StringWriter();
		ss.append("WtSoundManager.remove(\"").append(sound.getId()).append(
				"\", \"").append(sound.getUrl()).append("\");");
		this.wApp_.doJavaScript(ss.toString());
	}

	public void play(WSound sound, int loops) {
		StringWriter ss = new StringWriter();
		ss.append("WtSoundManager.doPlay(\"").append(sound.getId()).append(
				"\", ").append(String.valueOf(sound.getLoops())).append(");");
		this.wApp_.doJavaScript(ss.toString());
	}

	public void stop(WSound sound) {
		StringWriter ss = new StringWriter();
		ss.append("WtSoundManager.doStop(\"").append(sound.getId()).append(
				"\");");
		this.wApp_.doJavaScript(ss.toString());
	}

	public boolean isFinished(WSound sound) {
		return true;
	}

	public int loopsRemaining(WSound sound) {
		return 0;
	}

	private WApplication wApp_;
}
