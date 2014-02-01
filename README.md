# TinySnap 2

## Contents

  * [What is TinySnap?](#what-is-tinysnap)
  * [Requirements](#requirements)
  * [Installation](#installation)
  * [Usage](#usage)
  * [Pull Requests](#pull-requests)

## What is TinySnap?<a id="#what-is-tinysnap"></a>
TinySnap is a project that aims to simplify the process of taking a screenshot. With TinySnap, a simple click-and-drag process allows for easy capturing of sections of the screen, or even the entire screen, and handles uploading the snap for sharing in the background.

## Requirements<a id="#requirements"></a>

  * Windows (other OS support coming soon; tested on Windows 7/8)
  * An FTP account/web server (used to upload the snaps to)

## Installation<a id="#installation"></a>

  1. Run `mvn install` to install dependencies and build TinySnap, or download a pre-compiled exe/jar from the [releases page](https://github.com/daviga404/TinySnap2/releases).
  2. Open TinySnap (located in 'target' if built with maven)
  3. Enter details of FTP access, and public URL of web server.

## Usage<a id="#usage"></a>
Currently, the set hotkey is `CTRL + 1`, but a configurable hotkey is a planned feature. After pressing the hotkey, simply click and drag the area you would like to capture, and release when ready to upload. If in classic mode, the URL will be copied to the clipboard. Otherwise, you can click the popup in the bottom right to obtain the link (disappears after 30 seconds if not clicked).

## Pull Requests<a id="#pull-requests"></a>
To install the dependencies, simply run
```
mvn clean install
```
Feel free to submit a pull request with any bug features/improvements!