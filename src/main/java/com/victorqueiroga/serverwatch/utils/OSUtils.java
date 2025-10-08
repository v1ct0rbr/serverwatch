package com.victorqueiroga.serverwatch.utils;

import eu.bitwalker.useragentutils.OperatingSystem;

public class OSUtils {

	private static final String OS_WINDOWS = "WINDOWS";
	private static final String OS_LINUX = "LINUX";
	private static final String OS_ANDROID = "ANDROID";
	private static final String OS_MAC_OS = "MAC OS";
	private static final String OS_IOS = "IOS";
	private static final String OS_WINDOWS_MOBILE = "WINDOWS MOBILE";
	private static final String OS_WINDOWS_PHONE = "WINDOWS PHONE";

	private static final String[] OS_LINUX_DISTRIBUTIONS = { "UBUNTU", "KUBUNTU", "DEBIAN", "FEDORA", "RED HAT",
			"CENTOS",
			"MANDRIVA", "MINT", "SUSE", "GENTOO", "ARCH", "SLACKWARE", "KNOPPIX", "LINUX MINT", "PCLINUXOS", "ZORIN",
			"CHROMIUM OS", "ELEMENTARY OS", "DEEPIN", "MANJARO", "ANTERGOS", "SOLUS", "OPEN SUSE", "LUBUNTU", "XUBUNTU",
			"EDUBUNTU", "LINUX MINT DEBIAN EDITION", "LINUX MINT LMDE", "LINUX MINT DEBIAN",
			"LINUX MINT DEBIAN EDITION",
			"RASPBIAN" };

	public static boolean isOSWindows(OperatingSystem op) {
		String sys = op.getName().toLowerCase();
		return sys.contains(OS_WINDOWS.toLowerCase())
				&& (!sys.contains(OS_WINDOWS_MOBILE.toLowerCase()) && !sys.contains(OS_WINDOWS_PHONE.toLowerCase()));
	}

	public static boolean isOSLinux(OperatingSystem os) {
		return os.getName().toLowerCase().contains(OS_LINUX.toLowerCase()) || isOSLinuxDistribution(os);
	}

	public static boolean isOSAndroid(OperatingSystem os) {
		return os.getName().toLowerCase().contains(OS_ANDROID.toLowerCase());
	}

	public static boolean isOSMAC(OperatingSystem os) {
		return os.getName().toLowerCase().contains(OS_MAC_OS.toLowerCase());
	}

	public static boolean isOSIOS(OperatingSystem os) {
		return os.getName().toLowerCase().contains(OS_IOS.toLowerCase());
	}

	public static boolean isOSWindowsMobile(OperatingSystem os) {
		return (os.getName().toLowerCase().contains(OS_WINDOWS_MOBILE.toLowerCase())
				|| os.getName().toLowerCase().contains(OS_WINDOWS_PHONE));
	}

	public static boolean isOSLinuxDistribution(OperatingSystem os) {
		String sys = os.getName().toUpperCase();
		for (String dist : OS_LINUX_DISTRIBUTIONS) {
			if (sys.contains(dist)) {
				return true;
			}
		}
		return false;
	}

}
