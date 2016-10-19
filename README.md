# om2m-bt-ble-zwave-bindings
OM2M plugins for automatic device detection in Bluetooth, BLE and Z-Wave networks.

This project contains 3 plugins for OM2M platform:
* plugin for detecting Bluetooth devices (org.eclipse.om2m.binding.bt),
* plugin for detecting Bluetooth Low Energy (BLE) devices (org.eclipse.om2m.binding.ble) and
* plugin for detecting Z-Wave nodes (org.eclipse.om2m.binding.zwave) + configuration files (ozw_config).

## Installation
These plugins are maven modules which can be included in OM2M version 1.0.0 (clone https://git.eclipse.org/r/om2m/org.eclipse.om2m).
Installation of a plugin is described here http://wiki.eclipse.org/OM2M/one/Developer. You have to:
* Add the plugin as a maven module to the OM2M parent project
* Add the plugin to the OM2M product(s).

Dependencies are already added.

### Bluetooth
Bluetooth plugin implementation uses Java library for Bluetooth - Bluecove (http://bluecove.org/).
It supports connecting to devices via *btspp* (RFCOMM) connections.

If BT plugin doesn't work and says library can't be found, it may be missing a file libbluetooth.so. This file has to be in your PATH so create a symbolic link to it in a folder which is in your PATH.


### Bluetooth Low Energy - BLE
BLE plugin implementation uses Linux's *bluez* tools: *gatttool* and *hcitool*.
*hcitool* is used for scanning nearby devices and *gatttool* is used for reading and writing data.

Before you build this plugin, you have to check if the correct HCI interface is specified (you can check it with command *hcitool dev*). Definition can be found in class org.eclipse.om2m.binding.ble.ReadGatt (currently is set to *hci1*).

### Z-Wave
Z-Wave plugin implementation uses a zwave4j library (https://github.com/zgmnkv/zwave4j) which is a Java wrapper for OpenZwave library (https://github.com/openzwave/, http://www.openzwave.net/).

Before you build this plugin, you have to correct the path to your configuration files (included *ozw_config* folder) and your Z-Wave controller serial port in file org.eclipse.om2m.binding.zwave.ZWaveMain.

## Requirements
* If you want to use Bluetooth plugin, you have to use 32-bit Java, because Bluecove doesn't support 64-bit.
* If you want to use BLE plugin, you have to run it in Linux operating system.
* If you want to use Z-Wave plugin, please check if your controller (and other nodes) is supported by OpenZwave library.


