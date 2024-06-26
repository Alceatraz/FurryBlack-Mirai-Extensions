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

package top.btswork.furryblack.extensions;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.UserMessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Face;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;
import top.btswork.furryblack.FurryBlack;
import top.btswork.furryblack.core.common.enhance.TimeEnhance;
import top.btswork.furryblack.core.handler.EventHandlerExecutor;
import top.btswork.furryblack.core.handler.annotation.Executor;
import top.btswork.furryblack.core.handler.common.Command;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Executor(
  value = "Executor-Roulette",
  outline = "俄罗斯轮盘赌",
  description = "提供赌注以参与一局俄罗斯轮盘赌",
  command = "roulette",
  usage = "/roulette 筹码 - 加入或者发起一局俄罗斯轮盘赌 重复下注可增加被枪毙的几率",
  privacy = {
    "获取命令发送人",
    "缓存群-成员-回合的数据 并在回合结束后丢弃"
  }
)
public class Roulette extends EventHandlerExecutor {

  private static final String[] ICON = {"1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣"};

  private static final DateTimeFormatter FORMATTER = TimeEnhance.pattern("mm:ss");

  private ConcurrentHashMap<Long, RouletteRound> rounds;

  @Override
  public void init() {
    rounds = new ConcurrentHashMap<>();
  }

  @Override
  public void boot() {}

  @Override
  public void shut() {}

  @Override
  public void handleUsersMessage(UserMessageEvent event, Command command) {
    FurryBlack.sendMessage(event, "好的，没有问题，成全你：");
    FurryBlack.sendMessage(event, new Face(Face.SHOU_QIANG).plus("\uD83D\uDCA5"));
    FurryBlack.sendMessage(event, new Face(Face.SHOU_QIANG).plus("\uD83D\uDCA5"));
    FurryBlack.sendMessage(event, new Face(Face.SHOU_QIANG).plus("\uD83D\uDCA5"));
    FurryBlack.sendMessage(event, new Face(Face.SHOU_QIANG).plus("\uD83D\uDCA5"));
    FurryBlack.sendMessage(event, new Face(Face.SHOU_QIANG).plus("\uD83D\uDCA5"));
    FurryBlack.sendMessage(event, new Face(Face.SHOU_QIANG).plus("\uD83D\uDCA5"));
  }

