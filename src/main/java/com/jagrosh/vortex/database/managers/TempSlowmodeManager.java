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
package com.jagrosh.vortex.database.managers;

import com.jagrosh.easysql.DataManager;
import com.jagrosh.easysql.DatabaseConnector;
import com.jagrosh.easysql.SQLColumn;
import com.jagrosh.easysql.columns.InstantColumn;
import com.jagrosh.easysql.columns.LongColumn;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * @author Michail K (mysteriouscursor+git@protonmail.com)
 */
public class TempSlowmodeManager extends DataManager {
    public static final SQLColumn<Long> CHANNEL_ID = new LongColumn("CHANNEL_ID", false, 0, true);
    public static final SQLColumn<Instant> FINISH = new InstantColumn("FINISH", false, Instant.EPOCH);

    public TempSlowmodeManager(DatabaseConnector connector) {
        super(connector, "TEMP_SLOWMODES");
    }

    public void setSlowmode(TextChannel channel, Instant finish) {
        readWrite(selectAll(CHANNEL_ID.is(channel.getId())), rs -> {
            if (rs.next()) {
                FINISH.updateValue(rs, finish);
                rs.updateRow();
            } else {
                rs.moveToInsertRow();
                CHANNEL_ID.updateValue(rs, channel.getIdLong());
                FINISH.updateValue(rs, finish);
                rs.insertRow();
            }
        });
    }

    public void clearSlowmode(TextChannel channel) {
        readWrite(selectAll(CHANNEL_ID.is(channel.getId())), rs -> {
            if (rs.next()) {
                rs.deleteRow();
            }
        });
    }

    public int timeUntilDisableSlowmode(TextChannel channel) {
        return read(selectAll(CHANNEL_ID.is(channel.getId())), rs -> {
            if (rs.next()) {
                Instant end = FINISH.getValue(rs);
                if (end == Instant.MAX) {
                    return 0;
                } else {
                    return (int) (Instant.now().until(end, ChronoUnit.SECONDS));
                }
            }

            return 0;
        });
    }

    public void checkSlowmode(JDA jda) {
        readWrite(selectAll(FINISH.isLessThan(Instant.now().getEpochSecond())), rs -> {
            while (rs.next()) {
                TextChannel tc = jda.getTextChannelById(CHANNEL_ID.getValue(rs));
                if (tc == null) {
                    continue;
                }

                if (tc.getGuild().getSelfMember().hasPermission(tc, Permission.MANAGE_CHANNEL)) {
                    tc.getManager().setSlowmode(0).reason("Temporary Slowmode Completed").queue(s -> {}, f -> {});
                }

                rs.deleteRow();
            }
        });
    }
}
