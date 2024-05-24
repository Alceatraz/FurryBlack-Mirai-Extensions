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
import java.util.*;
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


  private Set<Long> globalExclude;
  private Map<Long, Set<Long>> scopedExclude;

  @Override
  public void init() {

    ensureRootFolder();
    ensureConfFolder();

    globalExclude = new HashSet<>();
    scopedExclude = new HashMap<>();

    Path FILE_EXCLUDE = ensureConfFile("exclude.txt");

    for (String line : readLine(FILE_EXCLUDE)) {

      int indexOfColon = line.indexOf(":");

      if (indexOfColon < 0) {
        logger.warn("配置无效 {}", line);
        continue;
      }

      String group = line.substring(0, indexOfColon);
      String users = line.substring(indexOfColon + 1);

      if (group.equals("*")) {
        long usersId = Long.parseLong(users);
        globalExclude.add(usersId);
        logger.info("排除用户 {}", usersId);
      } else {
        long groupId = Long.parseLong(group);
        long usersId = Long.parseLong(users);
        Set<Long> set = scopedExclude.computeIfAbsent(groupId, it -> new HashSet<>());
        set.add(usersId);
        logger.info("排除成员 {}-{}", group, usersId);
      }
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
      FurryBlack.sendAtMessage(event, "成员过少，无法随机抽选");
      return;
    }

    long botId = FurryBlack.getBotID();
    long userId = sender.getId();
    long groupId = group.getId();

    List<NormalMember> temp = new ArrayList<>();

    for (NormalMember member : members) {
      long id = member.getId();
      if (id == botId) continue;
      if (id == userId) continue;
      if (id == groupId) continue;
      if (globalExclude.contains(id)) continue;
      Set<Long> set = scopedExclude.get(id);
      if (set != null && set.contains(userId)) continue;
      temp.add(member);
    }

    int size = temp.size();

    if (size < 2) {
      FurryBlack.sendAtMessage(event, "成员过少，无法随机抽选");
      return;
    }

    int index = ThreadLocalRandom.current().nextInt(size);

    NormalMember chosen = temp.get(index);

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
      groupId,
      userId,
      size,
      members.size(),
      chosen,
      command.getCommandBody()
    );

    FurryBlack.sendAtMessage(event, string);

  }
}
