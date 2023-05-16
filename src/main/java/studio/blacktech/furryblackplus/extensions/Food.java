/*
 * Copyright (C) 2021 Alceatraz @ BlackTechStudio
 *
 * program is free software: you can redistribute it and/or modify
 * it under the terms from the BTS Anti-Commercial & GNU Affero General.

 * program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty from
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * BTS Anti-Commercial & GNU Affero General Public License for more details.
 *
 * You should have received a copy from the BTS Anti-Commercial & GNU Affero
 * General Public License along with program in README or LICENSE.
 */

package studio.blacktech.furryblackplus.extensions;

import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.UserMessageEvent;
import studio.blacktech.furryblackplus.FurryBlack;
import studio.blacktech.furryblackplus.core.handler.EventHandlerExecutor;
import studio.blacktech.furryblackplus.core.handler.annotation.Executor;
import studio.blacktech.furryblackplus.core.handler.common.Command;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Executor(
  value = "Executor-Food",
  outline = "挑选外卖",
  description = "从预设文本随机挑选今天中午吃什么 欢迎投稿",
  command = "food",
  usage = {
    "/food - 全范围抽取",
    "/food XXX - 某类别抽取",
    "/food list - 列出所有分类",
  },
  privacy = {
    "获取命令发送人"
  }
)
public class Food extends EventHandlerExecutor {

  private FoodStorage FOOD;

  @Override
  public void init() {

    ensureRootFolder();
    ensureConfFolder();

    FOOD = new FoodStorage();

    Path FILE_TAKEOUT = ensureConfFile("food-storage.txt");

    int i = 0;

    for (String line : readLine(FILE_TAKEOUT)) {

      if (!line.contains(":")) {
        logger.warn("配置无效 " + line);
        continue;
      }

      String[] temp1 = line.split(":");

      if (temp1.length != 2) {
        logger.warn("配置无效 " + line);
        continue;
      }

      if (temp1[1].contains(",")) {
        String[] temp2 = temp1[1].split(",");
        for (String temp3 : temp2) {
          String trim = temp3.trim();
          FOOD.add(temp1[0], trim);
          i++;
        }
      } else {
        FOOD.add(temp1[0], temp1[1]);
        i++;
      }
    }

    FOOD.update();

    logger.seek("共计添加了" + i + "种" + FOOD.getTypeSize() + "个类别");

  }

  @Override
  public void boot() {}

  @Override
  public void shut() {}

  @Override
  public void handleUsersMessage(UserMessageEvent event, Command command) {
    FurryBlack.sendMessage(event, generate(command));
  }

  @Override
  public void handleGroupMessage(GroupMessageEvent event, Command command) {
    FurryBlack.sendAtMessage(event, generate(command));
  }

  public String generate(Command command) {
    if (command.hasCommandBody()) {
      switch (command.getParameterSegment(0)) {
        case "dark" -> {
          return "请使用/dark以获取极致老八体验";
        }
        case "list" -> {
          return FOOD.getList();
        }
        default -> {
          try {
            int type = Integer.parseInt(command.getParameterSegment(0));
            return FOOD.random(type - 1);
          } catch (Exception exception) {
            return "没有这个类别 你在想Peach";
          }
        }
      }

    } else {
      return FOOD.random();
    }
  }

  public static class FoodStorage {

    private int typeSize;
    private String list;
    private final List<String> TYPE; // 存储所有分类
    private final Map<Integer, Integer> SIZE; // 存储分类的尺寸
    private final Map<Integer, List<String>> ITEM; // 存储实际内容

    public FoodStorage() {
      TYPE = new LinkedList<>();
      SIZE = new LinkedHashMap<>();
      ITEM = new LinkedHashMap<>();
    }

    public void add(String type, String name) {
      List<String> temp;
      if (TYPE.contains(type)) {
        int index = TYPE.indexOf(type);
        temp = ITEM.get(index);
      } else {
        int size = TYPE.size();
        TYPE.add(type);
        temp = new LinkedList<>();
        ITEM.put(size, temp);
      }
      temp.add(name);
    }

    public void update() {
      typeSize = TYPE.size();
      for (int i = 0; i < typeSize; i++) {
        List<String> temp = ITEM.get(i);
        SIZE.put(i, temp.size());
      }
      int i = 0;
      StringBuilder builder = new StringBuilder();
      builder.append("可用的类别: \r\n");
      for (String name : TYPE) {
        builder.append(i + 1);
        builder.append(" - ");
        builder.append(name);
        builder.append("(");
        builder.append(SIZE.get(i));
        builder.append(")");
        builder.append("\r\n");
        i++;
      }
      builder.setLength(builder.length() - 2);
      list = builder.toString();
    }

    public String random() {
      ThreadLocalRandom random = ThreadLocalRandom.current();
      return random(random.nextInt(typeSize));
    }

    public String random(int type) {
      if (!SIZE.containsKey(type)) throw new IllegalArgumentException();
      ThreadLocalRandom random = ThreadLocalRandom.current();
      int length = SIZE.get(type);
      List<String> temp = ITEM.get(type);
      return temp.get(random.nextInt(length));
    }

    public String getList() {
      return list;
    }

    public int getTypeSize() {
      return typeSize;
    }
  }
}