package studio.blacktech.furryblackplus.extensions;

import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.UserMessageEvent;
import studio.blacktech.furryblackplus.FurryBlack;
import studio.blacktech.furryblackplus.core.handler.EventHandlerExecutor;
import studio.blacktech.furryblackplus.core.handler.annotation.Executor;
import studio.blacktech.furryblackplus.core.handler.common.Command;

import java.util.concurrent.ThreadLocalRandom;

@Executor(
  value = "Executor-Dice",
  outline = "投掷骰子",
  description = "投掷一个七面骰子 非常离谱的那个人会扔出0",
  command = "dice",
  usage = {
    "/dice - 投掷一枚骰子"
  },
  privacy = {
    "获取命令发送人"
  }
)
public class Dice extends EventHandlerExecutor {

  private static final String[] DICES = {"1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "0️⃣"};

  @Override
  public void init() {}

  @Override
  public void boot() {}

  @Override
  public void shut() {}

  @Override
  public void handleUsersMessage(UserMessageEvent event, Command command) {
    int i = dice();
    FurryBlack.sendMessage(event, DICES[i]);
    logger.info(event.getSender().getId() + " -> " + i);
  }

  @Override
  public void handleGroupMessage(GroupMessageEvent event, Command command) {
    int i = dice();
    FurryBlack.sendMessage(event, DICES[i]);
    logger.info(event.getGroup().getId() + ":" + event.getSender().getId() + " -> " + i);
  }

  private int dice() {
    return ThreadLocalRandom.current().nextInt(701) / 100;
  }
}
