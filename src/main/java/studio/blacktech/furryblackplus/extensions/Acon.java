package studio.blacktech.furryblackplus.extensions;

import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.UserMessageEvent;
import studio.blacktech.furryblackplus.Driver;
import studio.blacktech.furryblackplus.core.annotation.Executor;
import studio.blacktech.furryblackplus.core.interfaces.EventHandlerExecutor;
import studio.blacktech.furryblackplus.core.utilties.Command;

import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Executor(
    artificial = "Executor_ACON",
    name = "空调",
    description = "本群空调",
    privacy = {
        "按群存储耗电量",
        "按群存储耗工作模式",
        "按群存储上次更改模式的时间戳"
    },
    users = false,
    command = "acon",
    usage = {
        "/acon cost - 耗电量",
        "/acon off - 关机",
        "/acon wet - 加湿",
        "/acon dry - 除湿",
        "/acon cold - 制冰模式",
        "/acon cool - 制冷模式",
        "/acon warm - 制热模式",
        "/acon bake - 烘烤模式",
        "/acon burn - 烧烤模式",
        "/acon fire - 焚化模式",
        "/acon c2h2 - 乙炔炬模式",
        "/acon argon - 氩气引弧模式",
        "/acon plasma - 等离子模式",
        "/acon nova - 点亮一颗新星",
        "/acon cfnuke - 点燃一颗冷核武器",
        "/acon trnuke - 点燃一颗热核武器",
        "/acon tpnuke - 点燃一颗三相热核弹",
        "/acon ianova - Ia级超新星吸积引燃",
        "/acon ibnova - Ib级超新星吸积引燃",
        "/acon icnova - Ic级超新星吸积引燃",
        "/acon iinova - II级超新星吸积引燃",
        "/acon ~!C??? - Fy:????",
        "/acon ~!R[?? - FT//s??"
    }
)
public class Acon extends EventHandlerExecutor {


    public Acon(ExecutorInfo INFO) {
        super(INFO);
    }


    private final static BigInteger $1000 = BigInteger.valueOf(1000);
    private final static BigInteger $HOUR = BigInteger.valueOf(3600000);
    private final static BigInteger $FACT = BigInteger.valueOf(1980000);


    private Map<Long, AirCondition> AIR_CONDITIONS;


    @Override
    public void load() {
        AIR_CONDITIONS = new ConcurrentHashMap<>();
    }

    @Override
    public void boot() { }

    @Override
    public void shut() { }

    @Override
    public void handleUsersMessage(UserMessageEvent event, Command command) { }

