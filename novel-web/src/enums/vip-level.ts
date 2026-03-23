/**
 * VIP等级枚举
 */
export enum VipLevel {
  /** 普通 */
  NORMAL = 0,
  /** VIP1 */
  VIP1 = 1,
  /** VIP2 */
  VIP2 = 2,
  /** VIP3 */
  VIP3 = 3,
  /** 金主 */
  GOLD_MASTER = 4
}

/**
 * VIP等级名称
 */
export const VipLevelLabels: Record<VipLevel, string> = {
  [VipLevel.NORMAL]: '普通用户',
  [VipLevel.VIP1]: 'VIP1',
  [VipLevel.VIP2]: 'VIP2',
  [VipLevel.VIP3]: 'VIP3',
  [VipLevel.GOLD_MASTER]: '金主'
}

/**
 * 获取VIP等级名称
 */
export function getVipLevelName(level: number): string {
  return VipLevelLabels[level as VipLevel] || '未知'
}