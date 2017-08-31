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

/**
 * Schema for loading level
 */
public class LoadingLevelSchema {
	private Proxy proxy;
	private int pageSwitch = 0;

	public LoadingLevelSchema(Proxy proxy) {
		this.proxy = proxy;

	}

	public boolean loadLevel(int i) {

		if (i < 21)
			pageSwitch = 0;
		else if (i >= 22 && i <= 42)
			pageSwitch = 1;
		else if (i > 42)
			pageSwitch = 2;

		i = ((i % 21) == 0) ? 21 : i % 21;

		// System.out.println(StateUtil.checkCurrentState(proxy));
		loadLevel(StateUtil.getGameState(proxy), i);

		GameState state = StateUtil.getGameState(proxy);

		while (state != GameState.PLAYING) {
			System.out.println(" In state:   " + state + " Try reloading...");
			loadLevel(state, i);
			state = StateUtil.getGameState(proxy);
			if (state == GameState.PLAYING)
				break;
			try {
				// zmensil jsem z 12000!

				Thread.sleep(1000);

			} catch (InterruptedException e1) {

				e1.printStackTrace();
			}

		}
		return true;
	}

	private boolean loadLevel(GameState state, int i) {
		// if still at main menu or episode menu, skip it.
		ActionRobot.GoFromMainMenuToLevelSelection();

		if (state == GameState.WON || state == GameState.LOST) {

			/*
			 * if (state == GameState.WON && i >= current + 1) proxy.send(new
			 * ProxyClickMessage(500,375)); // go to the next level
			 */ /* if (state == GameState.WON) */ {

				proxy.send(new ProxyClickMessage(342, 382));// Click the left
															// most button at
															// the end page
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}

				while (pageSwitch-- > 0) {
					proxy.send(new ProxyClickMessage(378, 451));

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}

				proxy.send(new ProxyClickMessage(54 + ((i - 1) % 7) * 86, 110 + ((i - 1) / 7) * 100));
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {

					e.printStackTrace();
				}
			}
			if (i == 1)
				// skip the animation, the animation does not appear in the SD
				// mode.
				proxy.send(new ProxyClickMessage(1176, 704));
		} else if (state == GameState.PLAYING) {
			proxy.send(new ProxyClickMessage(48, 44));// Click the left most
														// button, pause
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {

				e1.printStackTrace();
			}
			proxy.send(new ProxyClickMessage(168, 28));// Click the left most
														// button, pause
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

			while (pageSwitch-- > 0) {
				proxy.send(new ProxyClickMessage(378, 451));

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
			proxy.send(new ProxyClickMessage(54 + ((i - 1) % 7) * 86, 110 + ((i - 1) / 7) * 100));
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
			if (i == 1)
				proxy.send(new ProxyClickMessage(1176, 704));
		} else {
			while (pageSwitch-- > 0) {
				proxy.send(new ProxyClickMessage(378, 451));

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}

			proxy.send(new ProxyClickMessage(54 + ((i - 1) % 7) * 86, 110 + ((i - 1) / 7) * 100));
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
			if (i == 1)
				proxy.send(new ProxyClickMessage(1176, 704));
		}

		// Wait 9000 seconds for loading the level
		GameState _state = StateUtil.getGameState(proxy);
		int count = 0; // at most wait 10 seconds
		while (_state != GameState.PLAYING && count < 30) {
			try {
				// zmensil sem z 3000

				Thread.sleep(250);
			} catch (InterruptedException e1) {

				e1.printStackTrace();
			}
			count++;
			_state = StateUtil.getGameState(proxy);

		}

		if (_state == GameState.PLAYING) {

			for (int k = 0; k < 15; k++) {
				proxy.send(new ProxyMouseWheelMessage(-1));
			}

			try {
				// zmensil jsem z 2000
				Thread.sleep(2000);
			} catch (InterruptedException e1) {

				e1.printStackTrace();
			}

		}

		// System.out.println("current: " + current + " i " + i);
		return true;

	}
}
