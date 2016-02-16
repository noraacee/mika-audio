#!/usr/bin/env python
import sys
from subprocess import call

call(['java', '-jar', 'signapk.jar', 'platform.x509.pem', 'platform.pk8', 'app-release-unsigned.apk', 'mikaaudio-server.apk']);
call(['adb', '-s', sys.argv[1], 'install', '-r', 'mikaaudio-server.apk']);