/*
 * Copyright (C) 2021 Alceatraz @ BlackTechStudio
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the BTS Anti-Commercial & GNU Affero General.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * BTS Anti-Commercial & GNU Affero General Public License for more details.
 *
 * You should have received a copy of the BTS Anti-Commercial & GNU Affero
 * General Public License along with this program in README or LICENSE.
 */

package studio.blacktech.furryblackplus.extensions;

import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.UserMessageEvent;
import studio.blacktech.furryblackplus.FurryBlack;
import studio.blacktech.furryblackplus.core.handler.common.Command;
import studio.blacktech.furryblackplus.core.handler.annotation.Executor;
import studio.blacktech.furryblackplus.core.handler.EventHandlerExecutor;

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


    private static final String[] DICES = {"0️⃣", "1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣"};


    @Override
    public void init() {}

    @Override
    public void boot() {}

    @Override
    public void shut() {}

    @Override
    public void handleUsersMessage(UserMessageEvent event, Command command) {
        FurryBlack.sendMessage(event, this.dice());
    }

    @Override
    public void handleGroupMessage(GroupMessageEvent event, Command command) {
        FurryBlack.sendAtMessage(event, this.dice());
    }

    private String dice() {
        int i = ThreadLocalRandom.current().nextInt(61);
        if (i == 0) {
            return DICES[0];
        } else {
            return DICES[i / 10 + 1];
        }
    }
}
