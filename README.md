# Immersive Machinery

A bunch of rustic machinery to transport, mine, and automate things, while staying close to the vanilla style.

基于 [Luke100000/ImmersiveMachinery](https://github.com/Luke100000/ImmersiveMachinery) 的改进版本，由 [zerocreator0](https://github.com/zerocreator0) 修改。

## 修改内容

### 蜂竹无人机 (Bamboo Bee)
- 修复黑白名单过滤功能
- 新增对精妙存储 (Sophisticated Storage) Fabric 版的兼容（通过 Fabric Transfer API）

### 红石羊 (Redstone Sheep)
- 新增区块模式：限制工作范围在初始位置所在的单一区块，可在配置中开关
- 新增对二格高作物（如玉米）的兼容：收割时自动移除上半部分
- 新增对瓜类（南瓜/西瓜）的收割：通过上方寻路策略破坏收集
- 作物识别可配置：`validCrops` 控制作物识别，`stemFruits` 控制实心果实寻路策略
- 燃料槽隔离：漏斗只能将燃料送入燃料槽

### 其他
- 修复配置界面反射崩溃问题
- 添加中文汉化 (zh_cn.json)
- `fabric-transfer-api-v1` 移至推荐依赖，无 Transfer API 也能正常加载

## 配置说明

配置文件位于 `config/immersive_machinery.json`：

| 配置项 | 类型 | 说明 |
|--------|------|------|
| `redstoneSheepChunkOnlyMode` | boolean | 是否限制红石羊在初始区块工作 |
| `redstoneSheepMinHorizontalScanRange` | int | 红石羊扫描范围（非区块模式时） |
| `fuelTicksPerHarvest` | int | 每次收割消耗的燃料刻数 |
| `validCrops` | Map<String, Boolean> | 可收割作物列表，`true`=收割，`false`=禁用 |
| `stemFruits` | Set<String> | 实心果实列表，使用上方寻路策略 |

### 添加其他 Mod 的作物

普通作物（继承 CropBlock）自动兼容，无需配置。

实心果实作物（类似南瓜/西瓜）需同时添加到两个配置项：

```json
{
  "validCrops": {
    "modid:custom_fruit": true
  },
  "stemFruits": [
    "modid:custom_fruit"
  ]
}
```

## 构建

```bash
./gradlew build
```

输出 JAR 位于 `fabric/build/libs/`。

## 依赖

- Minecraft 1.20.1
- Fabric Loader >= 0.14.21
- Fabric API
- Immersive Aircraft >= 1.2.0
- (推荐) Fabric Transfer API — 用于精妙存储兼容

## License

GPL-3.0-only
