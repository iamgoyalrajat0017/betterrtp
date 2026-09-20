package com.booksaw.betterTeams.commands.team;

import com.booksaw.betterTeams.CommandResponse;
import com.booksaw.betterTeams.Main;
import com.booksaw.betterTeams.PlayerRank;
import com.booksaw.betterTeams.Team;
import com.booksaw.betterTeams.TeamPlayer;
import com.booksaw.betterTeams.commands.ParentCommand;
import com.booksaw.betterTeams.commands.presets.TeamSubCommand;
import com.booksaw.betterTeams.message.HelpMessage;
import com.booksaw.betterTeams.message.ReferencedFormatMessage;
import com.booksaw.betterTeams.util.ConfirmationManager;
import com.booksaw.betterTeams.util.MoneyUtils;
import com.booksaw.betterTeams.util.TimeUtil;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.util.List;

/**
 * Handles /team bank transfer &lt;player&gt; &lt;amount&gt; - transfers money directly from the
 * team bank to a player's personal balance. Restricted to Owner and Co-owner.
 *
 * @author booksaw
 */
public class BankTransferCommand extends TeamSubCommand {

	private final ParentCommand parentCommand;

	public BankTransferCommand(ParentCommand parentCommand) {
		this.parentCommand = parentCommand;
	}

	@Override
	public CommandResponse onCommand(TeamPlayer player, String label, String[] args, Team team) {

		OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
		if (!target.hasPlayedBefore() && !target.isOnline()) {
			return new CommandResponse("bank.transfer.noPlayer");
		}

		double amount;
		try {
			amount = new BigDecimal(args[1]).doubleValue();
		} catch (Exception e) {
			return new CommandResponse(new HelpMessage(this, label, parentCommand));
		}

		if (amount <= 0 || Double.isNaN(amount) || Double.isInfinite(amount)) {
			return new CommandResponse("withdraw.tooLittle");
		}

		if (team.getMoney() - amount < 0) {
			return new CommandResponse("withdraw.notEnough");
		}

		String signature = "banktransfer:" + target.getUniqueId() + ":" + amount;
		if (!ConfirmationManager.isConfirmed(player.getPlayerUUID(), signature)) {
			ConfirmationManager.requestConfirmation(player.getPlayerUUID(), signature);
			long expirySeconds = Main.plugin.getConfig().getLong("confirmationExpirySeconds", 10);
			return new CommandResponse(new ReferencedFormatMessage("bank.transfer.confirm",
					MoneyUtils.getFormattedDouble(amount), target.getName(), TimeUtil.formatDuration(expirySeconds * 1000L)));
		}

		// Re-validate everything at the point of confirmation - state may have changed since the request
		if (team.getMoney() - amount < 0) {
			return new CommandResponse("withdraw.notEnough");
		}
		if (!target.hasPlayedBefore() && !target.isOnline()) {
			return new CommandResponse("bank.transfer.noPlayer");
		}

		EconomyResponse response = Main.econ.depositPlayer(target, amount);
		if (!response.transactionSuccess()) {
			return new CommandResponse("withdraw.fail");
		}

		// Only deduct from the team bank once the deposit to the player is confirmed successful,
		// so a failed deposit never results in the team losing money
		team.setMoney(team.getMoney() - amount);

		return new CommandResponse(true, new ReferencedFormatMessage("bank.transfer.success",
				MoneyUtils.getFormattedDouble(amount), target.getName()));
	}

	@Override
	public String getCommand() {
		return "transfer";
	}

	@Override
	public String getNode() {
		return "bank.transfer";
	}

	@Override
	public String getHelp() {
		return "Transfer money from the team bank to a player";
	}

	@Override
	public String getArguments() {
		return "<player> <amount>";
	}

	@Override
	public int getMinimumArguments() {
		return 2;
	}

	@Override
	public int getMaximumArguments() {
		return 2;
	}

	@Override
	public void onTabComplete(List<String> options, CommandSender sender, String label, String[] args) {
		if (args.length <= 1) {
			addPlayerStringList(options, (args.length == 0) ? "" : args[0]);
		} else {
			options.add("<amount>");
		}
	}

	@Override
	public PlayerRank getDefaultRank() {
		return PlayerRank.CO_OWNER;
	}

	@Override
	protected boolean runAsync(String[] args) {
		return false;
	}

}
