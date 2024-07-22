# NOTE: This Zip archive contains a ready-to-use compiled binary of the SSANS network simulator. To run the simulator, follow the instructions in the usage section below!

# Software Serial Arduino Network Simulator (SSANS)

This is a simple network simulator for Arduino. It is based on the ASSP protocol and allows you to simulate a network of Arduinos connected by serial ports. The simulator is written in Kotlin and uses a custom EmulatedSerial system to communicate with the Arduinos.

## Building

To build the simulator, you need to have the Kotlin 1.9.23 or later installed on your device. You can find out more about Kotlin from the [Kotlin website](https://kotlinlang.org/). Once you have Kotlin installed, you can build the simulator by executing the following command in the terminal:

For Linux and macOS:
```shell
./gradlew shadowJar
```

For Windows:
```shell
gradlew.bat shadowJar
```

The built JAR file will be located in the `build/libs` directory. This JAR file includes all the dependencies required to run the simulator by default.

Using shadowJar is a less elegant solution, and I would have preferred to use something like JLink to create a custom runtime image, but at present, I have not been able to get it to work.

## Usage

To run the Jar application, you need Java 21 or later installed on your computer. You can download it from the [Oracle website](https://www.oracle.com/java/technologies/downloads/#jdk21-windows), or you can use another Java distribution like OpenJDK. Any Java distribution that supports Java 21 or later should work.

Once you have Java installed, you can run the simulator by executing the following command in the terminal:

```shell
java -jar ssans.jar
```

If you have built the simulator using the `shadowJar` task, the JAR file will be located in the `build/libs` directory. You can run the simulator from there.

## Features

The simulator supports the following features:
- Adding and removing Arduinos from the network at runtime
- Sending and receiving messages between Arduinos
- Logging of network activity
- ~~Multiple Arduinos connected by emulated serial ports~~
- ~~Customizable network topology~~
- ~~Control over network speed~~

## Usage

When you run the simulator, you will see a window with a configuration pane on the left, a network pane in the centre, and a log pane on the right. The network pane displays all the Arduinos in the network, and the log pane displays the network activity.

### Adding/Removing Arduinos

To add an Arduino to the network, right-click on the network pane and select "Add Device". You will be asked to enter the name of the device. Once you have entered the name, the Arduino will be added to the network and will start listening for messages.

To remove an Arduino from the network, right-click on the Arduino in the network pane and select "Remove Device". The Arduino will be removed from the network, and all connections to it will be closed.

### Sending Messages

To send a message from one Arduino to another, right-click on the sending Arduino and select "Send Message". You will be asked to enter the name of the receiving Arduino and the message to send. Once you have entered the details, the message will be sent to the receiving Arduino.

### Logging

The simulator logs all network activity to the console. This includes messages sent and received by the Arduinos, as well as any errors that occur during communication. The log is updated in real-time and can be used to debug network issues.