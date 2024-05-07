/*
 * Copyright (C) 2021 Alceatraz @ BlackTechStudio
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms from the BTS Anti-Commercial & GNU Affero General.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty from
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * BTS Anti-Commercial & GNU Affero General Public License for more details.
 *
 * You should have received a copy from the BTS Anti-Commercial & GNU Affero
 * General Public License along with this program in README or LICENSE.
 */

package top.btswork.furryblack.extensions;

import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.UserMessageEvent;
import top.btswork.furryblack.FurryBlack;
import top.btswork.furryblack.core.handler.EventHandlerExecutor;
import top.btswork.furryblack.core.handler.annotation.Executor;
import top.btswork.furryblack.core.handler.common.Command;

import java.util.concurrent.ThreadLocalRandom;

@Executor(
  value = "Executor-Zhan",
  outline = "塔罗占卜",
  description = "抽取一张大阿卡那塔罗牌为某事占卜",
  command = "zhan",
  usage = {
    "/zhan XXX - 为某事占卜"
  },
  privacy = {
    "获取命令发送人"
  }
)
public class Zhan extends EventHandlerExecutor {

  private static final String[] CARD = {
    "O. THE FOOL 愚者正位\r\n盲目的 勇气 超越世俗 新的阶段 新的机会 追求自我的理想 旅行 超乎常人的自信 漠视道德舆论的",
    "O. THE FOOL 愚者逆位\r\n过于盲目 不顾现实 横冲直撞 拒绝负担责任的 违背常理的 逃避 危险的出行 幼稚",
    "I. THE MAGICIAN 魔术师正位\r\n成功 有实力 聪明能干 擅长沟通 机智的 唯我独尊 有能力 透过沟通获得智慧 影响他人 学习能力强 表达技巧良好",
    "I. THE MAGICIAN 魔术师逆位\r\n耍花招 瞒骗 失败 狡猾 善于谎言 能力不足 丧失信心 以不正当手段获取认同的",
    "II. THE HIGH PRIESTESS 女祭司正位\r\n纯真无邪 拥有直觉判断能力 揭发真相 运用潜意识的力量 掌握知识 正确的判断 理性思考 单恋 精神恋爱 对爱情严苛 回避爱情 对学业有助益",
    "II. THE HIGH PRIESTESS 女祭司逆位\r\n冷酷无情 无法正确思考 错误的方向 迷信 无理取闹 情绪不安 缺乏前瞻性的 严厉拒绝爱情",
    "III. THE EMPRESS 皇后正位\r\n温柔顺从 高贵美丽 享受生活 丰收 生产 温柔多情 维护爱情 女性魅力 母爱 创造力 财运充裕 快乐愉悦",
    "III. THE EMPRESS 皇后逆位\r\n骄傲放纵 过度享乐 浪费 嫉妒心 母性的独裁 占有欲 挥霍无度 纵欲 为爱颓废的 不伦之恋",
    "IV. THE EMPEROR 皇帝正位\r\n事业成功 物质丰厚 掌控爱情运 有手段 有方法 阳刚 独立自主 有男性魅力 大男人主义 有处理事情的能力 独断 想要实现野心与梦想",
    "IV. THE EMPEROR 皇帝逆位\r\n失败 过于刚硬 不利爱情 自以为是 权威过度 力量减弱 丧失理智 错误的判断 没有能力 过于在乎世俗的 权力欲望过重 徒劳无功",
    "V. THE HIEROPHANT 教皇正位\r\n有智慧 擅沟通 适时的帮助 找到真理 有精神上的援助 得到贵人帮助 一个有影响力的导师 找到正确的方向 学业出现援助 爱情上出现长辈的干涉 传达",
    "V. THE HIEROPHANT 教皇逆位\r\n过于依赖 错误的指导 盲目的安慰 无效的帮助 独裁 精神洗脑 以不正当手段取得认同 毫无能力 爱情遭破坏 第三者的介入",
    "VI. THE LOVERS 恋人正位\r\n爱情甜蜜 被祝福的关系 刚开始的爱情 顺利交往 美满结合 面临工作学业的选择 面对爱情的抉择 下决定的时刻 合作顺利的",
    "VI. THE LOVERS 恋人逆位\r\n遭遇分离 有第三者介入 感情不合 外力干涉 面临分手状况 爱情已远去 无法结合 遭受破坏的关系 爱错了人 不被祝福的恋情",
    "VII. THE CHARIOT 战车正位\r\n胜利 凯旋而归 不断征服 有收获 快速解决 充满信心 不顾危险 方向确定 坚持 冲劲十足",
    "VII. THE CHARIOT 战车逆位\r\n不易驾驭 失败 挫折 意外 障碍 挣扎 冲击 失去方向 丧失理智 鲁莽",
    "VIII. THE STRENGTH 力量正位\r\n内在的力量使得成功 正确 有信心 坦然 以柔克刚 有魅力 精神旺盛 有领导能力 理性 头脑清晰",
    "VIII. THE STRENGTH 力量逆位\r\n丧失信心 失去生命力 沮丧 失败 失去魅力 无助 情绪化 任性 退缩 没有能力 负面情绪",
    "IX. THE HERMIT 隐者正位\r\n有骨气 清高 有法力 自我修养,智慧 用智慧排除困难 给予正确的指导方向 有鉴赏力 三思而后行 谨慎",
    "IX. THE HERMIT 隐者逆位\r\n假清高 假道德 没骨气 没有能力 孤独寂寞 缺乏支持 错误的判断 被排挤 没有智慧 退缩 自以为是 与环境不合",
    "X. THE WHEEL OF FORTUNE 命运之轮正位\r\n忽然而来的幸运 即将转变的局势 顺应局势带来成功 把握命运给予的机会 意外的发展 不可预测的未来 突如其来的爱情运变动",
    "X. THE WHEEL OF FORTUNE 命运之轮逆位\r\n突如其来的厄运 无法抵抗局势的变化 事情的发展失去了掌控 错失良机 无法掌握命运的关键时刻而导致失败 不利的突发状况 没有答案 被人摆布 有人暗中操作",
    "XI. THE JUSTICE 正义正位\r\n明智 看清真相 正确的判断与选择 得到公平的待遇 走向正确的道路 理智与正义 维持平衡 诉讼得到正义与公平 重新调整使之平衡 不留情面",
    "XI. THE JUSTICE 正义逆位\r\n错误的决定 不公平待遇 没有原则 缺乏理想 失去方向 不合理 存有偏见 冥顽不灵 小心眼 过于冷漠 不懂感情 剑走偏锋",
    "XII. THE HANGED MAN 吊人正位\r\n心甘情愿 牺牲奉献 修练 不按常理 反其道而行 金钱损失 专注 坚定的信仰 长时间沉思 需要沉淀 成功之前的必经之道",
    "XII. THE HANGED MAN 吊人逆位\r\n精神上的虐待 心不甘情不愿 损失惨重 受到亏待 不满足 冷淡 自私自利 要求回报 逃离 错误",
    "XIII. DEATH 死亡正位\r\n结束旧有的现状 面临重新开始的时刻到了 将不好的过去清除 专注于新的开始 挥别过去的历史 展开心的旅程 做个了结 激烈的变化",
    "XIII. DEATH 死亡逆位\r\n已经历经了重生阶段了 革命已经完成 挥别了过去 失去 结束 失败 走出阴霾的时刻到了 没有回转余地",
    "XIV. TEMPERANCE 节制正位\r\n良好的疏导 希望与承诺 得到调和 平衡 沟通良好 成熟与均衡的个性 以机智处理问题 从过去的错误中学习 避免重蹈覆辙 净化 有技巧 有艺术才能",
    "XIV. TEMPERANCE 节制逆位\r\n缺乏能力 技术不佳 不懂事 需反省 失去平衡状态 沟通不良 缺乏自我控制力 不确定 重复犯错 挫败 受阻碍",
    "XV. THE DEVIL 恶魔正位\r\n不伦之恋 欲望 诱惑 违反世俗约定 不道德 特殊才能 消极 恐惧 愤怒 怨恨 阻碍 错误方向 不忠诚 秘密恋情",
    "XV. THE DEVIL 恶魔逆位\r\n脱离不伦之恋 挣脱枷锁 不顾道德 逃避 伤害 欲望化解 诅咒 欲望强大 不利环境 盲目判断",
    "XVI. THE TOWER 高塔正位\r\n关系破裂 难以挽救的局面 瓦解 损失 破坏 毁灭性 混乱 意外 悲伤 离别 失望 需要协助 生活需要重造",
    "XVI. THE TOWER 高塔逆位\r\n全军覆没 一切已破坏 毫无回转余地 失去 不安 暴力 厄运",
    "XVII. THE STAR 星星正位\r\n未来充满希望 新的诞生 无限希望 希望 达成目标 健康 纯洁 美好 好运 美丽的身心 时机 平静生活 平和环境",
    "XVII. THE STAR 星星逆位\r\n遥遥无期 失去信心 失去寄托 失去目标 感伤 放弃 毫无进展 过于虚幻 假想 偏执",
    "XVIII. THE MOON 月亮正位\r\n负面情绪 不安 恐惧 阴森恐怖 黑暗环境 低落 白日梦 忽略现实 未知危险 无法预料的威胁 胡思乱想 不脚踏实地的 沉溺的 固执的",
    "XVIII. THE MOON 月亮逆位\r\n度过低潮阶段 心情平复 黑暗即将过去 曙光乍现 挥别恐惧 恢复理智 看清现实 摆脱欲望 脚踏实地 走出谎言欺骗",
    "XIX. THE SUN 太阳正位\r\n前景看好 如日中天 成功 光明正大 热恋 美满婚姻 丰收 顺利 快乐 有成就 满足 旺盛",
    "XIX. THE SUN 太阳逆位\r\n热情消退 逐渐黯淡 失败 分离 傲慢 失去目标 失去活力 没有未来 贫乏 不快乐",
    "XX. THE LAST JUDGMENT 审判正位\r\n重新开始 觉醒 观念翻新 脱离束缚 满意 苦难结束 得到新启发 新开始",
    "XX. THE LAST JUDGMENT 审判逆位\r\n不公平 无法度过 旧事重演 固执 自以为是 思想狭隘 后悔 自责 不满意 挫败",
    "XXI. THE WORLD 世界正位\r\n完美结局 重新开始 生活完美阶段 获得成功 自由 完成 成功 心灵融合 自信十足 重大改变 完满结果",
    "XXI. THE WORLD 世界逆位\r\n不完美 过往的结束 缺乏自尊 难受 悲观 无法挽回 无法继续 残缺",

  };

