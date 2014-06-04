# RoboAnt

## Android Studio
### Requirements
#### i386 Libraries
If you're on a 64-bit machine, you may need the following libraries:
```
sudo apt-get install ia32-libs
```
#### JDK
Android Studio asks for Oracle JDK, but OpenJDK will work as well. Only one of them needs to be installed.
##### OpenJDK
```
sudo apt-get install openjdk-7-jdk
```

##### Oracle JDK
```
sudo add-apt-repository ppa:webupd8team/java -y
sudo apt-get update
sudo apt-get install oracle-java7-installer
```

##### Swapping between JDKs
```
sudo update-alternatives --config java
```
### Installation
Download and extract [Android Studio](http://developer.android.com/sdk/installing/studio.html#download)
#### Add to Path
```
echo export PATH=\${PATH}:/<path>/android-studio/sdk/tools:/<path>/android-studio/sdk/platform-tools:/<path>/android-studio/bin >> ~/.bashrc

echo export ANDROID_HOME=/<path>/android-studio/sdk >> ~/.bashrc
```

#### Install Android APIs
Open the Android SDK Manager using this command.
```
android
```
_In the SDK Manager make sure you have installed SDK versions Android 2.3.3 (API 10), Android 3.2 (API 13) and Android 4.2.2 (API 17). It's not a whim to install three different versions: we use API 10 for Gingerbread compatible libraries, API 13 for Honeycomb libraries (that cannot work on Gingerbread) and API 17.

#### Update Android Studio
Open Android Studio using this command.
```
studio.sh
```
Make sure to update Android Studio immediately on first start.

#### Install ROS repository
These commands are designed for Ubuntu Precise. Change the first line as needed.
```
sudo sh -c 'echo "deb http://packages.ros.org/ros/ubuntu precise main" > /etc/apt/sources.list.d/ros-latest.list'
wget http://packages.ros.org/ros.key -O - | sudo apt-key add -
sudo apt-get update
```

#### Setup ROS
```
mkdir -p ~/android

wstool init -j4 ~/android/src https://raw.github.com/rosjava/rosjava/hydro/android_core.rosinstall
% Use this command instead, incase you need the sample apps.
% wstool init -j4 ~/android/src https://raw.github.com/rosjava/rosjava/hydro/android_apps.rosinstall

source /opt/ros/hydro/setup.bash
cd ~/android
catkin_make
```

#### Start Android Studio
```
source ~/android/devel/setup.bash
studio.sh
````

Then import project at ```~/android/src/android_core/settings.gradle```. After gradle invocation completes, you can index the project by clicking the tooltip that popped up at the top right corner.