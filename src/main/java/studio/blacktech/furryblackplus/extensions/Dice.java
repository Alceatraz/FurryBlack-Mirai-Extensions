package studio.blacktech.furryblackplus.extensions;

import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.UserMessageEvent;
import studio.blacktech.furryblackplus.Driver;
import studio.blacktech.furryblackplus.core.annotation.Executor;
import studio.blacktech.furryblackplus.core.define.Command;
import studio.blacktech.furryblackplus.core.define.moduel.EventHandlerExecutor;

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
    public void init() { }

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