    @Override
    public void handleGroupMessage(GroupMessageEvent event, Command command) {
        long group = event.getGroup().getId();
        AirCondition airCondition;
        if (AIR_CONDITIONS.containsKey(group)) {
            airCondition = AIR_CONDITIONS.get(group);
        } else {
            airCondition = new AirCondition();
            AIR_CONDITIONS.put(group, airCondition);
        }
        if (command.hasCommandBody()) {
            switch (command.getParameterSegment(0)) {

                case "off":
                    Driver.sendAtMessage(event, "空调已关闭");
                    airCondition.changeMode(1L);
                    break;

                case "dry":
                    Driver.sendAtMessage(event, "切换至除湿模式");
                    airCondition.changeMode(5880L);
                    break;

                case "wet":
                    Driver.sendAtMessage(event, "切换至加湿模式");
                    airCondition.changeMode(5880L);
                    break;

                case "cold":
                    Driver.sendAtMessage(event, "切换至制冰模式 -20°");
                    airCondition.changeMode(14700L);
                    break;

                case "cool":
                    Driver.sendAtMessage(event, "切换至制冷模式 26.5°");
                    airCondition.changeMode(7350L);
                    break;

                case "warm":
                    Driver.sendAtMessage(event, "切换至制热模式 25.5°");
                    airCondition.changeMode(7350L);
                    break;

                case "bake":
                    Driver.sendAtMessage(event, "切换至烘烤模式 285°");
                    airCondition.changeMode(14700L);
                    break;

                case "burn":
                    Driver.sendAtMessage(event, "切换至烧烤模式 960°");
                    airCondition.changeMode(22050L);
                    break;

                case "fire":
                    Driver.sendAtMessage(event, "切换至焚化模式 1,200°");
                    airCondition.changeMode(29400L);
                    break;

                case "c2h2":
                    Driver.sendAtMessage(event, "切换至乙炔炬模式 3,300°");
                    airCondition.changeMode(33075L);
                    break;

                case "argon":
                    Driver.sendAtMessage(event, "切换至氩气弧模式 7,550°");
                    airCondition.changeMode(36750L);
                    break;

                case "plasma":
                    Driver.sendAtMessage(event, "切换至等离子模式 23,500°");
                    airCondition.changeMode(44100L);
                    break;

                case "nova":
                    Driver.sendAtMessage(event, "切换至新星模式 1,000,000°");
                    airCondition.changeMode(7350000L);
                    break;

                case "cfnuke":
                    Driver.sendAtMessage(event, "切换至冷核模式 100,000,000°");
                    airCondition.changeMode(29400000L);
                    break;

                case "trnuke":
                    Driver.sendAtMessage(event, "切换至热核模式 120,000,000°");
                    airCondition.changeMode(33075000L);
                    break;

                case "tfnuke":
                    Driver.sendAtMessage(event, "切换至三相热核模式 150,000,000°");
                    airCondition.changeMode(44100000L);
                    break;

                case "ianova":
                    Driver.sendAtMessage(event, "切换至Ia星爆发模式 800,000,000°");
                    airCondition.changeMode(294000000L);
                    break;

                case "ibnova":
                    Driver.sendAtMessage(event, "切换至Ib新星爆发模式 2,600,000,000°");
                    airCondition.changeMode(330750000L);
                    break;

                case "icnova":
                    Driver.sendAtMessage(event, "切换至Ic新星爆发模式 2,800,000,000°");
                    airCondition.changeMode(441000000L);
                    break;

                case "iinova":
                    Driver.sendAtMessage(event, "切换至II新星爆发模式 3,000,000,000°");
                    airCondition.changeMode(514500000L);
                    break;

                case "samrage":
                    Driver.sendAtMessage(event, "父王之怒 10,000,000,000,000,000,000,000,000,000°");
                    airCondition.changeMode(73500000000L);
                    break;

                case "samrape":
                    Driver.sendAtMessage(event, "父王之怒 -273.16°");
                    airCondition.changeMode(73500000000L);
                    break;


                default:
                    Driver.sendMessage(event, airCondition.cost());

            }
            return;
        }
        Driver.sendAtMessage(event, airCondition.cost());
    }


    public static class AirCondition {

        private long mode = 0;
        private long time = 0;
        private BigInteger cost = BigInteger.ZERO;

        /**
         * 将之前的运行模式计算为价格
         */
        private void updateCost() {
            long current = System.currentTimeMillis();
            long duration = current - this.time;
            duration = duration / 1000;
            BigInteger a = BigInteger.valueOf(duration);
            BigInteger b = BigInteger.valueOf(mode);
            BigInteger c = a.multiply(b);
            cost = cost.add(c);
            this.time = current;
        }

        /**
         * 更改模式
         *
         * @param mode 新的模式
         */
        public void changeMode(long mode) {
            this.updateCost();
            this.mode = mode;
        }

        /**
         * 查询价格 即使没有更新模式也要将之前的价格累加
         *
         * @return 消息
         */
        public String cost() {
            this.updateCost();
            return "累计共耗电：" + cost.divide($1000) + "kW(" + cost.divide($HOUR) + ")度\r\n群主须支付：" + cost.divide($FACT) + "元";
        }

    }

}