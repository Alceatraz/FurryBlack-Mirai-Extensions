package top.btswork.furryblack.extensions;

import net.mamoe.mirai.contact.ContactList;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.UserMessageEvent;
import top.btswork.furryblack.FurryBlack;
import top.btswork.furryblack.core.handler.EventHandlerExecutor;
import top.btswork.furryblack.core.handler.annotation.Executor;
import top.btswork.furryblack.core.handler.common.Command;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
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

    ensureRootFolder();
    ensureConfFolder();

    EXCLUDE = new HashMap<>();

    Path FILE_EXCLUDE = ensureConfFile("exclude.txt");

    for (String line : readLine(FILE_EXCLUDE)) {

      int indexOfColon = line.indexOf(":");

      if (indexOfColon < 0) {
        logger.warn("配置无效 {}", line);
        continue;
      }

      String group = line.substring(0, indexOfColon);
      String users = line.substring(indexOfColon + 1);

      long groupId = Long.parseLong(group);
      long usersId = Long.parseLong(users);

      List<Long> tempList = EXCLUDE.computeIfAbsent(groupId, k -> new ArrayList<>());
      tempList.add(usersId);
      logger.info("排除成员 {}-{}", group, usersId);
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
      return;
    }

    long botID = FurryBlack.getBotID();
    long userID = sender.getId();
    long groupID = group.getId();

    Stream<NormalMember> stream = members.stream()
      .filter(it -> it.getId() != botID && it.getId() != userID);

    List<NormalMember> memberList;

    List<Long> excludeList = EXCLUDE.get(groupID);

    if (excludeList == null) {
      memberList = stream.toList();
    } else {
      memberList = stream.filter(item -> !excludeList.contains(item.getId())).toList();
    }

    int size = memberList.size();

    if (size < 2) {
      return;
    }

    int index = ThreadLocalRandom.current().nextInt(size);

    NormalMember chosen = memberList.get(index);

    StringBuilder builder = new StringBuilder();
    if (command.getParameterLength() > 0) {
      builder.append("因为: ");
      builder.append(command.getCommandBody());
      builder.append("\r\n");
    }
    builder.append("抽中了: ");
    builder.append(FurryBlack.getMemberMappedNickName(chosen));

    String string = builder.toString();

    logger.info(
      "{}:{} -> {}/{} 抽中 {} {}",
      groupID,
      userID,
      memberList.size(),
      members.size(),
      chosen,
      command.getCommandBody()
    );

    FurryBlack.sendAtMessage(event, string);

  }
}
