package studio.blacktech.furryblackplus.extensions;

import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.UserMessageEvent;
import studio.blacktech.furryblackplus.Driver;
import studio.blacktech.furryblackplus.core.define.annotation.Executor;
import studio.blacktech.furryblackplus.core.define.Command;
import studio.blacktech.furryblackplus.core.define.moduel.EventHandlerExecutor;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;


@Executor(
    value = "Executor-Food",
    outline = "挑选外卖",
    description = "从预设文本随机挑选今天中午吃什么 欢迎投稿",
    command = "food",
    usage = {
        "/food - 全范围抽取",
        "/food XXX - 某类别抽取",
        "/food list - 列出所有分类",
    },
    privacy = {
        "获取命令发送人"
    }
)
public class Food extends EventHandlerExecutor {


    private FoodStorage FOOD;


    @Override
    public void init() {

        this.initRootFolder();
        this.initConfFolder();

        this.FOOD = new FoodStorage();

        File FILE_TAKEOUT = this.initConfFile("food-storage.txt");


        int i = 0;

        for (String line : this.readFile(FILE_TAKEOUT)) {

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
                String[] temp2 = temp1[1].split(",");
                for (String temp3 : temp2) {
                    String trim = temp3.trim();
                    this.FOOD.add(temp1[0], trim);
                    i++;
                }
            } else {
                this.FOOD.add(temp1[0], temp1[1]);
                i++;
            }
        }

        this.FOOD.update();

        this.logger.seek("共计添加了" + i + "种" + this.FOOD.getTypeSize() + "个类别");

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

    public String generate(Command command) {
        if (command.hasCommandBody()) {
            switch (command.getParameterSegment(0)) {

                case "dark":
                    return "请使用/dark以获取极致美食体验";

                case "list":
                    return this.FOOD.getList();

                default:
                    try {
                        int type = Integer.parseInt(command.getParameterSegment(0));
                        return this.FOOD.random(type - 1);
                    } catch (Exception exception) {
                        return "有这个类别 你在想Peach";
                    }
            }

        } else {
            return this.FOOD.random();
        }
    }

    public static class FoodStorage {

        private int typeSize;
        private String list;
        private final List<String> TYPE; // 存储所有分类
        private final Map<Integer, Integer> SIZE; // 存储分类的尺寸
        private final Map<Integer, List<String>> ITEM; // 存储实际内容

        public FoodStorage() {
            this.TYPE = new LinkedList<>();
            this.SIZE = new LinkedHashMap<>();
            this.ITEM = new LinkedHashMap<>();
        }

        public void add(String type, String name) {
            List<String> temp;
            if (this.TYPE.contains(type)) {
                int index = this.TYPE.indexOf(type);
                temp = this.ITEM.get(index);
            } else {
                int size = this.TYPE.size();
                this.TYPE.add(type);
                temp = new LinkedList<>();
                this.ITEM.put(size, temp);
            }
            temp.add(name);
        }

        public void update() {
            this.typeSize = this.TYPE.size();
            for (int i = 0; i < this.typeSize; i++) {
                List<String> temp = this.ITEM.get(i);
                this.SIZE.put(i, temp.size());
            }
            int i = 0;
            StringBuilder builder = new StringBuilder();
            builder.append("可用的类别: \r\n");
            for (String name : this.TYPE) {
                builder.append(i + 1);
                builder.append(" - ");
                builder.append(name);
                builder.append("(");
                builder.append(this.SIZE.get(i));
                builder.append(")");
                builder.append("\r\n");
                i++;
            }
            builder.setLength(builder.length() - 2);
            this.list = builder.toString();
        }

        public String random() {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            return this.random(random.nextInt(this.typeSize));
        }

        public String random(int type) {
            if (!this.SIZE.containsKey(type)) throw new IllegalArgumentException();
            ThreadLocalRandom random = ThreadLocalRandom.current();
            int length = this.SIZE.get(type);
            List<String> temp = this.ITEM.get(type);
            return temp.get(random.nextInt(length));
        }

        public String getList() {
            return this.list;
        }

        public int getTypeSize() {
            return this.typeSize;
        }

    }
}