  @Override
  public synchronized void handleGroupMessage(GroupMessageEvent event, Command command) {

    Group group = event.getGroup();

    if (!command.hasCommandBody()) {
      FurryBlack.sendMessage(event, "你必须下注");
      return;
    }

    long current = System.currentTimeMillis();

    RouletteRound round;

    if (rounds.containsKey(group.getId())) {
      round = rounds.get(group.getId());
      if (round.getExpireTime().toEpochMilli() - current < 0) {
        rounds.remove(group.getId());
        round = new RouletteRound();
        rounds.put(group.getId(), round);
        logger.debug("{} -> 已超时", group.getId());
      }
    } else {
      round = new RouletteRound();
      rounds.put(group.getId(), round);
      logger.debug("{} -> 新对局", group.getId());
    }

    //

    if (round.join(event, command)) {

      if (round.isSinglePlayer()) {

        RouletteRound.PlayerJetton loser = round.gamblers.getFirst();

        long loserID = loser.member.getId();

        String jetton = round.getAllJetton(loserID);

        FurryBlack.sendAtMessage(event, new PlainText("好的，没有问题，成全你。📞俊·马尔福先生，有事麻烦您一下\r\n")
          .plus(new At(loserID))
          .plus(new Face(Face.SHOU_QIANG))
          .plus("\uD83D\uDCA5\r\n")
          .plus(new Face(Face.SHOU_QIANG))
          .plus("\uD83D\uDCA5\r\n")
          .plus(new Face(Face.SHOU_QIANG))
          .plus("\uD83D\uDCA5\r\n")
          .plus(new Face(Face.SHOU_QIANG))
          .plus("\uD83D\uDCA5\r\n")
          .plus(new Face(Face.SHOU_QIANG))
          .plus("\uD83D\uDCA5\r\n")
          .plus(new Face(Face.SHOU_QIANG))
          .plus("\uD83D\uDCA5\r\n目标已被击毙: " + FurryBlack.getMemberMappedNickName(loser.member) + "\r\n掉落了以下物品:" + jetton)
        );

        logger.debug("{}:{} -> 单人 {}", event.getGroup().getId(), event.getSender().getId(), jetton);

      } else {

        int bullet = round.roll();

        RouletteRound.PlayerJetton loser = round.gamblers.get(bullet);

        long loserID = loser.member.getId();

        String jetton = round.getAllJetton(loserID);

        Message message = new PlainText("名单已凑齐 装填子弹中\r\n");

        for (int i = 0; i < 6; i++) {
          RouletteRound.PlayerJetton temp = round.gamblers.get(i);
          message = message.plus(ICON[i] + " " + FurryBlack.getMemberMappedNickName(temp.member) + " ");
          message = message.plus(new Face(Face.SHOU_QIANG));
          if (i == round.getLoser()) {
            message = message.plus("\uD83D\uDCA5\r\n"); // 💥
          } else {
            message = message.plus("\r\n");
          }
        }
        message = message.plus("\r\n目标已被击毙: ");
        message = message.plus(new At(loserID));
        message = message.plus("\r\n掉落了以下物品: " + jetton);
        FurryBlack.sendMessage(event, message);

        logger.debug("{}  -> {} : {}", group.getId(), loserID, jetton);

      }

      rounds.remove(group.getId());

    } else {

      StringBuilder builder = new StringBuilder();

      builder.append(" 俄罗斯轮盘 - 当前人数 (");
      builder.append(round.getGamblers().size());
      builder.append("/6)\r\n");

      int i = 0;

      int size = round.getGamblers().size();

      for (; i < size; i++) {
        RouletteRound.PlayerJetton temp = round.getGamblers().get(i);
        builder.append(ICON[i]);
        builder.append(" ");
        builder.append(temp.getMember().getId());
        builder.append(" - ");
        String jetton = temp.getJetton();
        if (jetton.length() > 15) {
          builder.append(jetton, 0, 12).append("...");
        } else {
          builder.append(jetton);
        }
        builder.append("\r\n");
      }

      for (; i < 6; i++) {
        builder.append(ICON[i]);
        builder.append(" - 等待加入\r\n");
      }

      Instant instant = Instant.ofEpochMilli(round.getExpireTime().toEpochMilli() - current);

      builder.append("剩余时间 - ");
      builder.append(FORMATTER.format(instant));

      FurryBlack.sendMessage(event, new Face(Face.SHOU_QIANG).plus(builder.toString()));

      logger.debug("{}  -> 加入 {} : {}", group.getId(), event.getSender().getId(), command.getCommandBody());

    }

  }

  private static class RouletteRound {

    private final Instant expireTime = Instant.ofEpochMilli(System.currentTimeMillis() + 600000);
    private final CopyOnWriteArrayList<PlayerJetton> gamblers = new CopyOnWriteArrayList<>();

    private boolean hint = true;
    private int loser = 6;

    public boolean join(GroupMessageEvent event, Command command) {
      if (gamblers.size() > 6) return false;
      if (hint && gamblers.stream().anyMatch(item -> item.getMember().getId() == event.getSender().getId())) {
        FurryBlack.sendAtMessage(event, "✔️ 经科学证实重复下注可有效增加被枪毙的机率");
        hint = false;
      }
      gamblers.add(new PlayerJetton(event.getSender(), command.getCommandBody(200)));
      return gamblers.size() == 6;
    }

    public int roll() {
      loser = ThreadLocalRandom.current().nextInt(6);
      return loser;
    }

    public int getLoser() {
      return loser;
    }

    public boolean isSinglePlayer() {
      return gamblers.stream().map(item -> item.getMember().getId()).collect(Collectors.toUnmodifiableSet()).size() == 1;
    }

    public String getAllJetton(long id) {
      List<PlayerJetton> jettons = gamblers.stream().filter(item -> item.getMember().getId() == id).toList();
      StringBuilder builder = new StringBuilder();
      for (RouletteRound.PlayerJetton jetton : jettons) {
        builder.append("\r\n");
        builder.append(jetton.getJetton());
      }
      return builder.toString();
    }

    public Instant getExpireTime() {
      return expireTime;
    }

    public List<PlayerJetton> getGamblers() {
      return gamblers;
    }

    private record PlayerJetton(Member member, String jetton) {

      public Member getMember() {
        return member;
      }

      public String getJetton() {
        return jetton;
      }
    }
  }
}

