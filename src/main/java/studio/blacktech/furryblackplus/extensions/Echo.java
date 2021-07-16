package studio.blacktech.furryblackplus.extensions;

import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.UserMessageEvent;
import studio.blacktech.furryblackplus.Driver;
import studio.blacktech.furryblackplus.core.annotation.Component;
import studio.blacktech.furryblackplus.core.interfaces.EventHandlerExecutor;
import studio.blacktech.furryblackplus.core.utilties.Command;


@Component(
    artificial = "Executor_Echo",
    name = "回显",
    description = "查看机器人是否在线",
    privacy = {
        "获取命令发送人"
    },
    command = "echo",
    usage = {
        "/echo - Ping!Pong!",
        "/echo XXX - 原样返回"
    }
)
public class Echo extends EventHandlerExecutor {


    @Override
    public void load() { }

    @Override
    public void boot() { }

    @Override
    public void shut() { }

    @Override
    public void handleUsersMessage(UserMessageEvent event, Command command) {
        Driver.sendMessage(event, command.getParameterLength() == 0 ? "Pang!" : command.getCommandBody());
    }

    @Override
    public void handleGroupMessage(GroupMessageEvent event, Command command) {
        Driver.sendAtMessage(event, command.getParameterLength() == 0 ? "Pang!" : command.getCommandBody());
    }


}
