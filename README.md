#SBT4jEdit

The goal of this plugin is to make SBT easy to use while developing

Look at the [downloads page](https://github.com/StefanE/SBT4jEdit/downloads) to see latest release version.

##Implemented
- Correct dependencies for plugin
- SBT console
- Commands completion. I have hardcoded most commands:), have problems with jline
- Error highlighting when compiling/testing
- Support for continous execution
- Errors,warnings etc. should be colored in console (Should be refined)
- Make console colors configurable (some of them are, not all. Yet)

##TODO
- Get real completion in sbt console
- Remove repetition of entered command in console
- easy processors installation/guide
- Setup sbt project with GUI

##Showcase
[Short video presentation](http://bit.ly/gazvQU)

##Howto

Download the [plugin](https://github.com/StefanE/SBT4jEdit/downloads)

Extract the jars to jEdits "jars" folder.

First time you should load the plugin from "Plugin Manager"

When you start SBT, it will take the rootpath from your current active project in Project Viewer. If you haven't chosen a project, it will ask for a path to start in.

Now it should be ready to use...