将\libs\library目录下的ioyouyun_ui_chat_library项目导入到eclipse
将ioyouyun_ui_chat_library关联引用到主项目中 步骤如下：

* 右击主项目Properties然后选中Android出现如图:  
 ![][2]
* 点击右边Add按钮选择ioyouyun_ui_chat_library后如图:  
 ![][3]  
* 点击ok

主项目中的AndroidManifest.xml中的修改(可参考\demo\ioyouyun_ui_chat_sdk_demo)：

* 增加以下permission 放到<manifest></manifest>标签中:
   
    ```
        <!-- ioyouyun ui chat sdk permission -->
        <uses-permission android:name="android.permission.READ_PHONE_STATE" />
        <uses-permission android:name="android.permission.INTERNET" />
        <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
        <uses-permission android:name="android.permission.GET_TASKS" />
        <uses-permission android:name="android.permission.VIBRATE" />
        <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
        <uses-permission android:name="android.permission.RECORD_AUDIO" />
        <uses-permission android:name="android.permission.WAKE_LOCK" />
        <uses-permission android:name="android.permission.BLUETOOTH" />
        <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
    ```
* 增加以下activity放到<application></application>标签中:

    ```   
            <!-- ioyouyun ui chat sdk activity -->
        <activity
            android:name="com.ioyouyun.ui.chat.opensource.ChatActivity"
            android:configChanges="orientation|keyboardHidden"
            android:launchMode="singleTask"
            android:screenOrientation="portrait"
            android:windowSoftInputMode="adjustUnspecified|stateHidden" />
        <activity
            android:name="com.ioyouyun.ui.chat.ui.UseCameraActivity"
            android:configChanges="orientation|keyboardHidden|navigation"
            android:screenOrientation="portrait" />
        <activity
            android:name="com.ioyouyun.ui.chat.ui.choosemultipictures.ChooseMultiPicturesActivity"
            android:configChanges="orientation|keyboardHidden"
            android:screenOrientation="portrait" />
        <activity
            android:name="com.ioyouyun.ui.chat.ui.ShowChatImgActivity"
            android:configChanges="orientation|keyboardHidden"
            android:windowSoftInputMode="adjustUnspecified|stateHidden" />
        <activity
            android:name="com.ioyouyun.ui.chat.ui.temp.ChatActivityNotiTmp" />
        <activity
            android:name="com.ioyouyun.ui.chat.opensource.ConversationActivity"
            android:configChanges="orientation|keyboardHidden"
            android:launchMode="singleTask"
            android:screenOrientation="portrait"
            android:windowSoftInputMode="adjustUnspecified|stateHidden" />
    ```
* 增加以下receiver放到<application></application>标签中:

    ```       
        <!-- ioyouyun ui chat sdk receiver -->
        <receiver
            android:name="com.ioyouyun.ui.chat.core.chat.push.ChatPushReceiver"
            android:exported="false" >
            <intent-filter>
                <action android:name="me.weimi.push.action.10001" />
            </intent-filter>
        </receiver>
注意：这里的APPID替换成自己的APPID，APPID是分配给第三方的clientId里面的一部分假设clientId为1-10001-abcdefghijklmn123456-android 那么APPID就是10001依次根据自己的clientId类推
    ```
* 增加以下service放到<application></application>标签中:
 
    ```       
            <!-- ioyouyun ui chat sdk service -->
        <service android:name="com.ioyouyun.wchat.countly.OpenUDID_service" >
            <intent-filter>
                <action android:name="org.OpenUDID.GETUDID" />
            </intent-filter>
        </service>
        <service
            android:name="com.weimi.push.service.WeimiPushService"
            android:exported="true"
            android:process=":push" >
            <intent-filter>
                <action android:name="me.weimi.PushService.BIND" />
            </intent-filter>
        </service>
    ```

首次执行 需在Activity中的onCreate里面初始化一次SDK(注意:请不要在Application中初始化)：WmOpenChatSdk.getInstance().init(this);

接口参考UI SDK文档以及\demo目录中ioyouyun_ui_chat_sdk_demo示例项目

混淆部分配置参考：
    -keepattributes InnerClasses
	-keepattributes *Annotation*

	-keep class **.R$*{*;}

	-dontwarn com.ioyouyun.ui.chat.**
	-keep class com.ioyouyun.ui.chat.**{*;}
    
关于修改聊天界面（比如单聊顶部的昵称、UI样式等）将youyun-ui-chat-sdk-opensource.jar替换成源码放到项目中，方法如下：

* 删除ioyouyun_ui_chat_library项目中的\libs\下的youyun-ui-chat-sdk-opensource-1.3.9.jar文件
* 将\开源部分\开源项目\ioyouyun_ui_chat_sdk_jar_opensource这个项目中的src下的源码复制到当前主项目中
* 对应的类大致为：聊天界面ChatActivity.java、聊天界面ListView适配器MessagAdapter.java、会话界面ConversationActivity.java、会话界面ListView适配器MessageAdapter.java。自行修改源码即可

[2]: ./resource/android_ui_chat_sdk_res/2.jpg
[3]: ./resource/android_ui_chat_sdk_res/3.jpg

