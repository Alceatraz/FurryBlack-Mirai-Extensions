package studio.blacktech.furryblackplus.extensions;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.UserMessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Face;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;
import studio.blacktech.furryblackplus.Driver;
import studio.blacktech.furryblackplus.core.annotation.Executor;
import studio.blacktech.furryblackplus.core.interfaces.EventHandlerExecutor;
import studio.blacktech.furryblackplus.core.utilties.Command;
import studio.blacktech.furryblackplus.core.utilties.DateTool;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;


@Executor(
    artificial = "Executor_Roulette",
    name = "俄罗斯轮盘赌",
    description = "你看这子弹又尖又长，这名单又大又宽",
    privacy = {
        "获取命令发送人",
        "缓存群-成员-回合 结束后丢弃"
    },
    command = "roulette",
    usage = {
        "/roulette 筹码 - 加入或者发起一局俄罗斯轮盘赌 重复下注可增加被枪毙的几率"
    }
)
public class Roulette extends EventHandlerExecutor {


    public Roulette(ExecutorInfo INFO) {
        super(INFO);
    }


    private final static String[] ICON = {
        "1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣"
    };


    private HashMap<Long, RouletteRound> rounds;


    @Override
    public void init() {
        rounds = new HashMap<>();
    }

    @Override
    public void boot() { }

    @Override
    public void shut() { }

    @Override
    public void handleUsersMessage(UserMessageEvent event, Command command) {
        Driver.sendMessage(event, "好的，没有问题，成全你");
        Driver.sendMessage(event, new Face(Face.手枪).plus("\uD83D\uDCA5"));
        Driver.sendMessage(event, new Face(Face.手枪).plus("\uD83D\uDCA5"));
        Driver.sendMessage(event, new Face(Face.手枪).plus("\uD83D\uDCA5"));
        Driver.sendMessage(event, new Face(Face.手枪).plus("\uD83D\uDCA5"));
        Driver.sendMessage(event, new Face(Face.手枪).plus("\uD83D\uDCA5"));
        Driver.sendMessage(event, new Face(Face.手枪).plus("\uD83D\uDCA5"));
    }


