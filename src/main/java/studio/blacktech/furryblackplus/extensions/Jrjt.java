package studio.blacktech.furryblackplus.extensions;

import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.UserMessageEvent;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import studio.blacktech.furryblackplus.Driver;
import studio.blacktech.furryblackplus.core.annotation.Component;
import studio.blacktech.furryblackplus.core.interfaces.EventHandlerExecutor;
import studio.blacktech.furryblackplus.core.utilties.Command;
import studio.blacktech.furryblackplus.core.utilties.TimeTool;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


@Component(
    artificial = "Executor_Jrjt",
    name = "沙雕软件-今日鸡汤",
    description = "https://shadiao.app的快捷方式",
    command = "jrjt",
    usage = {
        "/jrjt - 每天送你一碗热气腾腾的翔"
    }
)
public class Jrjt extends EventHandlerExecutor {


    private Thread thread;

    private Map<Long, String> JRJT;

    private Request request;
    private OkHttpClient httpClient;

    private File JRJT_FILE;


    @Override
    public void init() {

        this.initRootFolder();
        this.initDataFolder();

        this.JRJT_FILE = this.initDataFile("jrjt.txt");

        this.JRJT = new ConcurrentHashMap<>();

        this.httpClient = new OkHttpClient.Builder()
                              .callTimeout(2, TimeUnit.SECONDS)
                              .readTimeout(2, TimeUnit.SECONDS)
                              .writeTimeout(2, TimeUnit.SECONDS)
                              .connectTimeout(2, TimeUnit.SECONDS)
                              .build();

        this.request = new Request.Builder().url("https://du.shadiao.app/api.php").get().build();

        if (TimeTool.isToday(this.JRJT_FILE.lastModified())) {
            Base64.Decoder decoder = Base64.getDecoder();
            for (String line : this.readFile(this.JRJT_FILE)) {
                String[] temp = line.split(":");
                Long user = Long.parseLong(temp[0].trim());
                byte[] decode = decoder.decode(temp[1]);
                String string = new String(decode, StandardCharsets.UTF_8);
                this.JRJT.put(user, string);
            }
            this.logger.seek("从持久化文件中读取了 " + this.JRJT.size() + "条数据");
        } else {
            this.logger.seek("持久化文件已过期");
        }

        this.thread = new Thread(this::schedule);
    }


    @Override
    public void boot() {
        Driver.scheduleAtNextDayFixedRate(this.thread, 1000 * 3600 * 24, TimeUnit.MILLISECONDS);
    }

    @Override
    public void shut() {
        this.thread.interrupt();
        try {
            this.thread.join();
        } catch (InterruptedException exception) {
            this.logger.error("等待计划任务结束失败", exception);
            if (Driver.isShutModeDrop()) Thread.currentThread().interrupt();
        }
        try (FileWriter fileWriter = new FileWriter(this.JRJT_FILE, false)) {
            for (Map.Entry<Long, String> entry : this.JRJT.entrySet()) {
                var k = entry.getKey();
                var v = entry.getValue();
                fileWriter.write(String.valueOf(k));
                fileWriter.write(":");
                fileWriter.write(Base64.getEncoder().encodeToString(v.getBytes(StandardCharsets.UTF_8)));
                fileWriter.write("\n");
            }
            fileWriter.flush();
        } catch (IOException exception) {
            this.logger.warning("保存数据失败", exception);
        }
    }


    @Override
    public void handleUsersMessage(UserMessageEvent event, Command command) {
        Driver.sendMessage(event, this.generate(event.getSender().getId()));
    }

    @Override
    public void handleGroupMessage(GroupMessageEvent event, Command command) {
        Driver.sendAtMessage(event, this.generate(event.getSender().getId()));
    }

    private String generate(long user) {
        String message;
        if (this.JRJT.containsKey(user)) {
            message = this.JRJT.get(user);
        } else {
            try {
                message = Objects.requireNonNull(this.httpClient.newCall(this.request).execute().body()).string();
                this.JRJT.put(user, message);
            } catch (IOException exception) {
                this.logger.error("沙雕服务器连接失败", exception);
                message = "沙雕App的服务器炸了";
            }
        }
        return message;
    }

    private void schedule() {
        this.JRJT.clear();
        try (FileWriter fileWriter = new FileWriter(this.JRJT_FILE, false)) {
            fileWriter.write("");
            fileWriter.flush();
        } catch (IOException exception) {
            this.logger.warning("清空数据失败", exception);
        }
    }

}
