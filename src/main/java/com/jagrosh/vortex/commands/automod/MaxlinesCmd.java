/*
 * Copyright 2018 John Grosh (john.a.grosh@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. Furthermore, I'm putting this sentence in all files because I messed up git and its not showing files as edited -\\_( :) )_/-
 */
package com.jagrosh.vortex.commands.automod;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.vortex.Vortex;
import com.jagrosh.vortex.utils.FormatUtil;
import net.dv8tion.jda.api.Permission;

/**
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class MaxlinesCmd extends Command {
    private final Vortex vortex;

    public MaxlinesCmd(Vortex vortex) {
        this.vortex = vortex;
        this.name = "maxlines";
        this.guildOnly = true;
        this.aliases = new String[]{"maxnewlines"};
        this.category = new Category("AutoMod");
        this.arguments = "<maximum | OFF>";
        this.help = "sets maximum lines allowed per message";
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.replyError("Please provide a maximum number of newlines allowed!");
            return;
        }

        int maxlines;
        try {
            maxlines = Integer.parseInt(event.getArgs());
        } catch (NumberFormatException ex) {
            if (event.getArgs().equalsIgnoreCase("none") || event.getArgs().equalsIgnoreCase("off")) {
                maxlines = 0;
            } else {
                event.replyError(FormatUtil.filterEveryone("`" + event.getArgs() + "` is not a valid integer!"));
                return;
            }
        }

        vortex.getDatabase().automod.setMaxLines(event.getGuild(), maxlines);
        if (maxlines <= 0) {
            event.replySuccess("There is now no maximum line limit.");
        } else if (maxlines == 1) {
            event.replyError("Maximum lines must be greater than 1!");
        } else {
            event.replySuccess("Messages longer than `" + maxlines + "` lines will now be automatically deleted, " + "and users will receive strikes for every additional multiple of up to `" + maxlines + "` lines.");
        }
    }
}
