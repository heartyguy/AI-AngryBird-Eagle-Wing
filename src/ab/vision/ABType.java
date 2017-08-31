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
package ab.vision;

public enum ABType {
	Ground(1), Hill(2), Sling(3), RedBird(4), YellowBird(5), BlueBird(6), BlackBird(7), WhiteBird(8), Pig(9), Ice(
			10), Wood(11), Stone(12), TNT(9), // 18
	Unknown(0);

	public int id;

	private ABType(int id) {
		this.id = id;
	}

	/**
	 * @return bird radius
	 */
	public int getBirdRadius() {
		int result = 0;

		switch (this) {
		case RedBird:
			result = 6;
			break;
		case YellowBird:
			result = 7;
			break;
		case WhiteBird:
			result = 13;
			break;
		case BlackBird:
			result = 8;
			break;
		case BlueBird:
			result = 4;
			break;
		default:
			result = 5;
		}

		return result;
	}
}
