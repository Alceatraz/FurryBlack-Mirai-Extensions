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
import studio.blacktech.furryblackplus.core.define.Command;
import studio.blacktech.furryblackplus.core.define.annotation.Executor;
import studio.blacktech.furryblackplus.core.define.moduel.EventHandlerExecutor;
import studio.blacktech.furryblackplus.core.utilties.logger.LoggerX;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Executor(
    value = "Executor-Roulette",
    outline = "俄罗斯轮盘赌",
    description = "提供赌注以参与一局俄罗斯轮盘赌",
    command = "roulette",
    usage = "/roulette 筹码 - 加入或者发起一局俄罗斯轮盘赌 重复下注可增加被枪毙的几率",
    privacy = {
        "获取命令发送人",
        "缓存群-成员-回合的数据 并在回合结束后丢弃"
    }
)
public class Roulette extends EventHandlerExecutor {


    private static final String[] ICON = {
        "1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣"
    };


    private HashMap<Long, RouletteRound> rounds;


    @Override
    public void init() {
        this.rounds = new HashMap<>();
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

        if (this.rounds.containsKey(group.getId())) {
            round = this.rounds.get(group.getId());
            if (round.getExpireTime().toEpochMilli() - current < 0) {
                this.rounds.remove(group.getId());
                round = new RouletteRound();
                this.rounds.put(group.getId(), round);
            }
        } else {
            round = new RouletteRound();
            this.rounds.put(group.getId(), round);
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

            this.rounds.remove(group.getId());

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
            builder.append(LoggerX.format("mm:ss", round.getExpireTime().toEpochMilli() - current));

            Driver.sendMessage(event, new Face(Face.手枪).plus(builder.toString()));

        }

    }


    private static class RouletteRound {

        private final Instant expireTime = Instant.ofEpochMilli(System.currentTimeMillis() + 600000);
        private final List<PlayerJetton> gamblers = new ArrayList<>(6);

        private boolean hint = true;
        private int loser = 6;


        public boolean join(GroupMessageEvent event, Command command) {
            if (this.gamblers.size() > 6) return false;
            if (this.hint && this.gamblers.stream().anyMatch(item -> item.getMember().getId() == event.getSender().getId())) {
                Driver.sendAtMessage(event, "✔️ 经科学证实重复下注可有效增加被枪毙的机率");
                this.hint = false;
            }
            this.gamblers.add(new PlayerJetton(event.getSender(), command.getCommandBody(200)));
            return this.gamblers.size() == 6;
        }


        public int roll() {
            this.loser = ThreadLocalRandom.current().nextInt(6);
            return this.loser;
        }


        public int getLoser() {
            return this.loser;
        }

        public boolean isSinglePlayer() {
            long id = this.gamblers.get(0).getMember().getId();
            for (int i = 1; i < 6; i++) {
                long current = this.gamblers.get(i).getMember().getId();
                if (id != current) return false;
            }
            return true;
        }


        public String getAllJetton(long id) {
            List<PlayerJetton> jettons = this.gamblers.stream()
                                             .filter(item -> item.getMember().getId() == id)
                                             .collect(Collectors.toList());
            StringBuilder builder = new StringBuilder();
            for (RouletteRound.PlayerJetton jetton : jettons) {
                builder.append("\r\n");
                builder.append(jetton.getJetton());
            }
            return builder.toString();
        }


        public Instant getExpireTime() {
            return this.expireTime;
        }

        public List<PlayerJetton> getGamblers() {
            return this.gamblers;
        }


        private static class PlayerJetton {

            private final Member member;
            private final String jetton;

            public PlayerJetton(Member member, String jetton) {
                this.member = member;
                this.jetton = jetton;
            }

            public Member getMember() {
                return this.member;
            }

            public String getJetton() {
                return this.jetton;
            }
        }
    }
}

