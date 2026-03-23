/**
 * 小说频道枚举
 */
export enum Channel {
  /** 男频 */
  MALE = 1,
  /** 女频 */
  FEMALE = 2
}

/**
 * 频道名称
 */
export const ChannelLabels: Record<Channel, string> = {
  [Channel.MALE]: '男频',
  [Channel.FEMALE]: '女频'
}