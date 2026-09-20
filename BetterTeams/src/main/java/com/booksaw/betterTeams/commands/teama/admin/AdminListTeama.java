package com.booksaw.betterTeams.commands.teama.admin;

import com.booksaw.betterTeams.CommandResponse;
import com.booksaw.betterTeams.PlayerRank;
import com.booksaw.betterTeams.Team;
import com.booksaw.betterTeams.TeamPlayer;
import com.booksaw.betterTeams.commands.SubCommand;
import com.booksaw.betterTeams.message.MessageManager;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Handles /teama admin list - shows every team with their owner, level, member count and bank
 * balance. Admin-only (gated by the betterteams.admin permission on the parent /teama command).
 *
 * @author booksaw
 */
public class AdminListTeama extends SubCommand {

	@Override
	public CommandResponse onCommand(CommandSender sender, String label, String[] args) {

		String[] teamNames = Team.getTeamManager().sortTeamsByMembers();

		MessageManager.sendMessage(sender, "admin.inspect.listHeader", teamNames.length);

		for (String teamName : teamNames) {
			if (teamName == null) {
				continue;
			}
			Team team = Team.getTeam(teamName);
			if (team == null) {
				continue;
			}

			String ownerName = "none";
			List<TeamPlayer> owners = team.getRank(PlayerRank.OWNER);
			if (!owners.isEmpty() && owners.get(0).getPlayer().getName() != null) {
				ownerName = owners.get(0).getPlayer().getName();
			}

			MessageManager.sendMessage(sender, "admin.inspect.listEntry",
					team.getName(), ownerName, team.getLevel(), team.getMembers().size(), team.getBalance());
		}

		return new CommandResponse(true);
	}

	@Override
	public String getCommand() {
		return "list";
	}

	@Override
	public int getMinimumArguments() {
		return 0;
	}

	@Override
	public String getNode() {
		return "admin.inspect.list";
	}

	@Override
	public String getHelp() {
		return "List all teams with their owner, level, member count and bank";
	}

	@Override
	public String getArguments() {
		return "";
	}

	@Override
	public int getMaximumArguments() {
		return 0;
	}

	@Override
	public void onTabComplete(List<String> options, CommandSender sender, String label, String[] args) {
	}

	@Override
	public boolean runAsync(String[] args) {
		return false;
	}
}
