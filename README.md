# Join OpenRune Friends

The server owner runs the game. Players install only a client.

## Windows or Linux

1. Install [RSProx from its official releases](https://github.com/blurite/rsprox/releases).
2. Open RSProx. Next to the target selector, click **+**, choose **From URL**, and paste:

   `https://raw.githubusercontent.com/oshabana/openrune-friends-client/main/proxy-targets.yaml`

3. Restart RSProx, select **OpenRune Friends**, choose **RuneLite**, and launch.
4. Sign in to world 255 with the account name and password you want to use on this server.

The import URL includes the server's public login key, world address, and revision 240 client settings. The game uses TCP port 43594. If RSProx reports that the target could not be downloaded, check that the import URL opens in your browser and retry.

## macOS (manual setup)

This Mac has launched RuneLite through RSProx and loaded game data from the public server. A full graphical login and a friend-ready Mac installer still need testing. RSProx's [private-server notes](https://github.com/blurite/rsprox#private-server-usage-osrs) list Windows and Linux, but its [macOS instructions](https://github.com/blurite/rsprox#macos-support-osrs) describe the loopback alias needed for a custom target.

1. Install Java 21 and download `rsprox-launcher.jar` from the [official RSProx releases](https://github.com/blurite/rsprox/releases) into Downloads.
2. In Terminal, add the alias for world 255 on the first custom target, then start RSProx:

   ```bash
   sudo ifconfig lo0 alias 127.0.255.3 up
   "$(/usr/libexec/java_home -v 21)/bin/java" -jar "$HOME/Downloads/rsprox-launcher.jar"
   ```

3. Import the same URL shown in the Windows/Linux section, restart RSProx, then select **OpenRune Friends** and **RuneLite**.

The alias may need to be added again after a Mac restart. If this is not the first custom target in RSProx, its group number may differ; the [RSProx alias instructions](https://github.com/blurite/rsprox#macos-support-osrs) explain the mapping.
