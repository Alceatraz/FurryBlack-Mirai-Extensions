package top.btswork.furryblack.extensions;

import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.UserMessageEvent;
import top.btswork.furryblack.FurryBlack;
import top.btswork.furryblack.core.handler.EventHandlerExecutor;
import top.btswork.furryblack.core.handler.annotation.Executor;
import top.btswork.furryblack.core.handler.common.Command;

@Executor(
  value = "Executor-Echo",
  outline = "回显测试",
  description = "Ping! Pong!",
  command = "echo",
  usage = {
    "/echo - Ping!Pong!",
    "/echo XXX - 原样返回"
  },
  privacy = {
    "获取命令发送人"
  }
)
public class Echo extends EventHandlerExecutor {

  @Override
  public void init() {}

  @Override
  public void boot() {}

  @Override
  public void shut() {}

  @Override
  public void handleUsersMessage(UserMessageEvent event, Command command) {
    FurryBlack.sendMessage(event, command.hasCommandBody() ? command.getCommandBody() : "Pang!");
  }

  @Override
  public void handleGroupMessage(GroupMessageEvent event, Command command) {
    FurryBlack.sendAtMessage(event, command.hasCommandBody() ? command.getCommandBody() : "Pang!");
  }

}
