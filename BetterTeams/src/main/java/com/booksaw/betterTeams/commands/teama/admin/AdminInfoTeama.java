package com.booksaw.betterTeams.commands.teama.admin;

import com.booksaw.betterTeams.CommandResponse;
import com.booksaw.betterTeams.PlayerRank;
import com.booksaw.betterTeams.Team;
import com.booksaw.betterTeams.TeamPlayer;
import com.booksaw.betterTeams.commands.SubCommand;
import com.booksaw.betterTeams.message.MessageManager;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles /teama admin info &lt;team&gt; - full inspection of a team: tag, owner, co-owners,
 * team admins, members, level, count/limit and bank. Admin-only.
 *
 * @author booksaw
 */
public class AdminInfoTeama extends SubCommand {

	@Override
	public CommandResponse onCommand(CommandSender sender, String label, String[] args) {

		Team team = Team.getTeam(args[0]);
		if (team == null) {
			return new CommandResponse("admin.inspect.noTeam");
		}

		MessageManager.sendMessage(sender, "admin.inspect.infoHeader", team.getDisplayName());
		if (team.getTag() != null && !team.getTag().isEmpty()) {
			MessageManager.sendMessage(sender, "admin.inspect.infoTag", team.getTag());
		}

		MessageManager.sendMessage(sender, "admin.inspect.infoOwner", namesOf(team.getRank(PlayerRank.OWNER)));
		MessageManager.sendMessage(sender, "admin.inspect.infoCoOwners", namesOf(team.getRank(PlayerRank.CO_OWNER)));
		MessageManager.sendMessage(sender, "admin.inspect.infoAdmins", namesOf(team.getRank(PlayerRank.ADMIN)));

		MessageManager.sendMessage(sender, "admin.inspect.infoMembers",
				team.getMembers().size(), team.getTeamLimit(), namesOf(new java.util.ArrayList<>(team.getMembers().getClone())));

		MessageManager.sendMessage(sender, "admin.inspect.infoLevel", team.getLevel());
		MessageManager.sendMessage(sender, "admin.inspect.infoBank", team.getBalance());

		return new CommandResponse(true);
	}

	private String namesOf(List<TeamPlayer> players) {
		if (players.isEmpty()) {
			return "none";
		}
		return players.stream()
				.map(p -> p.getPlayer().getName() == null ? "unknown" : p.getPlayer().getName())
				.collect(Collectors.joining(", "));
	}

	@Override
	public String getCommand() {
		return "info";
	}

	@Override
	public int getMinimumArguments() {
		return 1;
	}

	@Override
	public String getNode() {
		return "admin.inspect.info";
	}

	@Override
	public String getHelp() {
		return "View full details about a team";
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
