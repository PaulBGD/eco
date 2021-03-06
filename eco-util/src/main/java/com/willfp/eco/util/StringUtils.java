package com.willfp.eco.util;

import com.willfp.eco.util.integrations.placeholder.PlaceholderManager;
import com.willfp.eco.util.optional.Prerequisite;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static net.md_5.bungee.api.ChatColor.COLOR_CHAR;

@UtilityClass
public class StringUtils {
    /**
     * Regex for gradients.
     */
    private static final Pattern GRADIENT_REGEX = Pattern.compile("<GRADIENT:([0-9A-Fa-f]{6})>(.*?)</GRADIENT:([0-9A-Fa-f]{6})>");

    /**
     * Regex for hex codes.
     */
    private static final Pattern HEX_PATTERN = Pattern.compile("&#" + "([A-Fa-f0-9]{6})" + "");

    /**
     * Translate a string - converts Placeholders and Color codes.
     *
     * @param message The message to translate.
     * @param player  The player to translate placeholders with respect to.
     * @return The message, translated.
     */
    public String translate(@NotNull final String message,
                            @Nullable final Player player) {
        String processedMessage = message;
        if (Prerequisite.MINIMUM_1_16.isMet()) {
            processedMessage = translateGradients(processedMessage);
        }
        processedMessage = PlaceholderManager.translatePlaceholders(processedMessage, player);
        processedMessage = translateHexColorCodes(processedMessage);
        processedMessage = ChatColor.translateAlternateColorCodes('&', processedMessage);
        return processedMessage;
    }

    /**
     * Translate a string without respect to a player.
     *
     * @param message The message to translate.
     * @return The message, translated.
     * @see StringUtils#translate(String, Player)
     */
    public String translate(@NotNull final String message) {
        return translate(message, null);
    }

    private static String translateHexColorCodes(@NotNull final String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, COLOR_CHAR + "x"
                    + COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1)
                    + COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3)
                    + COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5));
        }

        return matcher.appendTail(buffer).toString();
    }

    /**
     * Colors a string with a gradient.
     *
     * @param string The string to color.
     * @param start  The start color.
     * @param end    The end color.
     * @return The string, colored.
     */
    private static String processGradients(@NotNull final String string,
                                           @NotNull final Color start,
                                           @NotNull final Color end) {
        String processedString = string;
        List<ChatColor> modifiers = new ArrayList<>();
        if (processedString.contains("&l")) {
            modifiers.add(ChatColor.BOLD);
        }
        if (processedString.contains("&o")) {
            modifiers.add(ChatColor.ITALIC);
        }
        if (processedString.contains("&n")) {
            modifiers.add(ChatColor.UNDERLINE);
        }
        if (processedString.contains("&k")) {
            modifiers.add(ChatColor.MAGIC);
        }
        processedString = processedString.replace("&l", "");
        processedString = processedString.replace("&o", "");
        processedString = processedString.replace("&n", "");
        processedString = processedString.replace("&k", "");

        StringBuilder stringBuilder = new StringBuilder();
        ChatColor[] colors = getGradientColors(start, end, processedString.length());
        String[] characters = processedString.split("");
        for (int i = 0; i < processedString.length(); i++) {
            stringBuilder.append(colors[i]);
            modifiers.forEach(stringBuilder::append);
            stringBuilder.append(characters[i]);
        }
        return stringBuilder.toString();
    }

    /**
     * Creates chatColors for gradients.
     *
     * @param start The start color.
     * @param end   The end color.
     * @param step  How many colors are returned.
     * @return Array of chat colors.
     */
    private static ChatColor[] getGradientColors(@NotNull final Color start,
                                                 @NotNull final Color end,
                                                 final int step) {
        ChatColor[] colors = new ChatColor[step];
        int stepR = Math.abs(start.getRed() - end.getRed()) / (step - 1);
        int stepG = Math.abs(start.getGreen() - end.getGreen()) / (step - 1);
        int stepB = Math.abs(start.getBlue() - end.getBlue()) / (step - 1);
        int[] direction = new int[]{
                start.getRed() < end.getRed() ? +1 : -1,
                start.getGreen() < end.getGreen() ? +1 : -1,
                start.getBlue() < end.getBlue() ? +1 : -1
        };

        for (int i = 0; i < step; i++) {
            Color color = new Color(start.getRed() + ((stepR * i) * direction[0]), start.getGreen() + ((stepG * i) * direction[1]), start.getBlue() + ((stepB * i) * direction[2]));
            colors[i] = ChatColor.of(color);
        }
        return colors;
    }

    /**
     * Add gradients to a string.
     *
     * @param string The string.
     * @return The string, colorized.
     */
    private static String translateGradients(@NotNull final String string) {
        String processedString = string;
        Matcher matcher = GRADIENT_REGEX.matcher(string);
        while (matcher.find()) {
            String start = matcher.group(1);
            String end = matcher.group(3);
            String content = matcher.group(2);
            processedString = processedString.replace(matcher.group(), processGradients(content, new Color(Integer.parseInt(start, 16)), new Color(Integer.parseInt(end, 16))));
        }
        return processedString;
    }

    /**
     * Internal implementation of {@link String#valueOf}.
     * Formats collections and doubles better.
     *
     * @param object The object to convert to string.
     * @return The object stringified.
     */
    public String internalToString(@Nullable final Object object) {
        if (object == null) {
            return "null";
        }

        if (object instanceof Integer) {
            return ((Integer) object).toString();
        } else if (object instanceof String) {
            return (String) object;
        } else if (object instanceof Double) {
            return NumberUtils.format((Double) object);
        } else if (object instanceof Collection<?>) {
            Collection<?> c = (Collection<?>) object;
            return c.stream().map(StringUtils::internalToString).collect(Collectors.joining(", "));
        } else {
            return String.valueOf(object);
        }
    }

    /**
     * Remove a string of characters from the start of a string.
     *
     * @param string The string to remove the prefix from.
     * @param prefix The substring to remove.
     * @return The string with the prefix removed.
     */
    public String removePrefix(@NotNull final String string,
                               @NotNull final String prefix) {
        if (string.startsWith(prefix)) {
            return string.substring(prefix.length());
        }
        return string;
    }
}
