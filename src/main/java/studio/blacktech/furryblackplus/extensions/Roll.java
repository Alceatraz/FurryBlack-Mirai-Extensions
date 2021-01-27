package studio.blacktech.furryblackplus.extensions;

import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.UserMessageEvent;
import studio.blacktech.furryblackplus.Driver;
import studio.blacktech.furryblackplus.core.annotation.Executor;
import studio.blacktech.furryblackplus.core.interfaces.EventHandlerExecutor;
import studio.blacktech.furryblackplus.core.utilties.Command;

import java.security.SecureRandom;


@Executor(
    artificial = "Executor_Roll",
    name = "随机数",
    description = "生成随机数并窥探本质",
    privacy = {
        "获取命令发送人"
    },
    command = "roll",
    usage = {
        "/roll - 抽取真假",
        "/roll 数字 - 从零到给定数字任选一个数字[0,x)",
        "/roll 数字 数字 - 从给定两个数字中间抽取一个[x,y)"
    }
)
public class Roll extends EventHandlerExecutor {


    public Roll(ExecutorInfo INFO) {
        super(INFO);
    }


    @Override
    public void init() { }

    @Override
    public void boot() { }

    @Override
    public void shut() { }

    @Override
    public void handleUsersMessage(UserMessageEvent event, Command command) {
        Driver.sendMessage(event, roll(command));
    }

    @Override
    public void handleGroupMessage(GroupMessageEvent event, Command command) {
        Driver.sendMessage(event, roll(command));
    }

    private String roll(Command command) {

        String res;
        SecureRandom random = new SecureRandom();

        switch (command.getParameterLength()) {

            // ============================================================

            case 0:
                res = random.nextBoolean() ? " 1️⃣" : " 0️⃣";
                break;

            // ============================================================

            case 1:
                int range;
                try {
                    range = Integer.parseInt(command.getParameterSegment(0));
                    res = Integer.toString(random.nextInt(range));
                } catch (Exception ignored) {
                    res = command.getCommandBody(200) + " 是 " + (random.nextBoolean() ? " 1️⃣" : " 0️⃣");
                }
                break;

            // ============================================================

            case 2:
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
                break;

            default:
                res = command.getCommandBody(200) + " 是 " + (random.nextBoolean() ? " 1️⃣" : " 0️⃣");
        }

        return res;

    }

}
