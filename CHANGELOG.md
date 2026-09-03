# Changelog / 更新日志

## [1.7.4] - 2026-09-01

### Added
- Added a config option for the Fire Counter damage reduction ratio (default 90%), adjustable from no reduction to full immunity.
  新增赤焰反制伤害减免比例的配置项（默认 90%），可在不减免到完全免疫之间自由调整。

### Changed
- Flammable Spore contagion is no longer limited to a single spread: a source entity now spreads spores repeatedly while its spore effect lasts. Each entity can only be infected once per spore effect.
  易燃孢子传染不再限定为一次：携带孢子的实体在效果持续期间可持续传染；每个实体在持有孢子效果期间只会被传染一次。

### Fixed
- Fixed Thunder caster mobs' inconsistent bottle hit/miss detection by unifying them with the shared throw logic used by other elements.
  修复雷霆施法生物投掷药水瓶命中/未命中判定与其他元素不一致的问题，统一使用共享投掷判定逻辑。

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

