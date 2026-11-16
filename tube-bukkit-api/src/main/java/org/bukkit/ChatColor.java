package org.bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public enum ChatColor {
    BLACK('0'),
    DARK_BLUE('1'),
    DARK_GREEN('2'),
    DARK_AQUA('3'),
    DARK_RED('4'),
    DARK_PURPLE('5'),
    GOLD('6'),
    GRAY('7'),
    DARK_GRAY('8'),
    BLUE('9'),
    GREEN('a'),
    AQUA('b'),
    RED('c'),
    LIGHT_PURPLE('d'),
    YELLOW('e'),
    WHITE('f'),
    MAGIC('k', true),
    BOLD('l', true),
    STRIKETHROUGH('m', true),
    UNDERLINE('n', true),
    ITALIC('o', true),
    RESET('r');

    public static final char COLOR_CHAR = '§';
    private static final Pattern STRIP_COLOR = Pattern.compile("(?i)" + COLOR_CHAR + "[0-9A-FK-OR]");
    private static final Map<Character, ChatColor> BY_CHAR = new HashMap<>();

    static {
        for (ChatColor color : values()) {
            BY_CHAR.put(color.code, color);
        }
    }

    private final char code;
    private final boolean format;
    private final String toString;

    ChatColor(char code) {
        this(code, false);
    }

    ChatColor(char code, boolean format) {
        this.code = code;
        this.format = format;
        this.toString = new String(new char[] {COLOR_CHAR, code});
    }

    public char getChar() {
        return code;
    }

    public boolean isFormat() {
        return format;
    }

    public boolean isColor() {
        return !format && this != RESET;
    }

    @Override
    public String toString() {
        return toString;
    }

    public static ChatColor getByChar(char code) {
        return BY_CHAR.get(Character.toLowerCase(code));
    }

    public static String stripColor(String input) {
        return input == null ? null : STRIP_COLOR.matcher(input).replaceAll("");
    }

    public static String translateAlternateColorCodes(char altColorChar, String textToTranslate) {
        char[] chars = textToTranslate.toCharArray();
        for (int i = 0; i < chars.length - 1; i++) {
            if (chars[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(chars[i + 1]) > -1) {
                chars[i] = COLOR_CHAR;
                chars[i + 1] = Character.toLowerCase(chars[i + 1]);
            }
        }
        return new String(chars);
    }

    public static String getLastColors(String input) {
        StringBuilder result = new StringBuilder();
        for (int i = input.length() - 2; i >= 0; i--) {
            if (input.charAt(i) == COLOR_CHAR) {
                ChatColor color = getByChar(input.charAt(i + 1));
                if (color != null) {
                    result.insert(0, color);
                    if (color.isColor() || color == RESET) {
                        break;
                    }
                }
            }
        }
        return result.toString();
    }
}
