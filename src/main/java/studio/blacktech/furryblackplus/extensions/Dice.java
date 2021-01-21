package studio.blacktech.furryblackplus.extensions;

import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.UserMessageEvent;
import studio.blacktech.furryblackplus.Driver;
import studio.blacktech.furryblackplus.core.annotation.Executor;
import studio.blacktech.furryblackplus.core.interfaces.EventHandlerExecutor;
import studio.blacktech.furryblackplus.core.utilties.Command;

import java.util.concurrent.ThreadLocalRandom;


@Executor(
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


    public Dice(ExecutorInfo INFO) {
        super(INFO);
    }


    @Override
    public void init() {
    }

    @Override
    public void boot() {
    }

    @Override
    public void shut() {
    }


    @Override
    public void handleUsersMessage(UserMessageEvent event, Command command) {
        Driver.sendMessage(event, dice());
    }


    @Override
    public void handleGroupMessage(GroupMessageEvent event, Command command) {
        Driver.sendMessage(event, dice());
    }


    private final static String[] DICE = {
            "0️⃣", "1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣"
    };


    private String dice() {
        int i = ThreadLocalRandom.current().nextInt(61);
        if (i == 0) {
            return DICE[0];
        } else {
            return DICE[i / 10 + 1];
        }
    }


    private String diceNormal() {
        return DICE[ThreadLocalRandom.current().nextInt(5) + 1];
    }


}
