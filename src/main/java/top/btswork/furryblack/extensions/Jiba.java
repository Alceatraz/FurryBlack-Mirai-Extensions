package top.btswork.furryblack.extensions;

import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.UserMessageEvent;
import top.btswork.furryblack.FurryBlack;
import top.btswork.furryblack.core.handler.EventHandlerExecutor;
import top.btswork.furryblack.core.handler.annotation.Executor;
import top.btswork.furryblack.core.handler.common.Command;

@Executor(
  value = "Executor-Jiba",
  outline = "寄吧生成器",
  description = "根据提供的词语生成寄吧圣经",
  command = "jiba",
  usage = {
    "/jiba 名词 - 根据一个字按照叠字生成",
    "/jiba 简称 全名 - 根据一个字简称和全名生成"
  },
  privacy = {
    "获取命令发送人"
  }
)
public class Jiba extends EventHandlerExecutor {

  private static final String template = """
    X瘾发作最严重的一次，躺在床上，拼命念大悲咒，难受的一直抓自己牛子，以为刷贴吧没事，看到贴吧都在发Y的图，眼睛越来越大都要炸开了一样，拼命扇自己眼睛，越扇越用力，扇到自己眼泪流出来，真的不知道该怎么办，我真的想Y想得要发疯了。我躺在床上会想Y，我洗澡会想Y，我出门会想Y，我走路会想Y，我坐车会想Y，我工作会想Y，我玩手机会想Y，我盯着路边的Y看，我盯着马路对面的Y看，我盯着地铁里的Y看，我盯着网上的Y看，我盯着UP视频里的Y看，我每时每刻眼睛都直直地盯着Y看，像一台雷达一样扫视经过Y身体的每一寸，我真的觉得自己像中邪了一样，我对Y的念想似乎都是病态的了，我好孤独啊!真的好孤独啊！这世界上只有一个的Y为什么不能是属于我的？？？你知道吗？每到深夜，我的眼睛滚烫滚烫，我发病了我要疯狂看Y，我要狠狠看Y，我的眼睛受不了了，Y，我的Y，我的Y，我的Y，我的Y，我的Y，我的Y，我的Y，Y……""";

  @Override
  protected void init() {}

  @Override
  protected void boot() {}

  @Override
  protected void shut() {}

  @Override
  protected void handleUsersMessage(UserMessageEvent event, Command command) {
    String generate = generate(command);
    FurryBlack.sendMessage(event, generate);
  }

  @Override
  protected void handleGroupMessage(GroupMessageEvent event, Command command) {
    String generate = generate(command);
    FurryBlack.sendAtMessage(event, generate);
  }

  private static String generate(Command command) {
    switch (command.getParameterLength()) {
      case 1 -> {
        if (command.getParameterSegment(0).length() != 1) {
          return "必须是一个字";
        } else {
          return generate(command.getParameterSegment(0));
        }
      }
      case 2 -> {
        if (command.getParameterSegment(0).length() != 1) {
          return "第一个参数必须是一个字";
        }
        if (command.getParameterSegment(1).length() != 2) {
          return "第二个参数必须是两个字";
        }
        return generate(command.getParameterSegment(0), command.getParameterSegment(1));
      }
      default -> {
        return "命令用法错误";
      }
    }
  }

  private static String generate(String x) {
    return generate(x, x + x);
  }

  private static String generate(String x, String y) {
    return template.replaceAll("X", x).replaceAll("Y", y);
  }
}
