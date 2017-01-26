# Extractocol
Automatic Protocol Behavior Analysis Framework for Android Apps.
This version is incomplete and we will finish this work by Feb, 2017.

# How To
Extractocol has two main modules that are program slicing and signature building modules. We extended flowdroid to slice network related instructions in APK. We implement signature building module using Soot framework. Currently, the two modules are divided so you should run the modules respectively (We plan to provide an all in one script). We implement Extractocol using Eclipse and this repository includes eclipse project setting file. Therefore, you can easily set up a development environment using import existing workspace menu in eclipse.

** Program Slicing Module 
1. Main Method: soot.jimple.infoflow.android.TestApps.Test
2. Arguments
APK, AndroidSDK path, --noexceptions --nostatic --aplength 1 --aliasflowins --layoutmode none --nocallbacks [--backward]

Example - (Extracting request (backward) program slice of wish.apk)

wish.apk
D:\extractocol\AndroidSDK
--noexceptions --nostatic --aplength 1 --aliasflowins --layoutmode none --nocallbacks
--backward

** Signature Building Module
1. Main Method: Extractocol.Tester.BackendTester_Request
2. Arguments
--app (AppName) --backward

Example: (Building request signatures for wish app)

--app wish --backward


# License
<code>CC BY-NC-SA</code> <a href="https://github.com/idleberg/Creative-Commons-Markdown/blob/spaces/4.0/by-nc-sa.markdown">Attribution-NonCommercial-ShareAlike</a>

# Reference

Jeongmin Kim, Hyunwoo Choi, Hoon Namgung, Woohyun Choi, Byungkwon Choi, Hyunwook Choi, Yongdae Kim, Jonghyup Lee  and Dongsu Han, <i>Enabling Automatic Protocol Behavior Analysis for Android Apps</i>, ACM CoNEXT 2016 [<a href="http://ina.kaist.ac.kr/~dongsuh/paper/kim-conext16.pdf" target="_blank">PDF</a>]


# Contact
Jeongmin Kim (appff at kaist.ac.kr)

