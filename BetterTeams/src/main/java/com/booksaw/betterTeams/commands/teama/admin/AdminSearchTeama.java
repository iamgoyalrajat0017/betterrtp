package com.booksaw.betterTeams.commands.teama.admin;

import com.booksaw.betterTeams.CommandResponse;
import com.booksaw.betterTeams.Team;
import com.booksaw.betterTeams.TeamPlayer;
import com.booksaw.betterTeams.commands.SubCommand;
import com.booksaw.betterTeams.message.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Handles /teama admin search &lt;player&gt; - shows which team a player belongs to and their
 * rank within it.
 *
 * @author booksaw
 */
public class AdminSearchTeama extends SubCommand {

	@Override
	public CommandResponse onCommand(CommandSender sender, String label, String[] args) {

		OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
		if (!target.hasPlayedBefore() && !target.isOnline()) {
			return new CommandResponse("admin.inspect.noPlayer");
		}

		Team team = Team.getTeam(target);
		if (team == null) {
			return new CommandResponse("admin.inspect.notInTeam");
		}

		TeamPlayer teamPlayer = team.getTeamPlayer(target);
		String rank = teamPlayer == null ? "unknown" : teamPlayer.getRank().toString();

		MessageManager.sendMessage(sender, "admin.inspect.searchResult", target.getName(), team.getName(), rank);

		return new CommandResponse(true);
	}

	@Override
	public String getCommand() {
		return "search";
	}

	@Override
	public int getMinimumArguments() {
		return 1;
	}

	@Override
	public String getNode() {
		return "admin.inspect.search";
	}

	@Override
	public String getHelp() {
		return "Search for the team and rank of a player";
	}

	@Override
	public String getArguments() {
		return "<player>";
	}

	@Override
	public int getMaximumArguments() {
		return 1;
	}

	@Override
	public void onTabComplete(List<String> options, CommandSender sender, String label, String[] args) {
		if (args.length == 1) {
			addPlayerStringList(options, args[0]);
		}
	}

	@Override
	public boolean runAsync(String[] args) {
		return false;
	}
}
