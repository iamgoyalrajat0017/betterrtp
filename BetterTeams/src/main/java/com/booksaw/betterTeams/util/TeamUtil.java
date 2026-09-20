package com.booksaw.betterTeams.util;

import com.booksaw.betterTeams.CommandResponse;
import com.booksaw.betterTeams.Main;
import com.booksaw.betterTeams.Team;
import com.booksaw.betterTeams.message.ReferencedFormatMessage;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;


public class TeamUtil {
	private TeamUtil() {

	}

	public static @Nullable CommandResponse verifyTeamName(@Nullable String teamName) {
		if (!Team.isValidTeamName(teamName)) {
			return new CommandResponse("create.banned");
		}

		int max = Math.min(55, Main.plugin.getConfig().getInt("maxTeamLength"));
		if (max != -1 && max < teamName.length()) {
			return new CommandResponse("create.maxLength");
		}

		int min = Math.max(0, Math.min(55, Main.plugin.getConfig().getInt("minTeamLength")));
		if (min != 0 && min > teamName.length()) {
			return new CommandResponse("create.minLength");
		}

		return null;
	}

	public static @Nullable CommandResponse verifyTagName(@Nullable String tagName) {
		if (!Team.isValidTeamName(tagName)) {
			return new CommandResponse("tag.banned");
		}

		int max = Math.min(55, Main.plugin.getConfig().getInt("maxTagLength"));
		if (max != -1 && max < tagName.length()) {
			return new CommandResponse("tag.maxLength");
		}

		return null;
	}

	/**
	 * Checks whether the player can afford the configured cost for an action, without charging them.
	 * Used to validate BEFORE the action is performed, so a player is never charged for a failed action.
	 *
	 * @param sender                       the player performing the action
	 * @param economyConfigKey             the key under the "economy" config section, e.g. "team-name-change"
	 * @param insufficientFundsMessageKey  the messages.yml key to show if they cannot afford it
	 * @return a CommandResponse to return immediately if they cannot afford it, or null if they can proceed
	 */
	public static @Nullable CommandResponse verifyCanAffordCost(Player sender, String economyConfigKey, String insufficientFundsMessageKey) {
		double cost = Main.plugin.getConfig().getDouble("economy." + economyConfigKey);
		if (cost <= 0 || Main.econ == null || sender.hasPermission("betterteams.cost.bypass")) {
			return null;
		}
		if (!Main.econ.has(sender, cost)) {
			return new CommandResponse(new ReferencedFormatMessage(insufficientFundsMessageKey,
					MoneyUtils.getFormattedDouble(cost), MoneyUtils.getFormattedDouble(Main.econ.getBalance(sender))));
		}
		return null;
	}

	/**
	 * Withdraws the configured cost from the player. ONLY call this after the action has definitely
	 * succeeded (verifyCanAffordCost should be called beforehand to confirm they can afford it).
	 *
	 * @param sender            the player to charge
	 * @param economyConfigKey  the key under the "economy" config section, e.g. "team-name-change"
	 */
	public static void chargeCost(Player sender, String economyConfigKey) {
		double cost = Main.plugin.getConfig().getDouble("economy." + economyConfigKey);
		if (cost <= 0 || Main.econ == null || sender.hasPermission("betterteams.cost.bypass")) {
			return;
		}
		EconomyResponse response = Main.econ.withdrawPlayer(sender, cost);
		if (!response.transactionSuccess()) {
			Main.plugin.getLogger().warning("Failed to withdraw " + economyConfigKey + " cost from "
					+ sender.getName() + ": " + response.errorMessage);
		}
	}
}
