package org.bukkit.help;

import org.bukkit.command.CommandSender;

public abstract class HelpTopic {

    protected String name;
    protected String shortText;
    protected String fullText;
    protected String amendedPermission;

    public abstract boolean canSee(CommandSender player);

    public void amendCanSee(String amendedPermission) {
        this.amendedPermission = amendedPermission;
    }

    public String getName() {
        return name;
    }

    public String getShortText() {
        return shortText;
    }

    public String getFullText(CommandSender forWho) {
        return fullText;
    }

    public void amendTopic(String amendedShortText, String amendedFullText) {
        if (amendedShortText != null) {
            shortText = amendedShortText.replace("<text>", shortText == null ? "" : shortText);
        }
        if (amendedFullText != null) {
            fullText = amendedFullText.replace("<text>", fullText == null ? "" : fullText);
        }
    }
}
