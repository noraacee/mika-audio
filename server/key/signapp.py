#!/usr/bin/env python
import sys
from subprocess import call

call(['java', '-jar', 'signapk.jar', 'platform.x509.pem', 'platform.pk8', 'app-release-unsigned.apk', 'mikaaudio-server.apk']);
call(['adb', '-s', 'YT9110M4QW', 'install', '-r', 'mikaaudio-server.apk']);
call(['adb', '-s', 'YT9110M4QW', 'shell', 'monkey', '-p', 'com.mikaaudio.Server', '-c', 'android.intent.category.LAUNCHER', '1']);
