#!/bin/bash
keytool -genkeypair -alias gemfire -dname "CN=jblum-mbpro.local" -validity 365 -keypass s3cr3t -keystore ./trusted.keystore -storepass s3cr3t -storetype JKS
