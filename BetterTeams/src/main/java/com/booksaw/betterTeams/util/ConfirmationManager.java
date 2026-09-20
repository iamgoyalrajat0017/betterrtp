package com.booksaw.betterTeams.util;

import com.booksaw.betterTeams.Main;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A generic, in-memory "type it again to confirm" system for actions that move money or
 * otherwise change team state in an impactful way (bank withdrawals, bank-to-player
 * transfers, team level upgrades).
 * <p>
 * A confirmation is tied to a specific action signature (e.g. "withdraw:500000") so that
 * confirming one action can never accidentally execute a different one (different amount/
 * target) that happens to be requested within the confirmation window.
 * <p>
 * Each confirmation is single-use: successfully consuming it via {@link #isConfirmed} removes
 * it immediately, preventing double execution from spamming the command.
 *
 * @author booksaw
 */
public class ConfirmationManager {

	private static final Map<UUID, PendingConfirmation> pending = new ConcurrentHashMap<>();

	private static class PendingConfirmation {
		final String signature;
		final long timestamp;

		PendingConfirmation(String signature, long timestamp) {
			this.signature = signature;
			this.timestamp = timestamp;
		}
	}

	private ConfirmationManager() {
	}

	/**
	 * @return the configured confirmation expiry, in milliseconds
	 */
	private static long getExpiryMillis() {
		return Main.plugin.getConfig().getLong("confirmationExpirySeconds", 10) * 1000L;
	}

	/**
	 * Checks whether the player has an active, matching, non-expired confirmation pending
	 * for this exact action, and if so, consumes it (so it cannot be reused/replayed).
	 *
	 * @param player    the player confirming the action
	 * @param signature a string uniquely identifying this exact action + its parameters
	 * @return true if this call should proceed as the confirmed execution
	 */
	public static boolean isConfirmed(UUID player, String signature) {
		PendingConfirmation confirmation = pending.get(player);
		if (confirmation == null) {
			return false;
		}
		if (System.currentTimeMillis() - confirmation.timestamp > getExpiryMillis()) {
			// expired - never valid, and cannot be reused
			pending.remove(player);
			return false;
		}
		if (!confirmation.signature.equals(signature)) {
			// a different action is being requested, not a confirmation of the pending one
			return false;
		}
		// Consume immediately - a confirmation can only ever be used once
		pending.remove(player);
		return true;
	}

	/**
	 * Registers a pending confirmation for the given action signature, overwriting any
	 * previous pending confirmation for this player.
	 */
	public static void requestConfirmation(UUID player, String signature) {
		pending.put(player, new PendingConfirmation(signature, System.currentTimeMillis()));
	}

	/**
	 * Explicitly clears any pending confirmation for a player (e.g. after a related action
	 * makes it no longer valid).
	 */
	public static void clear(UUID player) {
		pending.remove(player);
	}
}
