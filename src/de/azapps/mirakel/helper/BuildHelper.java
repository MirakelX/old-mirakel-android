package de.azapps.mirakel.helper;

import de.azapps.mirakelandroid.BuildConfig;

public class BuildHelper {
	public static boolean isNightly() {
		return BuildConfig.DEBUG;
	}

	public static boolean isBeta() {
		return BuildConfig.DEBUG;
	}

	public static boolean isRelease() {
		return !BuildConfig.DEBUG;
	}

	public static boolean isForPlayStore() {
		return BuildConfig.DONATIONS_GOOGLE;
	}

	public static boolean isForFDroid() {
		return BuildConfig.DONATIONS_GOOGLE;
	}

	public static boolean useAutoUpdater() {
		return !(isForPlayStore() || isForFDroid());
	}
}
