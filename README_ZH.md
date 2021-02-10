# 华为接触卫士

## 简介

该项目是华为移动服务中接触卫士的代码片段，主要包括：密钥管理、蓝牙（BLE）任务管理、密钥文件分析、数据存储。本项目还提供了示例代码，为您展示如何在已集成Google Exposure Notification的应用中快速集成Contact Shield SDK。

## 密钥管理

本模块描述了如何生成周期性密钥、动态共享码（DSC）和辅助元数据（SD）。启动接触卫士服务（Contact Shield）时，调用`ContactAdvBeaconGenerator.generate()`可以生成上述与周期性密钥相关的数据。关键步骤如下：

1. 获取当天的周期性密钥。注意每个应用都有自己的周期性密钥集合。调用`PeriodicKeyGenerator.getPeriodicKey()`以获取周期性密钥，其逻辑为：

  - 如果数据库中已有当天的周期性密钥，则调用该接口会立即返回此密钥。
  - 如果数据库中未保存当天的周期性密钥，该接口会调用`PeriodicKeyGenerator.cRNG()`生成一个新的随机数组作为密钥，并将其保存至数据库中，之后返回新生成的密钥。

2. DSC的生成依赖`DSC Key`和`PaddedData`。DSC Key派生自周期性密钥，可通过调用`DynamicSharingCode.getDscKey()`生成。PaddedData包含了当前时间的Interval Number值，通过调用`DynamicSharingCode.generatePaddedData()`生成。当准备好DSC Key和PaddedData后，调用`EncryptDecrypt.aesEcbEncrypt()`使用DSC Key加密`PaddedData`，从而生成DSC。所以，DSC是加密后的`PaddedData`。

3. SD的生成需要使用到`DSC`（DSC生成请参考步骤2）、`SD Key`和`metadata`。`SupplementaryData`类中的`SupplementaryData.getSdKey()`可用于生成SD Key。SD Key也是派生自周期性密钥,用于加密承载了BLE报文版本和传输功率的蓝牙metadata。具体实现细节请参见`ContactAdvBeaconGenerator`。最后，调用`EncryptDecrypt.aesCtrEncrypt()`生成新的SD。

## 蓝牙（BLE）任务管理

接触卫士使用BLE与其他设备进行数据交互，期间向周围广播和扫描DSC和SD。接触卫士将扫描端与广播端整合在一起，便于管理。`ContactBeacon`类承载了具体的广播报文。可以通过调用`pack()`和`unpack()`方法来转换广播报文和`ContactBeacon`。调用`ContactManage.startContact()`后，接触卫士将进行如下操作：

1. 检查蓝牙状态，确保您的设备支持BLE。如果当前蓝牙处于关闭状态，则打开蓝牙。

2. 调用`ContactBleScanner.startBleScan()`启动BLE扫描，并配置`BLE scanner timer`，使扫描在4秒后停止。BLE扫描端的具体实现细节集中在`ContactBleScanner`类中。当接触卫士广播时，此类会调用`ContactBeacon.unpack()`并将新发现的`ContactBeacon`传给`ContactManager`。由`ContactManager`来保`存ContactBeacon`的`DSC`、`SD`及`RSSI`。

3. 开始BLE广播。首先调用`ContactAdvBeaconGenerator.generate()`来生成`ContactBeacon`。`ContactBeaco`n可以连结`DS`C数据（对应当前时间戳Interval Number的值）和`SD`数据（加密的蓝牙metadata）。然后将生成的`ContactBeacon`传入`ContactBleAdvertiser.startBleAdv()`。BLE广播端的实现细节集中在`ContactBleAdvertiser`类中。

4. 当`BLE scanner timer`失效后，停止BLE扫描并调用`ContactDataManage.flushScanDataToDb()`将扫描到的所有数据从缓存写入数据库。然后配置`Contact Shield alarm`，以便在3-4分钟后开始下一次BLE扫描。

5. 每次触发`Contact Shield alarm`时，按照上述启动BLE扫描端持续扫描4秒，同时Contact Shield将检查BLE广播端数据更新的时间戳。如果超过十分钟未更新广播数据，则重新启动BLE广播端以更新`DSC/SD`数据。

## 密钥文件分析

`ContactAnalyze`类实现了密钥文件分析的核心步骤。`ContactAnalyze.analyzeKeyFileList()`是诊断入口，调用该接口时，需要传入诊断配置信息（`DiagnosisConfiguration`的实例）来计算接触风险值。

当app传入压缩文件后，`KeyFileParser.parseFiles()`可以将每个密钥文件转换成相应的周期性密钥，并基于每个周期性密钥的有效时间和生存周期来判断它们的合法性，丢弃那些超过潜伏期的周期性密钥。

在诊断过程中，密钥管理模块可以协助`ContactAnalyze.getTargetDsc()`为每个潜伏期内的密钥生成所有DSC，称之为目标DSC集合（`target DSCs`），然后对比目标DSC集合和本地数据库保存的BLE曾扫描到的病毒潜伏期内的DSC数据表。

一旦任一本地DSC与`目标DSC`匹配，便可确认当前用户与确诊者曾经有过近距离接触。接触卫士会根据匹配到的DSC数量，通过具体的`DiagnosisConfiguration`计算风险值。与此同时，接触详情（`Contact Detail`）、接触摘要（`Contact Sketch`）和接触窗口（`Contact Window`）都会同步更新。

诊断结束后，有三种方法可以获取诊断结果：

1. `ContactAnalyze.getContactSketch()` 提供传入的所有密钥压缩文件的摘要信息。
2. `ContactAnalyze.getDetailList()` 提供密钥压缩文件中已匹配的密钥的详细信息。
3. `ContactAnalyze.getContactWindows()` 提供`ContactWindow`模式下已匹配的密钥的详细信息。

诊断详情将保存至本地数据库以供后续查询，超过病毒潜伏期时将会被删除。

## 数据存储

本模块显示接触卫士会将哪些数据保存至数据库。底层数据库操作依赖于平台，此处不进行说明。以下是接触卫士工作时使用的主要数据：

| 类名 | 说明 |
|----------|-------------------|
| `ScanData` | BLE扫描数据，如DSC、SD、RSSI等。 |
| `PdkData`  | 历史周期性密钥。 |
| `ContactDetailData` | 诊断结果：接触详情、token等。 |
| `ContactSketchData` | 诊断结果：接触摘要、token等。 |
| `ContactWindowData` | 诊断结果：接触窗口、ScanInfoData等。 |

何时清除数据：

1. 当用户卸载接触卫士时，历史周期性密钥和诊断结果会被清除。
2. 数据超过病毒潜伏期后，`ContactDataManager.checkAndDeleteData()`会删除历史周期性密钥、诊断结果和扫描数据。
3. 用户可以在HMS Core Setting页删除历史周期性密钥和扫描数据。以EMUI为例：点击设置>应用管理>应用>HMS Core，然后点击右上角齿轮图标，选择COVID-19接触卫士>删除随机ID。

## Contact Shield Wrapper

根据此模块的封装接口，可以轻松更新已集成Google Exposure Notification的应用来集成Contact Shield SDK。详情请参考[ContactShieldWrapperReadme](./ContactShieldWrapper/ContacShieldWrapperREADME.md).
