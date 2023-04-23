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
import studio.blacktech.furryblackplus.core.handler.EventHandlerExecutor;
import studio.blacktech.furryblackplus.core.handler.annotation.Executor;
import studio.blacktech.furryblackplus.core.handler.common.Command;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    initRootFolder();
    initDataFolder();

    JRRP_FILE = initDataFile("jrrp.txt");

    JRRP = new ConcurrentHashMap<>();

    if (isToday(JRRP_FILE.lastModified())) {
      for (String line : readFile(JRRP_FILE)) {
        String[] temp = line.split(":");
        Long user = Long.parseLong(temp[0].trim());
        Integer jrrp = Integer.parseInt(temp[1].trim());
        JRRP.put(user, jrrp);
      }
      logger.seek("从持久化文件中读取了" + JRRP.size() + "条数据");
    } else {
      logger.seek("持久化文件已过期");
    }

    thread = new Thread(this::schedule);
    thread.setName("executor-jrrp-task");
  }

  @Override
  public void boot() {
    FurryBlack.scheduleAtNextDayFixedRate(thread, 1000 * 3600 * 24, TimeUnit.MILLISECONDS);
  }

  @Override
  public void shut() {
    thread.interrupt();
    try {
      thread.join();
    } catch (InterruptedException exception) {
      logger.error("等待计划任务结束失败", exception);
      if (FurryBlack.isShutModeDrop()) Thread.currentThread().interrupt();
    }
    try (FileWriter fileWriter = new FileWriter(JRRP_FILE, false)) {
      for (Map.Entry<Long, Integer> entry : JRRP.entrySet()) {
        var k = entry.getKey();
        var v = entry.getValue();
        fileWriter.write(String.valueOf(k));
        fileWriter.write(":");
        fileWriter.write(String.valueOf(v));
        fileWriter.write("\n");
      }
      fileWriter.flush();
    } catch (IOException exception) {
      logger.warning("保存数据失败", exception);
    }
  }

  @Override
  public void handleUsersMessage(UserMessageEvent event, Command command) {
    FurryBlack.sendMessage(event, generate(event.getSender().getId()));
  }

  @Override
  public void handleGroupMessage(GroupMessageEvent event, Command command) {
    FurryBlack.sendAtMessage(event, generate(event.getSender().getId()));
  }

  private String generate(long userid) {
    int luck;
    if (JRRP.containsKey(userid)) {
      luck = JRRP.get(userid);
    } else {
      luck = ThreadLocalRandom.current().nextInt(101);
      JRRP.put(userid, luck);
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
    JRRP.clear();
    try (FileWriter fileWriter = new FileWriter(JRRP_FILE, false)) {
      fileWriter.write("");
      fileWriter.flush();
    } catch (IOException exception) {
      logger.warning("清空数据失败", exception);
    }
  }

  private boolean isToday(long time) {
    LocalDate now = LocalDate.now();
    LocalDateTime that = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), FurryBlack.SYSTEM_OFFSET);
    return now.getYear() == that.getYear() && now.getDayOfYear() == that.getDayOfYear();
  }

}
