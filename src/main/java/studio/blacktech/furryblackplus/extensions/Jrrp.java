/*
 * Copyright (C) 2021 Alceatraz @ BlackTechStudio
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms from the BTS Anti-Commercial & GNU Affero General.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty from
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * BTS Anti-Commercial & GNU Affero General Public License for more details.
 *
 * You should have received a copy from the BTS Anti-Commercial & GNU Affero
 * General Public License along with this program in README or LICENSE.
 */

package studio.blacktech.furryblackplus.extensions;

import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.UserMessageEvent;
import studio.blacktech.furryblackplus.FurryBlack;
import studio.blacktech.furryblackplus.core.common.enhance.FileEnhance;
import studio.blacktech.furryblackplus.core.common.enhance.TimeEnhance;
import studio.blacktech.furryblackplus.core.exception.moduels.InitException;
import studio.blacktech.furryblackplus.core.handler.EventHandlerExecutor;
import studio.blacktech.furryblackplus.core.handler.annotation.Executor;
import studio.blacktech.furryblackplus.core.handler.common.Command;

import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

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

  private Path JRRP_FILE;

  @Override
  public void init() throws InitException {

    ensureRootFolder();
    ensureDataFolder();

    JRRP_FILE = ensureDataFile("jrrp.txt");

    JRRP = new ConcurrentHashMap<>();

    long lastModifyEpoch = FileEnhance.lastModifyEpoch(JRRP_FILE);

    if (isToday(lastModifyEpoch)) {
      for (String line : readLine(JRRP_FILE)) {
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
    FurryBlack.scheduleAtFixedRate(thread, TimeEnhance.toNextDay(), TimeEnhance.DURATION_DAY);
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

    List<String> strings = JRRP.entrySet().stream()
      .map(it -> {
        var k = it.getKey();
        var v = it.getValue();
        return k + ":" + v;
      })
      .toList();
    write(JRRP_FILE, strings);

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
    write(JRRP_FILE, "");
    logger.info("定时任务 -> 清空每日数据");
  }

  private boolean isToday(long time) {
    LocalDate now = LocalDate.now();
    LocalDateTime that = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), FurryBlack.SYSTEM_OFFSET);
    return now.getYear() == that.getYear() && now.getDayOfYear() == that.getDayOfYear();
  }

}
