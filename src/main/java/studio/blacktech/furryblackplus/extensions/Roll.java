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

import java.security.SecureRandom;


@Executor(
    value = "Executor-Roll",
    outline = "随机抽数",
    description = "生成随机数或者窥探本质",
    command = "roll",
    usage = {
        "/roll - 抽取真假",
        "/roll 数字 - 从零到给定数字任选一个数字[0,x)",
        "/roll 数字 数字 - 从给定两个数字中间抽取一个[x,y)"
    },
    privacy = {
        "获取命令发送人"
    }
)
public class Roll extends EventHandlerExecutor {


    @Override
    public void init() {}

    @Override
    public void boot() {}

    @Override
    public void shut() {}

    @Override
    public void handleUsersMessage(UserMessageEvent event, Command command) {
        FurryBlack.sendMessage(event, this.roll(command));
    }

    @Override
    public void handleGroupMessage(GroupMessageEvent event, Command command) {
        FurryBlack.sendAtMessage(event, this.roll(command));
    }

    private String roll(Command command) {

        String res;
        SecureRandom random = new SecureRandom();

        switch (command.getParameterLength()) {

            // ============================================================

            case 0 -> res = random.nextBoolean() ? " 1️⃣" : " 0️⃣";


            // ============================================================

            case 1 -> {
                int range;
                try {
                    range = Integer.parseInt(command.getParameterSegment(0));
                    res = Integer.toString(random.nextInt(range));
                } catch (Exception ignored) {
                    res = command.getCommandBody(200) + " 是 " + (random.nextBoolean() ? " 1️⃣" : " 0️⃣");
                }
            }

            // ============================================================

            case 2 -> {
                int min;
                int max;
                try {
                    min = Integer.parseInt(command.getParameterSegment(0));
                    max = Integer.parseInt(command.getParameterSegment(1));
                } catch (Exception ignored) {
                    return "参数必须是罗马数字";
                }
                int temp = random.nextInt(max - min);
                res = Integer.toString(temp + min);
            }
            default -> res = command.getCommandBody(200) + " 是 " + (random.nextBoolean() ? " 1️⃣" : " 0️⃣");
        }

        return res;

    }

}
