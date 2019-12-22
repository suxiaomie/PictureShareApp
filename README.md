# PictureShareApp
share picture app using okhttp
图片分享系统，使用android3.0、Myeclipse2014和MySQL，实现了登录注册、上传图片、浏览图片、点赞、分享，使用需修改android的java-com.example.mcl.picshare-utils-OkHttpUtil的ip，改为电脑局域网ip（手机、电脑连另一个手机热点），MySQL新建demo数据库,新建androidrecord、androiduser表，Myeclipse与MySQL连接，修改Myeclipse的hibernate.cfg.xml的用户名和密码
MyEclipse:https://github.com/suxiaomie/MyEclipse-PictureShareApp.git
androidrecord：id-int(11),主键，自动递增,name-varchar(255),count-int(11),fronId-int(11)
androiduser:id-int(11),主键，自动递增,user-varchar(255),pswd-varchar(255)

效果图：长按图片保存或分享
https://github.com/suxiaomie/PictureShareApp/blob/master/login.jpg
