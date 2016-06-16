package com.tingfeng.asorm2.readme;
/**
 * 
 * @author huitoukest/tingfeng
 * @version 2.X
 * time 20160329
 */
public class ReadMe {
/**
 * 2.0.X
 * 	1.完成回调方式的数据库操作，理论上比同步加锁的方式效率更高；
 * 		用DataBaseTaskManager.addDataBaseTask(....);来添加新的数据库任务。
 * 		管理事务的时候要防止事务的多次开启,目前是一个service方法代表一次事务，事务之间相互独立；
 * 
 * 2.1.X将数据库由单个数据多表库改为多个数据库多表，提高并发性能；
 * 
 */
}
