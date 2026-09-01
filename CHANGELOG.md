# Changelog / 更新日志

## [1.7.3] - 2026-08-28

### Added
- Fire attribute mobs now throw Splash Poison Potions instead of water bottles.
  赤焰属性生物改为投掷喷溅剧毒药水而非水瓶。
- Added a configurable cooldown for caster mob bottle throwing.
  新增施法生物投掷药水瓶冷却时间的配置项。

### Changed
- Reworked bottle throwing for attribute and caster mobs: fixed number of throws per round with a cooldown between rounds; throwing stops when the target's Wetness is full and resumes after the effect fades.
  重做属性/施法生物的投掷药水行为：每轮固定次数投掷，轮次间有冷却；目标潮湿满层时停止投掷，潮湿消退后恢复。
- Fire Counter now reduces all types of damage, including elemental damage.
  赤焰反制期间现在减免所有类型的伤害，包括属性伤害。

