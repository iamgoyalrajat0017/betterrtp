package com.booksaw.betterTeams.commands.teama.admin;

import com.booksaw.betterTeams.CommandResponse;
import com.booksaw.betterTeams.Team;
import com.booksaw.betterTeams.commands.SubCommand;
import com.booksaw.betterTeams.message.MessageManager;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Handles /teama admin bank &lt;team&gt; - shows a team's bank balance. To change a team's bank
 * balance, use the existing /teama money add|set|remove &lt;team&gt; &lt;amount&gt; commands.
 *
 * @author booksaw
 */
public class AdminBankTeama extends SubCommand {

	@Override
	public CommandResponse onCommand(CommandSender sender, String label, String[] args) {

		Team team = Team.getTeam(args[0]);
		if (team == null) {
			return new CommandResponse("admin.inspect.noTeam");
		}

		MessageManager.sendMessage(sender, "admin.inspect.bank", team.getName(), team.getBalance());

		return new CommandResponse(true);
	}

	@Override
	public String getCommand() {
		return "bank";
	}

	@Override
	public int getMinimumArguments() {
		return 1;
	}

	@Override
	public String getNode() {
		return "admin.inspect.bank";
	}

	@Override
	public String getHelp() {
		return "View a team's bank balance";
	}

	@Override
	public String getArguments() {
		return "<team>";
	}

	@Override
	public int getMaximumArguments() {
		return 1;
	}

	@Override
	public void onTabComplete(List<String> options, CommandSender sender, String label, String[] args) {
		if (args.length == 1) {
			addTeamStringList(options, args[0]);
		}
	}

	@Override
	public boolean runAsync(String[] args) {
		return false;
	}
}
