1. Copy files .vscode/*.template* without '.template' part, adjust JDK path in settings.json and projectName=$DIRECTORY_NAME in launch.json
2. Run Terminal, menu "Terminal - New terminal", type there "cmd" for Windows and "bash" for *NIX and run commands:
code --install-extension redhat.java
code --install-extension vscjava.vscode-java-debug
code --install-extension vscjava.vscode-java-test
code --install-extension eamodio.gitlens
code --install-extension mhutchie.git-graph
code --install-extension donjayamanne.githistory
code --install-extension alphabotsec.vscode-eclipse-keybindings
code --install-extension joaompinto.asciidoctor-vscode

Notes ONLY for Troubleshooting:
1. To re-create project use command "Java: Clean Java Language Server Workspace" in command palette (Ctrl + Shift + P)
2. Options for settings.json:
    "java.import.gradle.wrapper.enabled": false,
    "java.import.gradle.version": "6.9.1",
3. Generate .classpath file, run in console:
    gradlew cleanEclipse eclipse
