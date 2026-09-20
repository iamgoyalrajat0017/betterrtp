package com.booksaw.betterTeams.cooldown;

import com.booksaw.betterTeams.Main;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Stores per-player cooldown timestamps (epoch millis) in a dedicated data
 * file (playerdata-cooldowns.yml) so that they survive server restarts.
 * This is intentionally separate from {@link CommandCooldown} (which is
 * purely in-memory) because several custom cooldowns added for this server
 * (team-create, team-recreate, member-leave-lock) are required to persist.
 *
 * @author booksaw
 */
public class PersistentCooldownManager {

	private static PersistentCooldownManager instance;

	public static PersistentCooldownManager get() {
		if (instance == null) {
			instance = new PersistentCooldownManager();
		}
		return instance;
	}

	/**
	 * Only used in tests / reload scenarios to force a fresh instance
	 */
	public static void reset() {
		instance = null;
	}

	private final File file;
	private final YamlConfiguration config;

	private PersistentCooldownManager() {
		file = new File(Main.plugin.getDataFolder(), "playerdata-cooldowns.yml");
		if (!file.exists()) {
			try {
				file.getParentFile().mkdirs();
				file.createNewFile();
			} catch (IOException e) {
				Main.plugin.getLogger().log(Level.SEVERE, "Could not create playerdata-cooldowns.yml", e);
			}
		}
		config = YamlConfiguration.loadConfiguration(file);
	}

	/**
	 * Records the moment a timed cooldown/lock should be considered as having
	 * started for a player (i.e. "now"). The expiry is calculated later using
	 * the relevant config value, so that changing the config duration applies
	 * retroactively to already-running cooldowns.
	 *
	 * @param key    the cooldown identifier, e.g. "team-create", "team-recreate", "member-leave"
	 * @param player the player's UUID
	 */
	public void setTimestamp(String key, UUID player, long timestamp) {
		config.set(key + "." + player, timestamp);
		save();
	}

	public void setTimestampNow(String key, UUID player) {
		setTimestamp(key, player, System.currentTimeMillis());
	}

	/**
	 * @param key            the cooldown identifier
	 * @param player         the player's UUID
	 * @param durationMillis how long (in ms) the cooldown lasts from the stored timestamp
	 * @return milliseconds remaining, or 0 if there is no active cooldown
	 */
	public long getRemainingMillis(String key, UUID player, long durationMillis) {
		long stored = config.getLong(key + "." + player, -1);
		if (stored == -1) {
			return 0;
		}
		long expiry = stored + durationMillis;
		long remaining = expiry - System.currentTimeMillis();
		return Math.max(remaining, 0);
	}

	/**
	 * Removes a stored timestamp (e.g. once a cooldown has been consumed/is irrelevant)
	 */
	public void clear(String key, UUID player) {
		config.set(key + "." + player, null);
		save();
	}

	private void save() {
		try {
			config.save(file);
		} catch (IOException e) {
			Main.plugin.getLogger().log(Level.SEVERE, "Could not save playerdata-cooldowns.yml", e);
		}
	}

}
