package studio.blacktech.furryblackplus.extensions;

import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.UserMessageEvent;
import studio.blacktech.furryblackplus.Driver;
import studio.blacktech.furryblackplus.core.annotation.Component;
import studio.blacktech.furryblackplus.core.interfaces.EventHandlerExecutor;
import studio.blacktech.furryblackplus.core.utilties.Command;

import java.util.concurrent.ThreadLocalRandom;


@Component(
    artificial = "Executor_Dice",
    name = "骰子",
    description = "七面骰子",
    privacy = {
        "获取命令发送人"
    },
    command = "dice",
    usage = {
        "/dice - 投掷一枚骰子"
    }
)
public class Dice extends EventHandlerExecutor {


    private static final String[] DICES = {"0️⃣", "1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣"};


    @Override
    public void load() { }

    @Override
    public void boot() { }

    @Override
    public void shut() { }

    @Override
    public void handleUsersMessage(UserMessageEvent event, Command command) {
        Driver.sendMessage(event, this.dice());
    }

    @Override
    public void handleGroupMessage(GroupMessageEvent event, Command command) {
        Driver.sendAtMessage(event, this.dice());
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
