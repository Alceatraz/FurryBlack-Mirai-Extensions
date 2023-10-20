package studio.blacktech.furryblackplus.extensions;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.DslJson;
import com.dslplatform.json.runtime.Settings;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.UserMessageEvent;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
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
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Executor(
  value = "Executor-Jrjt",
  outline = "今日鸡汤",
  description = "对随机毒鸡汤API的封装 不做任何内容处理 所有语料来自于 https://api.shadiao.pro/du",
  command = "jrjt",
  usage = {
    "/jrjt - 每天送你一碗热气腾腾的翔"
  },
  privacy = {
    "获取命令发送人",
    "按命令发送人缓存每日语料 - 每日00:00清空"
  }
)
public class Jrjt extends EventHandlerExecutor {

  private final static DslJson<ShadiaoAppResponse> dslJson;

  static {
    DslJson.Settings<ShadiaoAppResponse> withRuntime = Settings.withRuntime();
    dslJson = new DslJson<>(withRuntime.allowArrayFormat(true).includeServiceLoader());
  }

  private Request request;
  private OkHttpClient httpClient;

  private Schema schema;
  private Thread thread;

  @Override
  public void init() {

    httpClient = new OkHttpClient.Builder()
      .callTimeout(2, TimeUnit.SECONDS)
      .readTimeout(2, TimeUnit.SECONDS)
      .writeTimeout(2, TimeUnit.SECONDS)
      .connectTimeout(2, TimeUnit.SECONDS)
      .build();

    request = new Request.Builder().url("https://api.shadiao.pro/du").get().build();

    ensureRootFolder();
    ensureDataFolder();

    Path storage = ensureDataFile("storage.properties");
    schema = new Schema(storage);
    schema.load();
  }

  @Override
  public void boot() {
    thread = Thread.ofVirtual().name("jrjt-worker").start(() -> {
      //noinspection InfiniteLoopStatement
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
    return schema.get(userid);
  }

  private class Schema {

    private final Path storage;
    private final Map<Long, String> cache;

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
        properties.forEach((k, v) -> cache.put(Long.parseLong(String.valueOf(k)), Common.decode(v)));
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
      cache.forEach((k, v) -> properties.setProperty(k.toString(), v));
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

    private String get(long key) {
      String i = cache.get(key);
      if (i == null || "沙雕App的服务器炸了".equals(i)) {
        i = call();
        put(key, i);
        logger.debug(key + " -> 新 " + i);
      } else {
        logger.debug(key + " -> 旧 " + i);
      }
      return i;
    }

    private void put(Long key, String value) {
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

  private String call() {
    Call newCall = httpClient.newCall(request);
    try (Response response = newCall.execute()) {
      InputStream inputStream = Objects.requireNonNull(response.body()).byteStream();
      ShadiaoAppResponse deserialize = dslJson.deserialize(ShadiaoAppResponse.class, inputStream);
      return Objects.requireNonNull(deserialize).data.text;
    } catch (IOException exception) {
      logger.error("沙雕服务器连接失败", exception);
      return "沙雕App的服务器炸了";
    }
  }

  @SuppressWarnings("unused")

  @CompiledJson
  public static class ShadiaoAppResponse {

    public Data data;

    public static class Data {
      public String type;
      public String text;
    }
  }

}
