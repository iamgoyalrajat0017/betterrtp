package com.booksaw.betterTeams.commands.team;

import com.booksaw.betterTeams.*;
import com.booksaw.betterTeams.commands.ParentCommand;
import com.booksaw.betterTeams.commands.presets.TeamSubCommand;
import com.booksaw.betterTeams.customEvents.TeamWithdrawEvent;
import com.booksaw.betterTeams.customEvents.post.PostTeamWithdrawEvent;
import com.booksaw.betterTeams.message.HelpMessage;
import com.booksaw.betterTeams.message.ReferencedFormatMessage;
import com.booksaw.betterTeams.util.ConfirmationManager;
import com.booksaw.betterTeams.util.MoneyUtils;
import com.booksaw.betterTeams.util.TimeUtil;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.util.List;

public class WithdrawCommand extends TeamSubCommand {

	private final ParentCommand parentCommand;

	public WithdrawCommand(ParentCommand parentCommand) {
		this.parentCommand = parentCommand;
	}

	@Override
	public CommandResponse onCommand(TeamPlayer player, String label, String[] args, Team team) {

		double amount;
		try {
			amount = new BigDecimal(args[0]).doubleValue();
		} catch (Exception e) {
			return new CommandResponse(new HelpMessage(this, label, parentCommand));
		}

		if (amount <= 0 || Double.isNaN(amount) || Double.isInfinite(amount)) {
			return new CommandResponse("withdraw.tooLittle");
		}

		if (team.getMoney() - amount < 0) {
			return new CommandResponse("withdraw.notEnough");
		}

		String signature = "withdraw:" + amount;
		if (!ConfirmationManager.isConfirmed(player.getPlayerUUID(), signature)) {
			ConfirmationManager.requestConfirmation(player.getPlayerUUID(), signature);
			long expirySeconds = Main.plugin.getConfig().getLong("confirmationExpirySeconds", 10);
			return new CommandResponse(new ReferencedFormatMessage("withdraw.confirm",
					MoneyUtils.getFormattedDouble(amount), TimeUtil.formatDuration(expirySeconds * 1000L)));
		}

		// Re-validate everything at the point of confirmation, state may have changed since the request
		if (team.getMoney() - amount < 0) {
			return new CommandResponse("withdraw.notEnough");
		}

		final TeamWithdrawEvent event = new TeamWithdrawEvent(team, player, amount);

		Bukkit.getPluginManager().callEvent(event);

		if (event.isCancelled()) {
			return new CommandResponse("withdraw.fail");
		}

		if (amount != event.getAmount())
			amount = event.getAmount();

		if (team.getMoney() - amount < 0) {
			return new CommandResponse("withdraw.notEnough");
		}

		EconomyResponse response = Main.econ.depositPlayer(player.getPlayer(), amount);

		if (!response.transactionSuccess()) {
			return new CommandResponse("withdraw.fail");
		}

		team.setMoney(team.getMoney() - amount);

		Bukkit.getPluginManager().callEvent(new PostTeamWithdrawEvent(team, player, amount));

		return new CommandResponse(true, "withdraw.success");
	}

	@Override
	public String getCommand() {
		return "withdraw";
	}

	@Override
	public String getNode() {
		return "balance";
	}

	@Override
	public String getHelp() {
		return "Withdraw money from the teams balance";
	}

	@Override
	public String getArguments() {
		return "<amount>";
	}

	@Override
	public int getMinimumArguments() {
		return 1;
	}

	@Override
	public int getMaximumArguments() {
		return 1;
	}

	@Override
	public void onTabComplete(List<String> options, CommandSender sender, String label, String[] args) {
		options.add("<amount>");
	}

	@Override
	public PlayerRank getDefaultRank() {
		return PlayerRank.CO_OWNER;
	}

	@Override
	public boolean runAsync(String[] args) {
		return false;
	}

}
