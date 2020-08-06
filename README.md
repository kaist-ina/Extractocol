# Extractocol
Automatic Protocol Behavior Analysis Framework for Android Apps.


## Running Extractocol
### Before you start
Copy the APK file of your application into the directory named (SerializationFiles).

### How to run
Run Extractocol by passing the application name as an argument

* The application name must be the same with the APK file. For example, if the apk file is myapp.apk, then you should pass 'myapp' to Extractocol.
* You need to allocate enough memory to the heap space. We used 12GB of memory when running Extractocol for the applications mentioned in the reference papers below.
* Extractocol might take from tens of minutes to several hours. Extractocol took 2 hours to run for an application named 'Wish', which is basically located in the directory 'SerializationFiles', when running on a machine with i5-7500@3.4GHz (4 cores).
* Extractocol would take a very long time to finish and sometimes it seems to run forever. Then, what you need to do is to reduce the value of the following parameters: maxMS and maxDepth. How extensive and in-depth Extractocol explores the source code is proportional to the value of the parameters. If you set those values high, Extractocol takes a long time to run but extracts richer information. If you set those values relatively low, then Extractocol finishes quickly but the result would be incorrect. The default value of the two parameters is 7 and 3, respectively. You can set those values by using the arg options: maxms and maxdepth. (Example: wish --maxms 7 --maxdepth 3)


## License
<code>CC BY-NC-SA</code> <a href="https://github.com/idleberg/Creative-Commons-Markdown/blob/spaces/4.0/by-nc-sa.markdown">Attribution-NonCommercial-ShareAlike</a>

## References

Jeongmin Kim, Hyunwoo Choi, Hoon Namgung, Woohyun Choi, Byungkwon Choi, Hyunwook Choi, Yongdae Kim, Jonghyup Lee  and Dongsu Han, <i>Enabling Automatic Protocol Behavior Analysis for Android Apps</i>, ACM CoNEXT 2016 [<a href="http://ina.kaist.ac.kr/~dongsuh/paper/kim-conext16.pdf" target="_blank">PDF</a>]

Byungkwon Choi, Jeongmin Kim, Daeyang Cho, Seongmin Kim, and Dongsu Han, <i>APPx: An Automated App Acceleration Framework for Low Latency Mobile App</i>, ACM CoNEXT 2018 [<a href="http://ina.kaist.ac.kr/~brad/appx.pdf" target="_blank">PDF</a>]


## Contact
Jeongmin Kim (appff at kaist.ac.kr)

Byungkwon Choi (cbkbrad at kaist.ac.kr)
