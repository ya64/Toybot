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
import com.jagrosh.easysql.columns.LongColumn;
import net.dv8tion.jda.api.audit.AuditLogEntry;

import java.util.LinkedList;
import java.util.List;

/**
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class AuditCacheManager extends DataManager {
    public static final SQLColumn<Long> GUILD_ID = new LongColumn("GUILD_ID", false, 0L, true);
    public static final SQLColumn<Long> OLD = new LongColumn("OLD", false, 0L);
    public static final SQLColumn<Long> OLDER = new LongColumn("OLDER", false, 0L);
    public static final SQLColumn<Long> OLDEST = new LongColumn("OLDEST", false, 0L);

    public AuditCacheManager(DatabaseConnector connector) {
        super(connector, "AUDIT_CACHE");
    }

    // TODO: Figure out why this was implemented like this
    public List<AuditLogEntry> filterUncheckedEntries(List<AuditLogEntry> list) {
        if (list.isEmpty()) {
            return list;
        }

        long gid = list.get(0).getGuild().getIdLong();
        return readWrite(selectAll(GUILD_ID.is(gid)), rs -> {
            LinkedList<AuditLogEntry> filtered = new LinkedList<>();
            long old, older, oldest;
            boolean found = rs.next();
            if (found) {
                old = OLD.getValue(rs);
                older = OLDER.getValue(rs);
                oldest = OLDEST.getValue(rs);
            } else {
                old = 0;
                older = 0;
                oldest = 0;
                rs.moveToInsertRow();
            }

            for (AuditLogEntry entry : list) {
                if (entry.getIdLong() > oldest && entry.getIdLong() != older && entry.getIdLong() != old) {
                    filtered.add(0, entry);
                }
            }

            OLD.updateValue(rs, list.get(0).getIdLong());
            if (list.size() >= 3) {
                OLDER.updateValue(rs, list.get(1).getIdLong());
                OLDEST.updateValue(rs, list.get(2).getIdLong());
            } else if (list.size() == 2) {
                OLDER.updateValue(rs, list.get(1).getIdLong());
                OLDEST.updateValue(rs, list.get(1).getIdLong());
            } else {
                OLDER.updateValue(rs, list.get(0).getIdLong());
                OLDEST.updateValue(rs, list.get(0).getIdLong());
            }

            if (found) {
                rs.updateRow();
            } else {
                GUILD_ID.updateValue(rs, gid);
                rs.insertRow();
            }

            return filtered;
        });
    }

    // this is messy but will be replaced by hibernate soon
    public long getLastParsed(long guildId) {
        Long val = read(select(GUILD_ID.is(guildId), GUILD_ID, OLDEST), rs -> rs.first() ? OLDEST.getValue(rs) : 0L);
        return val == null ? 0L : val;
    }

    public void setLastParsed(long guildId, long lastParsedId) {
        readWrite(select(GUILD_ID.is(guildId), GUILD_ID, OLDEST), rs -> {
           if (rs.first()) {
               OLDEST.updateValue(rs, lastParsedId);
               rs.updateRow();
           } else {
               rs.moveToInsertRow();
               GUILD_ID.updateValue(rs, guildId);
               OLDEST.updateValue(rs, lastParsedId);
               rs.insertRow();
           }
        });
    }
}
