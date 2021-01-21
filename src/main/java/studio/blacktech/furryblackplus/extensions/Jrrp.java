package studio.blacktech.furryblackplus.extensions;

import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.UserMessageEvent;
import studio.blacktech.furryblackplus.Driver;
import studio.blacktech.furryblackplus.core.annotation.Executor;
import studio.blacktech.furryblackplus.core.interfaces.EventHandlerExecutor;
import studio.blacktech.furryblackplus.core.utilties.Command;
import studio.blacktech.furryblackplus.core.utilties.DateTool;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;


@Executor(
        artificial = "Executor_Jrrp",
        name = "今日运气",
        description = "查看今天的运气值 - 大失败酱",
        privacy = {
                "获取命令发送人",
                "缓存用户与运气对应表 - 每日UTC+8 00:00 清空"
        },
        command = "jrrp",
        usage = {
                "/jrrp - 查看今日运气"
        }
)
public class Jrrp extends EventHandlerExecutor {


    public Jrrp(ExecutorInfo INFO) {
        super(INFO);
    }


    private Map<Long, Integer> JRRP;

    private Timer timer;


    @Override
    public void init() {
        JRRP = new HashMap<>();
    }

    @Override
    public void boot() {
        timer = new Timer();
        timer.scheduleAtFixedRate(
                new TimerTask() {
                    @Override
                    public void run() {
                        JRRP.clear();
                    }
                },
                DateTool.getNextDate(),
                DateTool.durationDay()
        );
    }

    @Override
    public void shut() {
        timer.cancel();
    }


    @Override
    public void handleUsersMessage(UserMessageEvent event, Command command) {
        Driver.sendMessage(event, generate(event.getSender().getId()));
    }


    @Override
    public void handleGroupMessage(GroupMessageEvent event, Command command) {
        Driver.sendMessage(event, generate(event.getSender().getId()));
    }


    private String generate(long userid) {
        int luck;
        if (JRRP.containsKey(userid)) {
            luck = JRRP.get(userid);
        } else {
            JRRP.put(userid, luck = ThreadLocalRandom.current().nextInt(100));
        }
        return "今天的运气是" + luck + "% !!!";
    }


}
