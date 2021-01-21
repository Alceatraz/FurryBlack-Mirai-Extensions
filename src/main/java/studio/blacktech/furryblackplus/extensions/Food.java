package studio.blacktech.furryblackplus.extensions;

import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.UserMessageEvent;
import studio.blacktech.furryblackplus.Driver;
import studio.blacktech.furryblackplus.core.annotation.Executor;
import studio.blacktech.furryblackplus.core.exception.initlization.InitException;
import studio.blacktech.furryblackplus.core.interfaces.EventHandlerExecutor;
import studio.blacktech.furryblackplus.core.utilties.Command;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 这功能是三木提的
 */
@Executor(
        artificial = "Executor_Food",
        name = "外卖吃什么",
        description = "随机选择外卖吃什么 解决选择困难",
        privacy = {
                "获取命令发送人"
        },
        command = "food",
        usage = {
                "/food - 全范围抽取",
                "/food XXX - 某类别抽取",
                "/food list - 列出所有分类",
        }
)
public class Food extends EventHandlerExecutor {


    public Food(ExecutorInfo INFO) {
        super(INFO);
    }


    private FoodStorage FOOD;


    @Override
    public void init() throws InitException {

        initRootFolder();
        initConfFolder();

        FOOD = new FoodStorage();


        File FILE_TAKEOUT = initConfFile("takeout.txt");


        for (String line : readFile(FILE_TAKEOUT)) {

            if (!line.contains(":")) {
                logger.warning("配置无效 " + line);
                continue;
            }

            String[] temp1 = line.split(":");

            if (temp1.length != 2) {
                logger.warning("配置无效 " + line);
                continue;
            }

            if (temp1[1].contains(",")) {
                String[] temp2 = temp1[1].split(",");
                for (String temp3 : temp2) {
                    String trim = temp3.trim();
                    FOOD.add(temp1[0], trim);
                    logger.seek("添加选项 " + temp1[0] + "-> " + trim);
                }
            } else {
                FOOD.add(temp1[0], temp1[1]);
                logger.seek("添加选项 " + temp1[0] + "-> " + temp1[1]);
            }
        }

        FOOD.update();

    }


    @Override
    public void boot() {

    }

    @Override
    public void shut() {

    }


    @Override
    public void handleUsersMessage(UserMessageEvent event, Command command) {
        Driver.sendMessage(event, generate(command));
    }


    @Override
    public void handleGroupMessage(GroupMessageEvent event, Command command) {
        Driver.sendMessage(event, generate(command));
    }


    public String generate(Command command) {
        if (command.hasCommandBody()) {
            switch (command.getParameterSegment(0)) {

                case "dark":
                    return "请使用/dark以获取极致美食体验";

                case "list":
                    return FOOD.getList();

                default:
                    try {
                        int type = Integer.parseInt(command.getParameterSegment(0));
                        return FOOD.random(type - 1);
                    } catch (Exception exception) {
                        return "有这个类别 你在想Peach";
                    }
            }

        } else {
            return FOOD.random();
        }
    }


    public static class FoodStorage {

        private int size;
        private String list;
        private final List<String> TYPE;
        private final Map<Integer, Integer> SIZE;
        private final Map<Integer, List<String>> NAME;

        public FoodStorage() {
            TYPE = new LinkedList<>();
            SIZE = new LinkedHashMap<>();
            NAME = new LinkedHashMap<>();
        }

        public void add(String type, String name) {
            List<String> temp;
            if (TYPE.contains(type)) {
                int index = TYPE.indexOf(type);
                temp = NAME.get(index);
            } else {
                int size = TYPE.size();
                TYPE.add(type);
                NAME.put(size, temp = new LinkedList<>());
            }
            temp.add(name);
        }

        public void update() {
            size = TYPE.size();
            for (int i = 0; i < size; i++) {
                List<String> list = NAME.get(i);
                SIZE.put(i, list.size());
            }
            int i = 0;
            StringBuilder builder = new StringBuilder();
            builder.append("可用的类别: \r\n");
            for (String name : TYPE) {
                builder.append(i + 1);
                builder.append(" - ");
                builder.append(name);
                builder.append("(");
                builder.append(SIZE.get(i));
                builder.append(")");
                builder.append("\r\n");
                i++;
            }
            builder.setLength(builder.length() - 2);
            list = builder.toString();
        }

        public String random() {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            return random(random.nextInt(size));
        }

        public String random(int type) {
            if (!SIZE.containsKey(type)) throw new IllegalArgumentException();
            ThreadLocalRandom random = ThreadLocalRandom.current();
            int length = SIZE.get(type);
            List<String> list = NAME.get(type);
            return list.get(random.nextInt(length));
        }

        public String getList() {
            return list;
        }
    }
}