  @Override
  public void init() {}

  @Override
  public void boot() {}

  @Override
  public void shut() {}

  @Override
  public void handleUsersMessage(UserMessageEvent event, Command command) {
    if (command.getParameterLength() == 0) {
      FurryBlack.sendMessage(event, "你不能占卜空气");
      return;
    }
    if (command.getCommandBody().length() > 100) {
      FurryBlack.sendMessage(event, "你占卜的太长了");
      return;
    }
    int i = random44();
    FurryBlack.sendMessage(event, "你因为 " + command.getCommandBody() + "\r\n抽到了 " + CARD[i]);
    logger.info("{} -> {} {}", event.getSender().getId(), i, command.getCommandBody());
  }

  @Override
  public void handleGroupMessage(GroupMessageEvent event, Command command) {
    if (command.getParameterLength() == 0) {
      FurryBlack.sendAtMessage(event, "你不能占卜空气");
      return;
    }
    if (command.getCommandBody().length() > 100) {
      FurryBlack.sendAtMessage(event, "你占卜的太长了");
      return;
    }
    int i = random44();
    FurryBlack.sendAtMessage(event, "你因为 " + command.getCommandBody() + "\r\n抽到了 " + CARD[i]);
    logger.info("{}:{} -> {} {}", event.getGroup().getId(), event.getSender().getId(), i, command.getCommandBody());
  }

  private int random44() {
    return ThreadLocalRandom.current().nextInt(44);
  }

}
