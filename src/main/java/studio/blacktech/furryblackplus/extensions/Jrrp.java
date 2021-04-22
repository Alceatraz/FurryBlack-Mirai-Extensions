package studio.blacktech.furryblackplus.extensions;

import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.UserMessageEvent;
import studio.blacktech.furryblackplus.Driver;
import studio.blacktech.furryblackplus.core.annotation.Executor;
import studio.blacktech.furryblackplus.core.interfaces.EventHandlerExecutor;
import studio.blacktech.furryblackplus.core.utilties.Command;
import studio.blacktech.furryblackplus.core.utilties.DateTool;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;


@Executor(
    artificial = "Executor_Jrrp",
    name = "今日运气",
    description = "查看今天的运气值 - 大失败酱",
    privacy = {
        "获取命令发送人",
        "存储用户与运气对应表 - 每日UTC+8 00:00 清空"
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


    private Thread thread;

    private Map<Long, Integer> JRRP;

    private File JRRP_FILE;


    @Override
    public void load() {

        initRootFolder();
        initDataFolder();

        JRRP_FILE = initDataFile("jrrp.txt");

        Calendar lastModified = Calendar.getInstance();
        lastModified.setTimeInMillis(JRRP_FILE.lastModified());

        JRRP = new ConcurrentHashMap<>();

        if (Calendar.getInstance().get(Calendar.DATE) == lastModified.get(Calendar.DATE)) {
            for (String line : readFile(JRRP_FILE)) {
                String[] temp = line.split(":");
                Long user = Long.parseLong(temp[0].trim());
                Integer jrrp = Integer.parseInt(temp[1].trim());
                JRRP.put(user, jrrp);
            }
            logger.seek("从持久化文件中读取了 " + JRRP.size() + "条数据");
        } else {
            logger.seek("持久化文件已过期");
        }

        thread = new Thread(this::schedule);
    }


    @Override
    public void boot() {
        long initialDelay = DateTool.getNextDate().getTime() - System.currentTimeMillis();
        Driver.scheduleWithFixedDelay(this.thread, initialDelay, 1000 * 3600 * 24, TimeUnit.MILLISECONDS);
    }

    @Override
    public void shut() {
        thread.interrupt();
        try {
            thread.join();
        } catch (InterruptedException exception) {
            logger.error("等待计划任务结束失败", exception);
            if (Driver.isShutModeDrop()) Thread.currentThread().interrupt();
        }
        try (FileWriter fileWriter = new FileWriter(JRRP_FILE, false)) {
            for (Map.Entry<Long, Integer> entry : JRRP.entrySet()) {
                var k = entry.getKey();
                var v = entry.getValue();
                fileWriter.write(String.valueOf(k));
                fileWriter.write(":");
                fileWriter.write(String.valueOf(v));
                fileWriter.write("\n");
            }
            fileWriter.flush();
        } catch (IOException exception) {
            logger.warning("保存数据失败", exception);
        }
    }


    @Override
    public void handleUsersMessage(UserMessageEvent event, Command command) {
        Driver.sendMessage(event, generate(event.getSender().getId()));
    }

    @Override
    public void handleGroupMessage(GroupMessageEvent event, Command command) {
        Driver.sendAtMessage(event, generate(event.getSender().getId()));
    }

    private String generate(long userid) {
        int luck;
        if (JRRP.containsKey(userid)) {
            luck = JRRP.get(userid);
        } else {
            luck = ThreadLocalRandom.current().nextInt(101);
            JRRP.put(userid, luck);
        }
        if (luck == 0) {
            return "今天没有运气!!!";
        } else if (luck == 100) {
            return "今天运气爆表!!!";
        } else {
            return "今天的运气是" + luck + "% !!!";
        }
    }

    private void schedule() {
        JRRP.clear();
        try (FileWriter fileWriter = new FileWriter(JRRP_FILE, false)) {
            fileWriter.write("");
            fileWriter.flush();
        } catch (IOException exception) {
            logger.warning("清空数据失败", exception);
        }
    }

}