    @Override
    public synchronized void handleGroupMessage(GroupMessageEvent event, Command command) {

        Group group = event.getGroup();

        if (!command.hasCommandBody()) {
            Driver.sendMessage(event, "你必须下注");
            return;
        }

        //

        RouletteRound round;

        //

        long current = System.currentTimeMillis();

        if (rounds.containsKey(group.getId())) {
            round = rounds.get(group.getId());
            if (round.getExpireTime().getTime() - current < 0) {
                rounds.remove(group.getId());
                rounds.put(group.getId(), round = new RouletteRound());
            }
        } else {
            rounds.put(group.getId(), round = new RouletteRound());
        }

        //

        if (round.join(event, command)) {

            if (round.isSinglePlayer()) {
                RouletteRound.PlayerJetton loser = round.gamblers.get(0);
                long loserID = loser.member.getId();
                Driver.sendAtMessage(event,
                    new PlainText("好的，没有问题，成全你\r\n")
                        .plus(new At(loserID))
                        .plus(new Face(Face.手枪)).plus("\uD83D\uDCA5\r\n")
                        .plus(new Face(Face.手枪)).plus("\uD83D\uDCA5\r\n")
                        .plus(new Face(Face.手枪)).plus("\uD83D\uDCA5\r\n")
                        .plus(new Face(Face.手枪)).plus("\uD83D\uDCA5\r\n")
                        .plus(new Face(Face.手枪)).plus("\uD83D\uDCA5\r\n")
                        .plus(new Face(Face.手枪)).plus("\uD83D\uDCA5\r\n目标已被击毙: " + Driver.getFormattedNickName(loserID) + "\r\n掉落了以下物品:" + round.getAllJetton(loserID))
                );

            } else {

                int bullet = round.roll();
                RouletteRound.PlayerJetton loser = round.gamblers.get(bullet);
                long loserID = loser.member.getId();

                Message message = new PlainText("名单已凑齐 装填子弹中\r\n");

                for (int i = 0; i < 6; i++) {
                    RouletteRound.PlayerJetton temp = round.gamblers.get(i);
                    message = message.plus(ICON[i] + " " + Driver.getFormattedNickName(temp.member.getId()) + " ");
                    message = message.plus(new Face(Face.手枪));
                    if (i == round.getLoser()) {
                        message = message.plus("\uD83D\uDCA5\r\n"); // 💥
                    } else {
                        message = message.plus("\r\n");
                    }
                }
                message = message.plus("\r\n目标已被击毙: ");
                message = message.plus(new At(loserID));
                message = message.plus("\r\n掉落了以下物品: " + round.getAllJetton(loserID));
                Driver.sendMessage(event, message);
            }

            rounds.remove(group.getId());

        } else {

            StringBuilder builder = new StringBuilder();

            builder.append(" 俄罗斯轮盘 - 当前人数 (");
            builder.append(round.getGamblers().size());
            builder.append("/6)\r\n");

            int i = 0;

            int size = round.getGamblers().size();

            for (; i < size; i++) {
                RouletteRound.PlayerJetton temp = round.getGamblers().get(i);
                builder.append(ICON[i]);
                builder.append(" ");
                builder.append(temp.getMember().getId());
                builder.append(" - ");
                String jetton = temp.getJetton();
                if (jetton.length() > 15) {
                    builder.append(jetton, 0, 12).append("...");
                } else {
                    builder.append(jetton);
                }
                builder.append("\r\n");
            }

            for (; i < 6; i++) {
                builder.append(ICON[i]);
                builder.append(" - 等待加入\r\n");
            }

            builder.append("剩余时间 - ");
            builder.append(DateTool.formatTime("mm:ss", round.getExpireTime().getTime() - current));

            Driver.sendMessage(event, new Face(Face.手枪).plus(builder.toString()));

        }

    }


    private static class RouletteRound {

        private final Date expireTime = new Date(System.currentTimeMillis() + 600000);
        private final List<PlayerJetton> gamblers = new ArrayList<>(6);

        private boolean hint = true;
        private int loser = 6;

        //

        public boolean join(GroupMessageEvent event, Command command) {
            if (gamblers.size() > 6) return false;
            if (hint && gamblers.stream().anyMatch(item -> item.getMember().getId() == event.getSender().getId())) {
                Driver.sendAtMessage(event, "✔️ 经科学证实重复下注可有效增加被枪毙的机率");
                hint = false;
            }
            gamblers.add(new PlayerJetton(event.getSender(), command.getCommandBody(200)));
            return gamblers.size() == 6;
        }


        public int roll() {
            this.loser = ThreadLocalRandom.current().nextInt(6);
            return loser;
        }


        public int getLoser() {
            return loser;
        }

        public boolean isSinglePlayer() {
            long id = gamblers.get(0).getMember().getId();
            for (int i = 1; i < 6; i++) {
                long current = gamblers.get(i).getMember().getId();
                if (id != current) return false;
            }
            return true;
        }


        public String getAllJetton(long id) {
            List<PlayerJetton> jettons = gamblers.stream()
                                             .filter(item -> item.getMember().getId() == id)
                                             .collect(Collectors.toList());
            StringBuilder builder = new StringBuilder();
            for (RouletteRound.PlayerJetton jetton : jettons) {
                builder.append("\r\n");
                builder.append(jetton.getJetton());
            }
            return builder.toString();
        }


        public Date getExpireTime() {
            return expireTime;
        }

        public List<PlayerJetton> getGamblers() {
            return gamblers;
        }


        private static class PlayerJetton {

            private final Member member;
            private final String jetton;

            public PlayerJetton(Member member, String jetton) {
                this.member = member;
                this.jetton = jetton;
            }

            public Member getMember() {
                return member;
            }

            public String getJetton() {
                return jetton;
            }
        }
    }
}

