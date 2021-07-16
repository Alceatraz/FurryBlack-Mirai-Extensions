package studio.blacktech.furryblackplus.extensions;

import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.UserMessageEvent;
import studio.blacktech.furryblackplus.Driver;
import studio.blacktech.furryblackplus.core.annotation.Component;
import studio.blacktech.furryblackplus.core.interfaces.EventHandlerExecutor;
import studio.blacktech.furryblackplus.core.utilties.Command;
import studio.blacktech.furryblackplus.core.utilties.LoggerX;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;


@Component(
    artificial = "Executor_Time",
    name = "环球时间",
    description = "现在都几点了，为什么那个孩子还在水群",
    privacy = {
        "无"
    },
    command = "time",
    usage = {
        "/time - 查看世界时间"
    }
)
public class Time extends EventHandlerExecutor {


    private static final ZoneId zone_00 = ZoneId.of("UTC");
    private static final ZoneId zone_CN = ZoneId.of("Asia/Shanghai");

    private Integer hour;
    private String cache;

    private Map<String, ZoneId> TIME_ZONE;


    @Override
    public void load() {

        this.initRootFolder();
        this.initConfFolder();

        this.TIME_ZONE = new LinkedHashMap<>();
        File FILE_TIMEZONE = this.initConfFile("timezone.txt");
        for (String line : this.readFile(FILE_TIMEZONE)) {
            if (!line.contains(":")) {
                this.logger.warning("配置无效 " + line);
                continue;
            }
            String[] temp = line.split(":");
            if (temp.length != 2) {
                this.logger.warning("配置无效 " + line);
                continue;
            }
            ZoneId timeZone = ZoneId.of(temp[1]);
            if (!temp[1].equals("GMT") && timeZone.getId().equals("GMT")) this.logger.warning("配置无效 TimeZone将不可识别的区域转换为GMT " + line);
            this.TIME_ZONE.put(temp[0], timeZone);
            this.logger.seek("添加时区 " + temp[0] + " -> " + timeZone.getId());
        }
    }

    @Override
    public void boot() { }

    @Override
    public void shut() { }

    @Override
    public void handleUsersMessage(UserMessageEvent event, Command command) {
        Driver.sendMessage(event, this.getTime());
    }

    @Override
    public void handleGroupMessage(GroupMessageEvent event, Command command) {
        Driver.sendAtMessage(event, this.getTime());
    }

    private String getTime() {
        int currentHour = LocalDateTime.now().getHour();
        if (this.hour == null || this.hour != currentHour) {
            this.hour = currentHour;
            StringBuilder builder = new StringBuilder();
            builder.append("\r\n世界协调时(UTC) ").append(LoggerX.format("yyyy-MM-dd HH:mm", zone_00)).append("\r\n");
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
