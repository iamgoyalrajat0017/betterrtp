package com.booksaw.betterTeams;

import com.booksaw.betterTeams.message.MessageManager;

/**
 * This class is used to define all the possible ranks that a player can be
 * within a team
 *
 * @author booksaw
 */
public enum PlayerRank {
	/**
	 * The starting rank with the lowest number of permissions
	 */
	DEFAULT(0),

	/**
	 * This player has permissions to invite new players to the team and kick
	 * default users, but cannot ban people or change team settings. Cannot
	 * access the team bank beyond depositing.
	 */
	ADMIN(1),

	/**
	 * A persistent rank between Team Admin and Owner. Co-owners can withdraw
	 * from and transfer the team bank, same as the Owner, but there is
	 * always exactly one true Owner (assigned via /team setowner)
	 */
	CO_OWNER(2),

	/**
	 * The highest rank, this rank has full permissions over the team
	 */
	OWNER(3);

	public final int value;

	PlayerRank(int value) {
		this.value = value;
	}

	public static PlayerRank getRank(String string) {

		switch (string.toUpperCase()) {
			case "DEFAULT":
				return DEFAULT;
			case "OWNER":
				return OWNER;
			case "CO_OWNER":
			case "COOWNER":
			case "CO-OWNER":
				return CO_OWNER;
			case "ADMIN":
				return ADMIN;
			default:
				return null;
		}

	}

	public static PlayerRank getRank(int value) {
		for (PlayerRank rank : values()) {
			if (rank.value == value) {
				return rank;
			}
		}
		return null;
	}

	/**
	 * @return the prefix for that player rank
	 */
	public String getPrefix() {
		return MessageManager.getMessage("prefix." + this.toString().toLowerCase());
	}

}
