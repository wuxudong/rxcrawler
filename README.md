# rxcrawler

##常见的爬虫

* crawler4j   
在抓取IP代理的项目[proxy-checker](https://github.com/wuxudong/proxy-checker) 使用。
	* 优点:
		* 简单易用
		* 支持 resume (停止服务后，重启继续之前的任务)
	* 缺点:
		* 仅处理GET
		* 单机

* webmagic  
	* 优点:
		* 结构清晰
		* 扩展性好
		* 开箱即用
	* 缺点:

		1. 对POST请求resume有缺陷(缺省保存url,没有保存post body)
		1. 当服务被终止时，可能丢失正在运行的请求(一般情况下，这不是什么问题， 但例如分类下的商品抓取，一页接一页，当服务被重启时，丢失了一个请求可能使得整个分类丢失)
		1. 基于bio，在高并发抓取下会消耗大量的线程。  
		
		前2个缺点基本可以通过扩展修改，但bio属于核心结构，无法修改。作者貌似也已不再维护。



## webmagic的rx-java改造
借鉴webmagic的结构和接口，但对核心的spider，downloader 使用 nio，rx-java 进行重写， 可以支持少量线程支持上千的并发抓取，配合squid和[proxy-checker](https://github.com/wuxudong/proxy-checker)获取的代理ip，极大提升抓取效率。

## JD
基于rxcralwer的例子，抓取京东的移动端接口。
