package com.booksaw.betterTeams.commands.team;

import com.booksaw.betterTeams.CommandResponse;
import com.booksaw.betterTeams.Main;
import com.booksaw.betterTeams.PlayerRank;
import com.booksaw.betterTeams.Team;
import com.booksaw.betterTeams.TeamPlayer;
import com.booksaw.betterTeams.commands.presets.TeamSubCommand;
import com.booksaw.betterTeams.cooldown.PersistentCooldownManager;
import com.booksaw.betterTeams.message.ReferencedFormatMessage;
import com.booksaw.betterTeams.util.TimeUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * This class handles the /team leave command
 *
 * @author booksaw
 */
public class LeaveCommand extends TeamSubCommand {

	@Override
	public CommandResponse onCommand(TeamPlayer teamPlayer, String label, String[] args, Team team) {

		if (teamPlayer.getRank() == PlayerRank.OWNER && team.getRank(PlayerRank.OWNER).size() == 1) {
			return new CommandResponse("leave.lastOwner");
		}

		// The team owner cannot leave until ownership has been transferred (/team setowner <player>)
		if (teamPlayer.getRank() == PlayerRank.OWNER) {
			return new CommandResponse("leave.ownerCannotLeave");
		}

		Player onlinePlayer = teamPlayer.getOnlinePlayer().orElse(null);
		if (onlinePlayer != null && !onlinePlayer.hasPermission("betterTeams.admin")) {
			long lockMillis = Main.plugin.getConfig().getLong("cooldowns.member-leave") * 1000L;
			long remaining = PersistentCooldownManager.get()
					.getRemainingMillis("member-leave", teamPlayer.getPlayerUUID(), lockMillis);
			if (remaining > 0) {
				return new CommandResponse(new ReferencedFormatMessage("leave.locked", TimeUtil.formatDuration(remaining)));
			}
		}

		if (team.removePlayer(teamPlayer.getPlayer())) {
			PersistentCooldownManager.get().setTimestampNow("team-recreate", teamPlayer.getPlayerUUID());
			PersistentCooldownManager.get().clear("member-leave", teamPlayer.getPlayerUUID());
			return new CommandResponse(true, "leave.success");
		}
		// event has been cancelled
		return new CommandResponse(false);
	}

	@Override
	public String getCommand() {
		return "leave";
	}

	@Override
	public int getMinimumArguments() {
		return 0;
	}

	@Override
	public String getNode() {
		return "leave";
	}

	@Override
	public String getHelp() {
		return "Leave your current team";
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
	public PlayerRank getDefaultRank() {
		return PlayerRank.DEFAULT;
	}

	@Override
	protected boolean runAsync(String[] args) {
		return false;
	}
}
