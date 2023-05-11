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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    "按命令发送人缓存每日语料 - 每日UTC+8 00:00 清空"
  }
)
public class Jrjt extends EventHandlerExecutor {

  private final static DslJson<ShaDiaoAppResponse> dslJson;

  static {
    DslJson.Settings<ShaDiaoAppResponse> withRuntime = Settings.withRuntime();
    dslJson = new DslJson<>(withRuntime.allowArrayFormat(true).includeServiceLoader());
  }

  private Thread thread;

  private Map<Long, String> JRJT;

  private Request request;
  private OkHttpClient httpClient;

  private Path JRJT_FILE;

  @Override
  public void init() {

    ensureRootFolder();
    ensureDataFolder();

    JRJT_FILE = ensureDataFile("jrjt.txt");

    JRJT = new ConcurrentHashMap<>();

    httpClient = new OkHttpClient.Builder()
      .callTimeout(2, TimeUnit.SECONDS)
      .readTimeout(2, TimeUnit.SECONDS)
      .writeTimeout(2, TimeUnit.SECONDS)
      .connectTimeout(2, TimeUnit.SECONDS)
      .build();

    request = new Request.Builder().url("https://api.shadiao.pro/du").get().build();

    long lastModifyEpoch = FileEnhance.lastModifyEpoch(JRJT_FILE);

    if (isToday(lastModifyEpoch)) {
      Base64.Decoder decoder = Base64.getDecoder();
      for (String line : readLine(JRJT_FILE)) {
        String[] temp = line.split(":");
        Long user = Long.parseLong(temp[0].trim());
        byte[] decode = decoder.decode(temp[1]);
        String string = new String(decode, StandardCharsets.UTF_8);
        JRJT.put(user, string);
      }
      logger.seek("从持久化文件中读取了" + JRJT.size() + "条数据");
    } else {
      logger.seek("持久化文件已过期");
    }

    thread = new Thread(this::schedule);
    thread.setName("executor-jrjt-task");

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
    Base64.Encoder encoder = Base64.getEncoder();
    List<String> strings = JRJT.entrySet().stream()
      .map(it -> {
        var k = it.getKey();
        var v = it.getValue();
        var t = encoder.encodeToString(v.getBytes(StandardCharsets.UTF_8));
        return k + ":" + t;
      })
      .toList();
    write(JRJT_FILE, strings);
  }

  @Override
  public void handleUsersMessage(UserMessageEvent event, Command command) {
    FurryBlack.sendMessage(event, generate(event.getSender().getId()));
  }

  @Override
  public void handleGroupMessage(GroupMessageEvent event, Command command) {
    FurryBlack.sendAtMessage(event, generate(event.getSender().getId()));
  }

  private String generate(long user) {
    String message;
    if (JRJT.containsKey(user)) {
      message = JRJT.get(user);
    } else {
      Call newCall = httpClient.newCall(request);
      try (Response response = newCall.execute()) {
        InputStream inputStream = Objects.requireNonNull(response.body()).byteStream();
        ShaDiaoAppResponse deserialize = dslJson.deserialize(ShaDiaoAppResponse.class, inputStream);
        message = Objects.requireNonNull(deserialize).data.text;
        JRJT.put(user, message);
      } catch (IOException exception) {
        logger.error("沙雕服务器连接失败", exception);
        message = "沙雕App的服务器炸了";
      }
    }
    return message;
  }

  private void schedule() {
    JRJT.clear();
    write(JRJT_FILE, "");
    logger.info("定时任务 -> 清空每日数据");
  }

  private boolean isToday(long epoch) {
    LocalDate now = LocalDate.now();
    LocalDate date = LocalDate.ofInstant(Instant.ofEpochMilli(epoch), FurryBlack.SYSTEM_OFFSET);
    return now.getYear() == date.getYear() && now.getDayOfYear() == date.getDayOfYear();
  }

  @CompiledJson
  public static class ShaDiaoAppResponse {

    public Data data;

    public static class Data {
      public String type;
      public String text;
    }
  }
}
