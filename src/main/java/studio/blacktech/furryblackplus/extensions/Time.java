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
import studio.blacktech.furryblackplus.FurryBlack;
import studio.blacktech.furryblackplus.core.common.exception.moduels.boot.BootException;
import studio.blacktech.furryblackplus.core.handler.EventHandlerExecutor;
import studio.blacktech.furryblackplus.core.handler.annotation.Executor;
import studio.blacktech.furryblackplus.core.handler.common.Command;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.zone.ZoneRules;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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

    private static final DateTimeFormatter FORMATTER_UTC = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(zone_00);
    private static final DateTimeFormatter FORMATTER_RPC = DateTimeFormatter.ofPattern("HH:mm").withZone(zone_CN);
    private static final DateTimeFormatter FORMATTER_NOR = DateTimeFormatter.ofPattern("HH:mm");

    private static final byte[] WRAP_LINE = "\r\n".getBytes(StandardCharsets.UTF_8);

    private String cache;
    private Instant cacheTime;


    private Map<String, ZoneId> TIME_ZONE;


    @Override
    public void init() {

        this.initRootFolder();
        this.initConfFolder();

        // =====================================================================

        File AVAILABLE_TIMEZONE = this.initModuleFile("available-timezone.txt");

        LinkedList<String> availableTimezone = new LinkedList<>(ZoneId.getAvailableZoneIds());
        availableTimezone.sort(CharSequence::compare);

        try (FileOutputStream fileOutputStream = new FileOutputStream(AVAILABLE_TIMEZONE)) {
            for (String line : availableTimezone) {
                fileOutputStream.write(line.getBytes(StandardCharsets.UTF_8));
                fileOutputStream.write(WRAP_LINE);
            }
        } catch (IOException exception) {
            throw new BootException("写入可用时区失败", exception);
        }

        // =====================================================================

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
        FurryBlack.sendMessage(event, this.getWithCache());
    }

    @Override
    public void handleGroupMessage(GroupMessageEvent event, Command command) {
        FurryBlack.sendAtMessage(event, "环球时间\r\n" + this.getWithCache());
    }


    private String getWithCache() {
        Instant now = Instant.now();
        if (this.cacheTime == null || now.isAfter(this.cacheTime)) {
            this.cache = this.build(now);
            ZonedDateTime expireTime = now.atZone(zone_00)
                .withNano(0)
                .withSecond(0)
                .plusMinutes(1);
            this.cacheTime = expireTime.toInstant();
        }
        return this.cache;
    }


    private String build(Instant instant) {

        ZonedDateTime china = Instant.from(instant).atZone(zone_CN);

        int chinaYear = china.getYear();
        int chinaDate = china.getDayOfYear();

        StringBuilder builder = new StringBuilder();

        builder.append("世界协调时(UTC) ");
        builder.append(FORMATTER_UTC.format(instant));
        builder.append("\r\n");

        for (Map.Entry<String, ZoneId> entry : this.TIME_ZONE.entrySet()) {

            var k = entry.getKey();
            var v = entry.getValue();

            builder.append(k);
            builder.append(" ");

            // =================================================================

            DateTimeFormatter withZone = FORMATTER_NOR.withZone(v);

            String format = withZone.format(instant);

            builder.append(format);

            // =================================================================

            ZonedDateTime local = Instant.from(instant).atZone(v);

            int localYear = local.getYear();
            int localDate = local.getDayOfYear();

            ZoneRules localRules = v.getRules();

            if (localRules.isDaylightSavings(instant)) {
                builder.append(" 夏令时");
            }

            // =================================================================

            int yearBias = chinaYear - localYear;
            int dateBias = chinaDate - localDate;

            if (yearBias == 0) {

                if (dateBias < 0) {
                    builder.append(" 明天");
                    builder.append(",");
                    builder.append(local.getDayOfMonth());
                    builder.append("日");
                } else if (dateBias > 0) {
                    builder.append(" 昨天");
                    builder.append(",");
                    builder.append(local.getDayOfMonth());
                    builder.append("日");
                } else {
                    builder.append(" 今天");
                }

            } else if (yearBias > 0) {
                builder.append(" 昨天");
                builder.append(",");
                builder.append(localYear);
                builder.append("年");
                builder.append(local.getDayOfMonth());
                builder.append("日");
            } else {
                builder.append(" 明天");
                builder.append(",");
                builder.append(localYear);
                builder.append("年");
                builder.append(local.getDayOfMonth());
                builder.append("日");
            }

            builder.append("\r\n");
        }

        builder.append("亚洲中国(UTC+8) ");
        builder.append(FORMATTER_RPC.format(instant));

        return builder.toString();
    }


}
