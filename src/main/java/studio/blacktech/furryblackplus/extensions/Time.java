/*
 * Copyright (C) 2021 Alceatraz @ BlackTechStudio
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the BTS Anti-Commercial & GNU Affero General.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * BTS Anti-Commercial & GNU Affero General Public License for more details.
 *
 * You should have received a copy of the BTS Anti-Commercial & GNU Affero
 * General Public License along with this program in README or LICENSE.
 */

package studio.blacktech.furryblackplus.extensions;

import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.UserMessageEvent;
import studio.blacktech.furryblackplus.Driver;
import studio.blacktech.furryblackplus.core.define.Command;
import studio.blacktech.furryblackplus.core.define.annotation.Executor;
import studio.blacktech.furryblackplus.core.define.moduel.EventHandlerExecutor;
import studio.blacktech.furryblackplus.core.utilties.logger.LoggerX;

import java.io.File;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;

@Executor(
    value = "Executor-Time",
    outline = "环球时间",
    description = "现在都几点了，为什么那个孩子还在水群",
    command = "time",
    usage = {
        "/time - 查看世界时间"
    },
    privacy = {
        "获取命令发送人"
    }
)
public class Time extends EventHandlerExecutor {


    private static final ZoneId zone_00 = ZoneId.of("UTC");
    private static final ZoneId zone_CN = ZoneId.of("Asia/Shanghai");

    private Integer hour;
    private String cache;

    private Map<String, ZoneId> TIME_ZONE;


    @Override
    public void init() {

        this.initRootFolder();
        this.initConfFolder();

        this.TIME_ZONE = new LinkedHashMap<>();

        File FILE_TIMEZONE = this.initConfFile("timezone.txt");

        for (String line : this.readFile(FILE_TIMEZONE)) {

            int indexOfColon = line.indexOf(":");

            if (indexOfColon < 0) {
                this.logger.warning("配置无效 " + line);
                continue;
            }

            String name = line.substring(0, indexOfColon);
            String zone = line.substring(indexOfColon + 1);

            ZoneId timeZone;

            try {
                timeZone = ZoneId.of(zone);
            } catch (DateTimeException exception) {
                this.logger.error("配置无效 TimeZone无法加载 -> " + zone, exception);
                continue;
            }

            if (!zone.equals("GMT") && timeZone.getId().equals("GMT")) {
                this.logger.warning("配置无效 TimeZone将不可识别的区域转换为GMT " + line);
            }

            this.TIME_ZONE.put(name, timeZone);

            this.logger.seek("添加时区 " + name + " -> " + timeZone.getId());
        }
    }

    @Override
    public void boot() {}

    @Override
    public void shut() {}

    @Override
    public void handleUsersMessage(UserMessageEvent event, Command command) {
        Driver.sendMessage(event, this.getTime());
    }

    @Override
    public void handleGroupMessage(GroupMessageEvent event, Command command) {
        Driver.sendAtMessage(event, "\r\n" + this.getTime());
    }

    private String getTime() {
        int currentHour = LocalDateTime.now().getHour();
        if (this.hour == null || this.hour != currentHour) {
            this.hour = currentHour;
            StringBuilder builder = new StringBuilder();
            builder.append("世界协调时(UTC) ").append(LoggerX.format("yyyy-MM-dd HH:mm", zone_00)).append("\r\n");
            for (Map.Entry<String, ZoneId> entry : this.TIME_ZONE.entrySet()) {
                ZoneId value = entry.getValue();
                builder.append(entry.getKey());
                builder.append(" ");
                builder.append(LoggerX.format("HH:mm", value));
                builder.append(suffix(value));
                builder.append("\r\n");
            }
            builder.append("亚洲中国(UTC+8) ").append(LoggerX.format("HH:mm", zone_CN));
            this.cache = builder.toString();
        }
        return this.cache;
    }


    public static StringBuilder suffix(ZoneId zone) {
        LocalDateTime local = LocalDateTime.now(zone);
        LocalDateTime china = LocalDateTime.now(zone_CN);
        StringBuilder builder = new StringBuilder();
        if (zone.getRules().isDaylightSavings(local.toInstant(ZoneOffset.of("+8")))) {
            builder.append(" 夏令时");
        }
        int localDay = local.getDayOfYear();
        int chinaDay = china.getDayOfYear();
        //noinspection ConstantConditions
        do {
            if (chinaDay - localDay > 0) {
                builder.append(" 昨天,");
            } else if (chinaDay - localDay < 0) {
                builder.append(" 明天,");
            } else {
                break;
            }
            builder.append(localDay);
            builder.append("日");
        } while (false);
        return builder;
    }

}
