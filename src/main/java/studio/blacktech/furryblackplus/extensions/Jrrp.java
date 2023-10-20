package studio.blacktech.furryblackplus.extensions;

import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.UserMessageEvent;
import studio.blacktech.furryblackplus.FurryBlack;
import studio.blacktech.furryblackplus.core.common.enhance.FileEnhance;
import studio.blacktech.furryblackplus.core.common.enhance.TimeEnhance;
import studio.blacktech.furryblackplus.core.handler.EventHandlerExecutor;
import studio.blacktech.furryblackplus.core.handler.annotation.Executor;
import studio.blacktech.furryblackplus.core.handler.common.Command;
import studio.blacktech.furryblackplus.extensions.common.Common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
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

  private Schema schema;
  private Thread thread;

  @Override
  public void init() {

    ensureRootFolder();
    ensureDataFolder();

    Path storage = ensureDataFile("storage.properties");
    schema = new Schema(storage);
    schema.load();
  }

  @Override
  public void boot() {
    thread = Thread.ofVirtual().name("jrrp-worker").start(() -> {
      while (true) {
        long nextDay = TimeEnhance.toNextDay();
        long sleep = nextDay - Instant.now().toEpochMilli();
        logger.info("清理线程计划于 {} -> {} ", nextDay, sleep);
        try {
          //noinspection BusyWait
          Thread.sleep(sleep);
        } catch (InterruptedException exception) {
          break;
        }
        schema.clear();
        logger.info("缓存已清除");
      }
      logger.info("清理线程已退出");
    });
    logger.info("清理线程已启动");
  }

  @Override
  public void shut() {
    thread.interrupt();
    try {
      thread.join();
    } catch (InterruptedException exception) {
      throw new RuntimeException("等待计划任务结束失败", exception);
    }
    schema.save();
    logger.info("数据缓存已保存 -> {} 条", schema.cache.size());
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
    if (luck == 0) {
      return "今天没有运气!!!";
    } else if (luck == 100) {
      return "今天运气爆表!!!";
    } else {
      return "今天的运气是" + luck + "% !!!";
    }
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
        logger.info("持久化文件不存在");
        return;
      }

      long lastModifyEpoch = FileEnhance.lastModifyEpoch(storage);

      if (Common.isToday(lastModifyEpoch)) {
        Properties properties = new Properties();
        try {
          InputStream inputStream = Files.newInputStream(storage);
          properties.load(inputStream);
        } catch (IOException exception) {
          throw new RuntimeException(exception);
        }
        properties.forEach((k, v) -> cache.put(Long.valueOf(String.valueOf(k)), Integer.valueOf(String.valueOf(v))));
        logger.info("从持久化文件中读取了 {} 条数据", cache.size());
      } else {
        try {
          Files.delete(storage);
        } catch (IOException exception) {
          throw new RuntimeException(exception);
        }
        logger.info("持久化文件已过期");
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
        logger.info(key + " -> 新 " + nextInt + "%");
        return nextInt;
      } else {
        logger.info(key + " -> 旧 " + i + "%");
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
