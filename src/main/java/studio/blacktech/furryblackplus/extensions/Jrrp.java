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
import studio.blacktech.furryblackplus.core.handler.common.Command;
import studio.blacktech.furryblackplus.core.handler.annotation.Executor;
import studio.blacktech.furryblackplus.core.handler.EventHandlerExecutor;
import studio.blacktech.furryblackplus.core.common.time.TimeTool;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;


@Executor(
    value = "Executor-Jrrp",
    outline = "今日运气",
    description = "查看今天的运气值 - 大失败酱",
    command = "jrrp",
    usage = "/jrrp - 查看今日运气",
    privacy = {
        "获取命令发送人",
        "存储用户与运气对应表 - 每日UTC+8 00:00 清空"
    }
)
public class Jrrp extends EventHandlerExecutor {

    private Thread thread;

    private Map<Long, Integer> JRRP;

    private File JRRP_FILE;

    @Override
    public void init() {

        this.initRootFolder();
        this.initDataFolder();

        this.JRRP_FILE = this.initDataFile("jrrp.txt");

        this.JRRP = new ConcurrentHashMap<>();

        if (TimeTool.isToday(this.JRRP_FILE.lastModified())) {
            for (String line : this.readFile(this.JRRP_FILE)) {
                String[] temp = line.split(":");
                Long user = Long.parseLong(temp[0].trim());
                Integer jrrp = Integer.parseInt(temp[1].trim());
                this.JRRP.put(user, jrrp);
            }
            this.logger.seek("从持久化文件中读取了" + this.JRRP.size() + "条数据");
        } else {
            this.logger.seek("持久化文件已过期");
        }

        this.thread = new Thread(this::schedule);
        this.thread.setName("executor-jrrp-task");
    }


    @Override
    public void boot() {
        FurryBlack.scheduleAtNextDayFixedRate(this.thread, 1000 * 3600 * 24, TimeUnit.MILLISECONDS);
    }

    @Override
    public void shut() {
        this.thread.interrupt();
        try {
            this.thread.join();
        } catch (InterruptedException exception) {
            this.logger.error("等待计划任务结束失败", exception);
            if (FurryBlack.isShutModeDrop()) Thread.currentThread().interrupt();
        }
        try (FileWriter fileWriter = new FileWriter(this.JRRP_FILE, false)) {
            for (Map.Entry<Long, Integer> entry : this.JRRP.entrySet()) {
                var k = entry.getKey();
                var v = entry.getValue();
                fileWriter.write(String.valueOf(k));
                fileWriter.write(":");
                fileWriter.write(String.valueOf(v));
                fileWriter.write("\n");
            }
            fileWriter.flush();
        } catch (IOException exception) {
            this.logger.warning("保存数据失败", exception);
        }
    }


    @Override
    public void handleUsersMessage(UserMessageEvent event, Command command) {
        FurryBlack.sendMessage(event, this.generate(event.getSender().getId()));
    }

    @Override
    public void handleGroupMessage(GroupMessageEvent event, Command command) {
        FurryBlack.sendAtMessage(event, this.generate(event.getSender().getId()));
    }

    private String generate(long userid) {
        int luck;
        if (this.JRRP.containsKey(userid)) {
            luck = this.JRRP.get(userid);
        } else {
            luck = ThreadLocalRandom.current().nextInt(101);
            this.JRRP.put(userid, luck);
        }
        if (luck == 0) {
            return "今天没有运气!!!";
        } else if (luck == 100) {
            return "今天运气爆表!!!";
        } else {
            return "今天的运气是" + luck + "% !!!";
        }
    }

    private void schedule() {
        this.JRRP.clear();
        try (FileWriter fileWriter = new FileWriter(this.JRRP_FILE, false)) {
            fileWriter.write("");
            fileWriter.flush();
        } catch (IOException exception) {
            this.logger.warning("清空数据失败", exception);
        }
    }

}
