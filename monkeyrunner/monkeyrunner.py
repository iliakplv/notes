from com.android.monkeyrunner import MonkeyRunner, MonkeyDevice

device = MonkeyRunner.waitForConnection()
device.installPackage('.apk')             # todo path

package = 'com.iliakplv.notes'
activity = 'com.iliakplv.notes.gui.main.MainActivity'
runComponent = package + '/' + activity
device.startActivity(component=runComponent)

device.press('KEYCODE_MENU', MonkeyDevice.DOWN_AND_UP)
result = device.takeSnapshot()
result.writeToFile('/shot1.png','png')    # todo path