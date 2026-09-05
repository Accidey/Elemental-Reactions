# Changelog / 更新日志

## [1.7.6] - 2026-09-05

### Changed
- Water-bottle-throwing mobs (attribute mobs and ISS caster mobs) now check whether the target can currently gain the Wetness state before throwing: they hold fire while the target is scorched, wetness-immune (Nether/water animals/blacklist), paralyzed, frozen, or at maximum Flammable Spore stacks, and resume once the target can be wetted again.
  投掷水瓶的属性生物与ISS施法生物现在会在投掷前检测目标当前能否获得潮湿状态：目标处于焦灼、潮湿免疫（下界/水生生物/黑名单）、麻痹、冻结或易燃孢子满层时暂停投掷，待目标可以再次获得潮湿后继续投掷。
- Attribute counters (Fire/Frost/Thunder/Nature) now only trigger when the health drop was caused by an entity attack. Self-inflicted damage such as falls, burning, starvation, poison ticks, or the mod's own damage-over-time effects no longer triggers counters.
  属性反制（赤焰/冰霜/雷霆/自然）现在仅在被实体攻击导致血量下降时触发；摔落、灼烧、饥饿、中毒以及本Mod自身持续伤害等自行受伤不再触发反制。
- Projectile attacks now derive their element from the projectile itself (the trident or thrown item, or the bow/crossbow that fired it) instead of the shooter's hands: throwing a splash potion while an enchanted weapon is held in the other hand no longer triggers any elemental effects, and melee now only considers the main-hand item.
  投射物攻击现在只认投掷物自身携带的属性攻击附魔（三叉戟/投掷物物品、发射它的弓弩），不再从施法者手上其他物品推断：另一只手持属性附魔武器时投掷药水瓶不会再触发属性特效；近战也只认主手武器。

## [1.7.5] - 2026-09-03

### Changed
- Flammable Spore equipment corrosion now damages durability as a percentage of each item's max durability (configurable, default 0.1% per second) instead of a fixed amount.
  易燃孢子的装备耐久腐蚀由固定点数改为按装备最大耐久的百分比计算（可配置，默认每秒 0.1%）。
- Debug mode now only shows messages related to you: your own reactions, or those of mobs currently fighting you. Events between other creatures are no longer displayed.
  Debug 模式现在只显示与自身相关的信息：你自己的反应，或正在与你战斗的生物的反应；其它生物之间的事件不再显示。

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

