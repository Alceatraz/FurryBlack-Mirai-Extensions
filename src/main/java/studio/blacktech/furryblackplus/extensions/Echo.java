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
import studio.blacktech.furryblackplus.core.handler.EventHandlerExecutor;
import studio.blacktech.furryblackplus.core.handler.annotation.Executor;
import studio.blacktech.furryblackplus.core.handler.common.Command;

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
