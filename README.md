# Extractocol
Automatic Protocol Behavior Analysis Framework for Android Apps.


## Running Extractocol
### Before you start
Copy the APK file of your application into the directory named `SerializationFiles`.

### How to run
Run Extractocol by running `src/extractocol/tester/Extractocol.java` with your application name as an argument

* The application name must be the same with the APK file. For example, if the apk file is myapp.apk, then you should pass 'myapp' to Extractocol.
* You need to allocate enough memory to the heap space. We used 12GB of memory when running Extractocol for the applications mentioned in the reference papers below.
* Extractocol might take from tens of minutes to several hours. Extractocol took 2 hours to run for an application named 'Wish', which is basically located in the directory 'SerializationFiles', when running on a machine with i5-7500@3.4GHz (4 cores).
* Extractocol would take a very long time to finish and sometimes it seems to run forever. Then, what you need to do is to reduce the value of the following parameters: `maxMS` and `maxDepth`. How extensive and in-depth Extractocol explores the source code is proportional to the value of the parameters. If you set those values high, Extractocol takes a long time to run but extracts richer information. If you set those values relatively low, then Extractocol finishes quickly but the result would be incorrect. The default value of the two parameters is 7 and 3, respectively. You can set those values by using the arg options: `maxms` and `maxdepth`. (Example: wish --maxms 5 --maxdepth 2)


## Outputs of Extractocol
* After Extractocol finishes, you can find a directory whose name is the same with your application name inside of the directory named `SerializationFiles`. For example, you can find the directory named `wish` when you run Extractocol for `wish.apk`.
* In the directory, you can find the directory named `[Signatures]`.
* In the directory `[Signatures]`, there is an html file named `Dependency Graph.html`. When you open it, you can see the dependency graph of the HTTP transactions that the application generates.
* In the directory named `Regex Signatures`, you can find the message format for the HTTP requests the application generates.
* You can find the detailed information of the dependency in the directory named `Proxy Signatures`. When you open `Dependency_XX.detailSig` in the directory, you can find the details. When you open `extractocol/SerializationFiles/wish/[Signatures]/Proxy Signatures/Dependency_18.detailSig`, for example, you can see the result. The meaning of the first line is that the `<0>` field in the body(`BODY`) of the HTTP message number 18 is retrieved from the response body of the HTTP message number 91. The following words `data`, `home_page_info`, `order_status_row`, and `view_all_deep_link` are the key names of the JSON body of the HTTP message number 91. You can find where the field `<0>` is in the file named `ProxySig_18.body`. The result in the file `proxySig_18.body` is a regex so `|` between `<0>` and `<1>` means the OR operation.


## State of Extractocol
We had updated Extractocol until late 2018. Extractocol would be unable to handle the features of Android and third party frameworks such as Retrofit, RxJava, OkHTTP, etc. released after then. 
That means Extractocol would fail to extract the message formats and dependency relationships from the applications that use the features.


## License
<code>CC BY-NC-SA</code> <a href="https://github.com/idleberg/Creative-Commons-Markdown/blob/spaces/4.0/by-nc-sa.markdown">Attribution-NonCommercial-ShareAlike</a>

## References

Jeongmin Kim, Hyunwoo Choi, Hoon Namgung, Woohyun Choi, Byungkwon Choi, Hyunwook Choi, Yongdae Kim, Jonghyup Lee  and Dongsu Han, <i>Enabling Automatic Protocol Behavior Analysis for Android Apps</i>, ACM CoNEXT 2016 [<a href="http://ina.kaist.ac.kr/~dongsuh/paper/kim-conext16.pdf" target="_blank">PDF</a>]

Byungkwon Choi, Jeongmin Kim, Daeyang Cho, Seongmin Kim, and Dongsu Han, <i>APPx: An Automated App Acceleration Framework for Low Latency Mobile App</i>, ACM CoNEXT 2018 [<a href="http://ina.kaist.ac.kr/~brad/appx.pdf" target="_blank">PDF</a>]


## Contact
Jeongmin Kim (appff at kaist.ac.kr)

Byungkwon Choi (cbkbrad at kaist.ac.kr)
