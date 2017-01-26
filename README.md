# Extractocol
Automatic Protocol Behavior Analysis Framework for Android Apps.

Currently, This version is incomplete.
So Extractocol may can't properly perform for some apps in our dataset. Because we have modified some core modules for other projects. When we finish implementing the modules, we will update that. 
<b>we will finish this work by Feb, 2017.</b>

# How To
Extractocol has two main modules that are program slicing and signature building modules. We extended flowdroid to slice network related instructions in APK. We implement signature building module using Soot framework. Currently, the two modules are divided so you should run the modules respectively (We plan to provide an all in one script). We implement Extractocol using Eclipse and this repository includes eclipse project setting file. Therefore, you can easily set up a development environment using import existing workspace menu in eclipse.

<b>** Program Slicing Module</b>

<b>A.Main Method</b>
<br>soot.jimple.infoflow.android.TestApps.Test

<b>B.Arguments</b>
<br>APK, AndroidSDK path, --noexceptions --nostatic --aplength 1 --aliasflowins --layoutmode none --nocallbacks [--backward or --forward]

<b>C.Example</b> : Extracting request(backward) program slice of wish.apk.
<br>wish.apk
D:\extractocol\AndroidSDK
--noexceptions --nostatic --aplength 1 --aliasflowins --layoutmode none --nocallbacks
--backward

<b>** Signature Building Module</b>

<b>A.Main Method</b>
<br>Extractocol.Tester.BackendTester_Request

<b>B.Arguments</b>
<br>--app (AppName) --backward

<b>C.Example</b> : Building request signatures for wish app. If you want to build response signatures, you should run Extractocol.Tester.BackendTester_Response class.
<br>--app wish --backward


# License
<code>CC BY-NC-SA</code> <a href="https://github.com/idleberg/Creative-Commons-Markdown/blob/spaces/4.0/by-nc-sa.markdown">Attribution-NonCommercial-ShareAlike</a>

# Reference

Jeongmin Kim, Hyunwoo Choi, Hoon Namgung, Woohyun Choi, Byungkwon Choi, Hyunwook Choi, Yongdae Kim, Jonghyup Lee  and Dongsu Han, <i>Enabling Automatic Protocol Behavior Analysis for Android Apps</i>, ACM CoNEXT 2016 [<a href="http://ina.kaist.ac.kr/~dongsuh/paper/kim-conext16.pdf" target="_blank">PDF</a>]


# Contact
Jeongmin Kim (appff at kaist.ac.kr)

