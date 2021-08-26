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

import net.mamoe.mirai.contact.ContactList;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.UserMessageEvent;
import studio.blacktech.furryblackplus.Driver;
import studio.blacktech.furryblackplus.core.define.Command;
import studio.blacktech.furryblackplus.core.define.annotation.Executor;
import studio.blacktech.furryblackplus.core.define.moduel.EventHandlerExecutor;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Executor(
    value = "Executor-Chou",
    outline = "随机抽人",
    description = "从当前的群随机抽一个人",
    command = "chou",
    users = false,
    usage = {
        "/chou - 抽一个人",
        "/chou XXX - 以某事抽一个人"
    },
    privacy = {
        "获取命令发送人",
        "获取群成员列表"
    }
)
public class Chou extends EventHandlerExecutor {


    private Map<Long, List<Long>> EXCLUDE;


    @Override
    public void init() {

        this.initRootFolder();
        this.initConfFolder();

        this.EXCLUDE = new HashMap<>();

        File FILE_EXCLUDE = this.initConfFile("exclude.txt");

        for (String line : this.readFile(FILE_EXCLUDE)) {

            int indexOfColon = line.indexOf(":");

            if (indexOfColon < 0) {
                this.logger.warning("配置无效 " + line);
                continue;
            }

            String group = line.substring(0, indexOfColon);
            String users = line.substring(indexOfColon + 1);

            long groupId = Long.parseLong(group);
            long usersId = Long.parseLong(users);

            List<Long> tempList = this.EXCLUDE.computeIfAbsent(groupId, k -> new ArrayList<>());
            tempList.add(usersId);
            this.logger.seek("排除成员 " + group + "-" + usersId);
        }
    }

    @Override
    public void boot() {}

    @Override
    public void shut() {}

    @Override
    public void handleUsersMessage(UserMessageEvent event, Command command) {}

    @Override
    public void handleGroupMessage(GroupMessageEvent event, Command command) {
        Group group = event.getGroup();
        Member sender = event.getSender();
        ContactList<NormalMember> members = group.getMembers();
        if (members.size() < 4) {
            Driver.sendAtMessage(event, "可用成员人数不足，无法使用此功能");
        } else {
            long botID = Driver.getBotID();
            long userID = sender.getId();
            long groupID = group.getId();
            Stream<Long> range = members.stream().map(Member::getId).filter(item -> item != botID && item != userID);
            if (this.EXCLUDE.containsKey(groupID)) {
                List<Long> list = this.EXCLUDE.get(groupID);
                if (!list.isEmpty()) range = range.filter(item -> !list.contains(item));
            }
            List<Long> list = range.collect(Collectors.toUnmodifiableList());
            int size = list.size();
            if (size < 2) {
                Driver.sendAtMessage(event, "可用成员人数不足，无法使用此功能。");
            } else {
                Long memberID = list.get(ThreadLocalRandom.current().nextInt(size));
                Member member = Driver.getMemberOrFail(groupID, memberID);
                StringBuilder builder = new StringBuilder();
                if (command.getParameterLength() > 0) {
                    builder.append("因为: ");
                    builder.append(command.getCommandBody(200));
                    builder.append("\r\n");
                }
                builder.append("抽中了: ");
                builder.append(Driver.getMemberMappedNickName(member));
                Driver.sendAtMessage(event, builder.toString());
            }
        }
    }
}
