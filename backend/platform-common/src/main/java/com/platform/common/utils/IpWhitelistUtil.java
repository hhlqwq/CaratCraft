package com.platform.common.utils;

/**
 * IP 白名单匹配工具 / IP whitelist matching utility.
 *
 * <p>支持精确 IP 与 IPv4 CIDR 网段两种规则, 例如 "162.62.182.0/24" 或 "116.62.148.248"。 Supports exact IPv4
 * addresses and IPv4 CIDR ranges such as "162.62.182.0/24".
 */
public class IpWhitelistUtil {

    /** 禁止实例化只提供静态方法的IP白名单工具. */
    private IpWhitelistUtil() {}

    /**
     * 判断 IP 是否命中单条规则 / Checks whether an IP matches a single rule.
     *
     * @param ip 请求 IP / request ip
     * @param rule 白名单规则, 支持精确 IP 或 CIDR / whitelist rule, exact ip or cidr
     * @return 是否命中 / true when matched
     */
    public static boolean matches(String ip, String rule) {
        if (ip == null || ip.isBlank() || rule == null || rule.isBlank()) {
            return false;
        }
        rule = rule.trim();
        if (!rule.contains("/")) {
            return rule.equals(ip.trim());
        }
        String[] parts = rule.split("/");
        if (parts.length != 2) {
            return false;
        }
        byte[] network = parseV4(parts[0]);
        byte[] address = parseV4(ip);
        if (network == null || address == null) {
            return false;
        }
        int prefix;
        try {
            prefix = Integer.parseInt(parts[1].trim());
        } catch (NumberFormatException e) {
            return false;
        }
        if (prefix <= 0) {
            return true;
        }
        if (prefix > 32) {
            return false;
        }
        int fullBytes = prefix / 8;
        int restBits = prefix % 8;
        for (int i = 0; i < fullBytes; i++) {
            if (network[i] != address[i]) {
                return false;
            }
        }
        if (restBits > 0) {
            int mask = 0xFF << (8 - restBits) & 0xFF;
            if ((network[fullBytes] & mask) != (address[fullBytes] & mask)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断 IP 是否命中规则列表 / Checks whether an IP matches any rule in the list.
     *
     * @param ip 请求 IP / request ip
     * @param rules 逗号分隔的规则 / comma separated rules
     * @return 是否命中 / true when any rule matched
     */
    public static boolean matchesAny(String ip, String rules) {
        if (rules == null || rules.isBlank()) {
            return false;
        }
        for (String rule : rules.split(",")) {
            if (matches(ip, rule)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 解析 IPv4 为 4 字节数组 / Parses an IPv4 string into a 4-byte array.
     *
     * @param ip IPv4 字符串 / ipv4 string
     * @return 字节数组, 非法返回 null / byte array or null when invalid
     */
    private static byte[] parseV4(String ip) {
        if (ip == null) {
            return null;
        }
        String[] segments = ip.trim().split("\\.");
        if (segments.length != 4) {
            return null;
        }
        byte[] result = new byte[4];
        for (int i = 0; i < 4; i++) {
            int value;
            try {
                value = Integer.parseInt(segments[i]);
            } catch (NumberFormatException e) {
                return null;
            }
            if (value < 0 || value > 255) {
                return null;
            }
            result[i] = (byte) value;
        }
        return result;
    }
}
