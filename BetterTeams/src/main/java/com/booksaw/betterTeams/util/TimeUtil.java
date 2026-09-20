package com.booksaw.betterTeams.util;

public class TimeUtil {
	private TimeUtil() {
	}

	/**
	 * Formats a duration given in milliseconds as a short human-readable string,
	 * e.g. "1d 3h 12m 4s". Zero-value units are omitted, values under a second
	 * round up to 1 second so cooldown messages never show "0s" while a
	 * cooldown is still active.
	 *
	 * @param millis the duration in milliseconds
	 * @return the formatted duration
	 */
	public static String formatDuration(long millis) {
		long totalSeconds = millis / 1000;
		if (millis % 1000 != 0) {
			totalSeconds++;
		}
		if (totalSeconds <= 0) {
			return "0s";
		}

		long days = totalSeconds / 86400;
		long hours = (totalSeconds % 86400) / 3600;
		long minutes = (totalSeconds % 3600) / 60;
		long seconds = totalSeconds % 60;

		StringBuilder builder = new StringBuilder();
		if (days > 0) {
			builder.append(days).append("d ");
		}
		if (hours > 0) {
			builder.append(hours).append("h ");
		}
		if (minutes > 0) {
			builder.append(minutes).append("m ");
		}
		if (seconds > 0 || builder.length() == 0) {
			builder.append(seconds).append("s ");
		}

		return builder.toString().trim();
	}
}
