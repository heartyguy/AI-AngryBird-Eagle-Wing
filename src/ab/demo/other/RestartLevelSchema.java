/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2015,  XiaoYu (Gary) Ge, Stephen Gould,Jochen Renz
 ** Sahan Abeyasinghe, Jim Keys,   Andrew Wang, Peng Zhang
 ** Team DataLab Birds: Karel Rymes, Radim Spetlik, Tomas Borovicka
 ** Team HeartyTian: Tian Jian Wang
 ** All rights reserved.
 **This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License.
 **To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/
 *or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
 *****************************************************************************/
package ab.demo.other;

import ab.server.Proxy;
import ab.server.proxy.message.ProxyClickMessage;
import ab.server.proxy.message.ProxyMouseWheelMessage;
import ab.utils.StateUtil;
import ab.vision.GameStateExtractor.GameState;

public class RestartLevelSchema {
	private Proxy proxy;

	// This schema is used for automatically restarting levels in the standalone
	// version.
	public RestartLevelSchema(Proxy proxy) {
		this.proxy = proxy;
	}

	public boolean restartLevel() {
		GameState state = StateUtil.getGameState(proxy);

		if (state == GameState.WON || state == GameState.LOST) {
			proxy.send(new ProxyClickMessage(420, 380));// Click the left most
														// button at the end
														// page
			System.out.println(" restart the level ");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		} else if (state == GameState.PLAYING) {
			proxy.send(new ProxyClickMessage(100, 39));
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}

		// Wait 4000 seconds for loading the level
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e1) {

			e1.printStackTrace();
		}
		// Zooming out
		System.out.println("Zooming out");
		for (int k = 0; k < 15; k++) {
			proxy.send(new ProxyMouseWheelMessage(-1));
		}
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {

			e1.printStackTrace();
		}

		return true;

	}
}
