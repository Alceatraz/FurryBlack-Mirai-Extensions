package studio.blacktech.furryblackplus.extensions;

import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.UserMessageEvent;
import okhttp3.OkHttpClient;
import okhttp3.Request;
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
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


@Executor(
    artificial = "Executor_Jrjt",
    name = "沙雕软件-今日鸡汤",
    description = "https://shadiao.app的快捷方式",
    command = "jrjt",
    usage = {
        "/jrjt - 每天送你一碗热气腾腾的翔"
    }
)
public class Jrjt extends EventHandlerExecutor {


    public Jrjt(ExecutorInfo INFO) {
        super(INFO);
    }


    private Thread thread;

    private Map<Long, String> JRJT;

    private Request request;
    private OkHttpClient httpClient;

    private File JRJT_FILE;


    @Override
    public void init() {

        initRootFolder();
        initDataFolder();

        JRJT_FILE = initDataFile("jrjt.txt");

        Calendar lastModified = Calendar.getInstance();
        lastModified.setTimeInMillis(JRJT_FILE.lastModified());

        JRJT = new ConcurrentHashMap<>();

        httpClient = new OkHttpClient.Builder()
                         .callTimeout(2, TimeUnit.SECONDS)
                         .readTimeout(2, TimeUnit.SECONDS)
                         .writeTimeout(2, TimeUnit.SECONDS)
                         .connectTimeout(2, TimeUnit.SECONDS)
                         .build();

        request = new Request.Builder().url("https://du.shadiao.app/api.php").get().build();

        if (Calendar.getInstance().get(Calendar.DATE) == lastModified.get(Calendar.DATE)) {
            for (String line : readFile(JRJT_FILE)) {
                String[] temp = line.split(":");
                Long user = Long.parseLong(temp[0].trim());
                JRJT.put(user, temp[1].trim());
            }
            logger.seek("从持久化文件中读取了 " + JRJT.size() + "条数据");
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
        }
        try (FileWriter fileWriter = new FileWriter(JRJT_FILE, false)) {
            for (Map.Entry<Long, String> entry : JRJT.entrySet()) {
                var k = entry.getKey();
                var v = entry.getValue();
                fileWriter.write(String.valueOf(k));
                fileWriter.write(":");
                fileWriter.write(v);
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

    private String generate(long user) {
        String message;
        if (JRJT.containsKey(user)) {
            message = JRJT.get(user);
        } else {
            try {
                JRJT.put(user, message = "今日鸡汤 " + Objects.requireNonNull(httpClient.newCall(request).execute().body()).string());
            } catch (IOException exception) {
                logger.error("沙雕服务器连接失败", exception);
                message = "沙雕App的服务器炸了";
            }
        }
        return message;
    }

    private void schedule() {
        JRJT.clear();
        try (FileWriter fileWriter = new FileWriter(JRJT_FILE, false)) {
            fileWriter.write("");
            fileWriter.flush();
        } catch (IOException exception) {
            logger.warning("清空数据失败", exception);
        }
    }

}
