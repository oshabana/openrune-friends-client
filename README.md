# ScapeMar

ScapeMar client downloads for world 255. The server is already hosted. [Choose your download](https://oshabana.github.io/scapemar-client/).

## Download

Choose your computer from the [latest release](https://github.com/oshabana/scapemar-client/releases/latest):

| Computer | File | Open after extracting |
| --- | --- | --- |
| Mac | `ScapeMar-macos.zip` | `Launch ScapeMar.command` |
| Windows | `ScapeMar-windows.zip` | `Launch ScapeMar.bat` |
| Linux | `ScapeMar-linux.zip` | `Launch ScapeMar.sh` |

Install [Java 21](https://adoptium.net/temurin/releases/?version=21) first. Extract the whole zip, then open the launcher. On Mac, approve the network prompt. If macOS blocks the downloaded launcher, right-click it and choose **Open**.

The launcher installs the public server connection and selects **Quest Helper** and **117 HD** for RuneLite's Plugin Hub. RSProx then opens: select **ScapeMar**, choose **RuneLite**, and click **Launch**. Sign in to world 255 with the account name and password you want to use here.

If either plugin does not appear, open RuneLite's wrench icon, open **Plugin Hub**, search for **Quest Helper** or **117 HD**, and click **Install**. The plugins come from RuneLite's Plugin Hub and receive updates there.

The bundle contains the [official RSProx launcher](https://github.com/blurite/rsprox/releases/tag/v1.0), the public connection profile, and our setup launcher. It contains no server code, account credentials, or modified RuneLite binary. The [release checksums](https://github.com/oshabana/scapemar-client/releases/latest) let you check the downloaded zips. ScapeMar is unofficial and is not affiliated with Jagex, RuneLite, or RSProx.

## If you already use RSProx

The launcher can migrate an unchanged earlier profile to ScapeMar. It leaves any other existing RSProx target file alone. If you have a different target file, import this URL in RSProx instead:

`https://raw.githubusercontent.com/oshabana/scapemar-client/main/proxy-targets.yaml`

On Mac, the first custom target needs `127.0.255.3` on `lo0`. The bundle adds it when needed. If you have multiple custom targets, follow the [RSProx group ID instructions](https://github.com/blurite/rsprox#macos-support-osrs).

## Maintainer

Run `./build-bundles.sh` with Java 21 to make the three release zips. The build verifies the official RSProx v1.0 launcher SHA-256 before packaging. The public address and login modulus live in `proxy-targets.yaml`; rebuild and republish when they change.
