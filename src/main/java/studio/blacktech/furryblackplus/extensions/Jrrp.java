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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Properties;
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
    "存储用户与运气对应表 - 每日00:00清空"
  }
)
public class Jrrp extends EventHandlerExecutor {

  private Thread thread;

  private Schema schema;

  @Override
  public void init() throws InitException {

    ensureRootFolder();
    ensureDataFolder();

    Path storage = ensureDataFile("storage.properties");
    schema = new Schema(storage);
    schema.load();
  }

  @Override
  public void boot() {

    thread = Thread.ofVirtual().name("jrrp-worker").start(() -> {
      //noinspection InfiniteLoopStatement
      while (true) {
        long nextDay = TimeEnhance.toNextDay();
        logger.debug("休眠 " + nextDay);
        try {
          //noinspection BusyWait
          Thread.sleep(nextDay);
        } catch (InterruptedException exception) {
          throw new RuntimeException(exception);
        }
        schema.clear();
        logger.debug("缓存已清除");
      }
    });

    logger.debug("线程已注册");
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
    schema.save();
    logger.debug("线程已退出");
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
    int luck = schema.get(userid);
    logger.debug(userid + " -> " + luck + "%");
    if (luck == 0) {
      return "今天没有运气!!!";
    } else if (luck == 100) {
      return "今天运气爆表!!!";
    } else {
      return "今天的运气是" + luck + "% !!!";
    }
  }

  private boolean isToday(long time) {
    LocalDate now = LocalDate.now();
    LocalDateTime that = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), TimeEnhance.SYSTEM_OFFSET);
    return now.getYear() == that.getYear() && now.getDayOfYear() == that.getDayOfYear();
  }

  private class Schema {

    private final Path storage;
    private final Map<Long, Integer> cache;

    private Schema(Path storage) {
      this.storage = storage;
      this.cache = new ConcurrentHashMap<>();
    }

    private void load() {

      if (!Files.exists(storage)) {
        logger.seek("持久化文件不存在");
        return;
      }

      long lastModifyEpoch = FileEnhance.lastModifyEpoch(storage);

      if (isToday(lastModifyEpoch)) {
        Properties properties = new Properties();
        try {
          InputStream inputStream = Files.newInputStream(storage);
          properties.load(inputStream);
        } catch (IOException exception) {
          throw new RuntimeException(exception);
        }
        properties.forEach((k, v) -> cache.put(Long.valueOf(String.valueOf(k)), Integer.valueOf(String.valueOf(v))));
        logger.seek("从持久化文件中读取了" + cache.size() + "条数据");
      } else {
        try {
          Files.delete(storage);
        } catch (IOException exception) {
          throw new RuntimeException(exception);
        }
        logger.seek("持久化文件已过期");
      }

    }

    private void save() {
      Properties properties = new Properties();
      cache.forEach((k, v) -> properties.setProperty(k.toString(), v.toString()));
      try {
        Files.deleteIfExists(storage);
        Files.createFile(storage);
      } catch (IOException exception) {
        throw new RuntimeException(exception);
      }
      try (OutputStream outputStream = Files.newOutputStream(storage)) {
        properties.store(outputStream, "SAVED " + System.currentTimeMillis());
      } catch (IOException exception) {
        throw new RuntimeException(exception);
      }
    }

    private int get(long key) {
      Integer i = cache.get(key);
      if (i == null) {
        int nextInt = ThreadLocalRandom.current().nextInt(101);
        put(key, nextInt);
        return nextInt;
      } else {
        return i;
      }
    }

    private void put(Long key, Integer value) {
      cache.put(key, value);
      save();
    }

    public void clear() {
      cache.clear();
      try {
        Files.deleteIfExists(storage);
      } catch (IOException exception) {
        throw new RuntimeException(exception);
      }
    }
  }

}
