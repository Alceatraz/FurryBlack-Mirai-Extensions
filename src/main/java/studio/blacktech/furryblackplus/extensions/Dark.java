package studio.blacktech.furryblackplus.extensions;

import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.UserMessageEvent;
import studio.blacktech.furryblackplus.Driver;
import studio.blacktech.furryblackplus.core.annotation.Component;
import studio.blacktech.furryblackplus.core.interfaces.EventHandlerExecutor;
import studio.blacktech.furryblackplus.core.utilties.Command;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


@Component(
    artificial = "Executor_Dark",
    name = "挑hei选an美liao食li",
    description = "白熊精选美食 1.0版",
    privacy = {
        "获取命令发送人"
    },
    command = "dark",
    usage = {
        "/dark - 使用随机种食材随机生成一些吃完了会死的东西",
        "/dark 数字 - 使用指定种食材随机生成一些没准吃完了不会死的东西",
    }
)
public class Dark extends EventHandlerExecutor {


    private int sizeCookMethod;
    private int sizeIngredient;

    private List<String> COOK_METHOD;
    private List<String> INGREDIENTS;


    @Override
    public void init() {

        this.initRootFolder();
        this.initConfFolder();

        this.COOK_METHOD = new ArrayList<>();
        this.INGREDIENTS = new ArrayList<>();

        File FILE_COOK_METHOD = this.initConfFile("dark-verb.txt");
        File FILE_INGREDIENTS = this.initConfFile("dark-item.txt");

        int i = 0;

        for (String line : this.readFile(FILE_COOK_METHOD)) {
            i++;
            this.COOK_METHOD.add(line);
        }

        int j = 0;

        for (String line : this.readFile(FILE_INGREDIENTS)) {

            if (!line.contains(":")) {
                this.logger.warning("配置无效 " + line);
                continue;
            }

            String[] temp1 = line.split(":");

            if (temp1.length != 2) {
                this.logger.warning("配置无效 " + line);
                continue;
            }

            if (temp1[1].contains(",")) {
                for (String temp : temp1[1].split(",")) {
                    String trim = temp.trim();
                    this.INGREDIENTS.add(trim);
                    j++;
                }
            } else {
                this.INGREDIENTS.add(temp1[1]);
                j++;
            }
        }

        this.sizeCookMethod = this.COOK_METHOD.size();
        this.sizeIngredient = this.INGREDIENTS.size();

        this.logger.seek("共添加了" + i + "种方式" + j + "种材料");

    }

    @Override
    public void boot() { }

    @Override
    public void shut() { }

    @Override
    public void handleUsersMessage(UserMessageEvent event, Command command) {
        Driver.sendMessage(event, this.generate(command));
    }

    @Override
    public void handleGroupMessage(GroupMessageEvent event, Command command) {
        Driver.sendAtMessage(event, this.generate(command));
    }

    private String generate(Command command) {
        StringBuilder builder = new StringBuilder();
        int size;
        if (command.hasCommandBody()) {
            try {
                size = Integer.parseInt(command.getParameterSegment(0));
            } catch (Exception exception) {
                builder.append("无效 我觉得你在想peach 成全你\r\n");
                size = this.sizeCookMethod;
            }
            if (size == 0) size = this.sizeCookMethod;
            if (size > this.sizeCookMethod) size = this.sizeCookMethod;
        } else {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            size = random.nextInt(4) + 2;
        }
        builder.append(this.generate(size));
        return builder.toString();
    }

    private String generate(int size) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        List<String> USED_COOK_METHOD = new ArrayList<>(size);
        List<String> USED_INGREDIENTS = new ArrayList<>(size);
        for (int i = 1; i < size; i++) {
            String temp;
            do {
                temp = this.COOK_METHOD.get(random.nextInt(this.sizeCookMethod));
            } while (USED_COOK_METHOD.contains(temp));
            USED_COOK_METHOD.add(temp);
        }
        for (int i = 0; i < size; i++) {
            String temp;
            do {
                temp = this.INGREDIENTS.get(random.nextInt(this.sizeIngredient));
            } while (USED_INGREDIENTS.contains(temp));
            USED_INGREDIENTS.add(temp);
        }
        size = size - 1;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < size; i++) {
            builder.append(USED_INGREDIENTS.remove(0));
            builder.append(USED_COOK_METHOD.remove(0));
        }
        builder.append(USED_INGREDIENTS.remove(0));
        return builder.toString();
    }
}