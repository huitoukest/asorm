# asorm
a android sqlite3 orm tools
# 前言

作为一个java工程师,做点android开发的时候,自己做了个orm数据库的映射框架,使用比较方便,所以就放出来;
asorm1.0beta-->android Sqlite3 ORM;

1. 本数据库支持自动创建表格,表的名字是你指定的Entity的名字;

   如果要更改建表的名称,可以在你的Entity中复写BaseEntity中的public String getTableName()方法即可;
2. 支持事务管理;

3. 如果需要实现外键和存储过程需要自己编写相关sql语句;

4. 不支持外键映射,只是支持对象和sql表之间的映射;

5. 本数据库依赖于fastJson,请导入相关的包,对于Entity中不需要序列化的属性,请用@JSONField(serialize=false)注解;
# 开始使用
1. 建立实体
这个和j2ee上使用Hibernate等框架是类似的,简历和数据库中表格相对应的类;
此类必须继承自BaseEntity;

示例:
```java
public class User extends BaseEntity {  
  
    public Long Id;   
    public String Login_Name;  
    public String Password;  
    public String Real_Name;  
    public Long State;  
    public String Uid;  
     @Entity_FieldProperty(FieldType=FieldType.JsonObject,cls=Sys_User_Info.class)  
     public Sys_User_Info UserInfo;  
     @Override  
    public Long getPrimaryKeyValue() {  
        return Id;  
    }  
    @Override  
    public String getPrimaryKeyName() {  
        return "Id";  
    }  
}
```
其中你必须Override其中的两个方法public Long getPrimaryKeyValue()
和public String getPrimaryKeyName(),返回此实体的主键值与主键名称;
此实体对应的表的名称默认为其类名,不可更改;

主键只能是Long或者int类型;

默认支持的属性类型有:int,Integer,Short,short,Double,double,Float,float,Date,String,Long,long,Byte,byte;以及注解过的对象

对于实体Entity类的属性,可以通过@Entity_FieldProperty注解来对其映射方式做说明;

对于Entity_FieldProperty的FieldType=FieldType.XXX有四种方式;

JsonList,JsonObject,Base,Transient,具体功能见下面代码说明;
注解内容:

```java
/** 
 * 指定当前Entity中的属性的属性,即自生的类别和转换的方式 
 * JsonList,JsonObject,Base,Transient 
 * 当FieldType.Base的时候,将会按照默认识别的类型使用,即此时的cls属性不会生效 
 * JsonList,JsonObject表示此对象需要转换为一个json对象或者字符串; 
 * Transient表示此对象,不进行数据库的存和取操作,选择Transient的时候,cls属性不会生效 
 */  
@Target(ElementType.FIELD)  
@Retention(RetentionPolicy.RUNTIME)  
public @interface Entity_FieldProperty {  
      
    /** 
     * 指定一个类型是FieldType的,表现为FieldType=默认为FieldType.Base的Annotation 
     * @return 
     */  
    FieldType FieldType() default FieldType.Base;  
    /** 
     * 指定一个类型是Class的,表现为cls=默认为String.class的Annotation 
     * @return 
     */  
    Class<?> cls() default String.class;  
    /** 
     * 指定当前属性的类型 
     * 
     */  
    public enum FieldType{          
        JsonList,JsonObject,Base,Transient;  
        };            
}  
```
对于对象,本框架是以json方式序列化属性为字符串之后存储到数据库的;

2. 初始化数据库和asorm
在android使用此框架操作数据之前,需要对数据库进行初始化;

调用DataBaseManager.initializeInstance(Context c,EntityInfos entityInfos);方法来初始化数据库和asorm;

只有初始化之后才可以使用和数据库相关的操作;建议在app启动的第一个类中最先调用此方法;并传入Application 的context

EntityInfos是一个接口,我们需要实现此接口,此接口包含如下方法:
```java
/** 
     * 返回Entity类,你的Entity类的set 
     * @return 
     */  
    public HashSet<Class<? extends BaseEntity>> getEntityClasses();  
    /** 
     * 返回数据库的名称,如"123"; 
     * @return 
     */  
    public String getDataBaseName();  
    /** 
     * 返回数据库的版本,如1; 
     * @return 
     */  
    public int getDataBaseVersion();  
    /** 
     * 当数据库版本变化的时候先调用此方法; 
     * 然后返回true表示调用默认方法,删除所有数据然后重新建立表; 
     * 然会false表示不调用默认方法,此方法不会自己管理事务; 
     * @return 
     */  
    public boolean onDataBaseVersionChange(SQLiteDatabase db);  
```
示例:
```java
public class MyEntityInfos implements EntityInfos{  
  
    @Override  
    public HashSet<Class<? extends BaseEntity>> getEntityClasses() {  
        HashSet<Class<? extends BaseEntity>> set=new HashSet<Class<? extends BaseEntity>>();  
        set.add(User.class);  
        return set;  
    }  
  
    @Override  
    public String getDataBaseName() {  
        return "dview242.db";  
    }  
  
    @Override  
    public int getDataBaseVersion() {  
        return 1;  
    }  
    @Override  
    public boolean onDataBaseVersionChange(SQLiteDatabase db) {  
        return true;  
    }  
}  
```
初始化中,主要做如下工作:
建立和Entity对应的表,同时保持表名称和主键信息;

之后我们可以调用EntityManager.getTableNameByCls(Class<? extends BaseEntity> cls);方法来获取表明,

同样,可以调用EntityManager的public static String getPrimaryKeyNameByCls(Class<? extends BaseEntity> cls);方法来获取主键名称;

3. 建立service层;
一个app应该有数据库层;控制层;表现层;

而asorm框架主要封装了数据层的操作的dao层,我们需要在控制层做好事务控制;

框架要求,所有的和数据库操作相关的顶层封装都需要建立事务的控制下,

意思就是你操作数据库的任何操作,都必须使用此框架提供的事务控制方式来控制事务,同时不要自己打开关闭数据库和事务,而是应该交给asorm来控制;

 - 第一步:建立Service方法接口,并继承自BaseServiceI接口:
```java
public interface LoginTypeServiceI extends BaseServiceI{  
    @SqliteTransaction(Type=TransActionType.Write)  
    public abstract LoginType getLoginType() throws Exception;  
    @SqliteTransaction(Type=TransActionType.Write)  
    public abstract boolean saveOrUpdate(LoginType loginType) throws Exception;  
  
}
```
```java
public interface BaseServiceI {  
    @SqliteTransaction(Type=TransActionType.Write)  
    public BaseEntityDaoI getBaseEntityDao();  
}
```
 BaseServiceI的主要作用是返回BaseEntityDaoI这个接口;
 - 第二步:建立Service的实现类;
实现自定的service接口,并实现其方法;

对于public BaseEntityDaoI getBaseEntityDao();可以继承类BaseService,此类中复写了此方法;
```java
public class BaseService implements BaseServiceI{  
    @SqliteTransaction(Type=TransActionType.Write)  
    public BaseEntityDaoI getBaseEntityDao(){  
        return BaseEntityDao.getBaseEntityDaoI();  
    }  
}  
```
当然,用户也可以自己复写方法,内容如下 BaseEntityDao.getBaseEntityDaoI();
对于BaseEntityDaoI系统提供的是BaseEntityDao实现,用户熟读源码后可以自行实现内容;

 - 第三步:注解事务
     利用BaseEntityDao我们可以对对象进行crud等操作;在操作之前,需要对方法事务进行管理;

     在此方法对应的接口的方法上,用注解来说明事物;
```java
public interface LoginTypeServiceI extends BaseServiceI{  
    @SqliteTransaction(Type=TransActionType.Read)  
    public abstract LoginType getLoginType() throws Exception;  
    @SqliteTransaction(Type=TransActionType.Write)  
    public abstract boolean saveOrUpdate(LoginType loginType) throws Exception;  
  
}
```

 目前支持Read和Write两种方式,底层的实现实际上都是得到一个可写的数据库连接;
不同的是对事物的管理不同;对于读写方式,请选择Write

 - 第四步 :操作对象
对Entity对象进行增删改查等操作,由于方法之间事务的独立性,service层也可以考虑做成单例;
示例:
```java
public class LoginTypeService implements LoginTypeServiceI {  
private BaseEntityDaoI baseDao;  
  
       private LoginTypeService(){  
           baseDao=BaseEntityDao.getBaseEntityDaoI();  
       }  
         
       public static LoginTypeServiceI getNewProxyInstance(){  
           return (LoginTypeServiceI) MySqliteTransactionProxy.getSqliteTransactionProxy(LoginTypeServiceI.class,new LoginTypeService());  
       }  
  
    @Override  
    public LoginType getLoginType() throws Exception{  
           LoginType lt=new LoginType();  
           lt.setId(1L);  
           try {  
               lt=baseDao.get(lt);  
               return lt;  
        } catch (Exception e) {  
            throw new ReturnException(null);  
        }  
       }  
    @Override  
    public boolean saveOrUpdate(LoginType loginType) throws Exception{  
           if(loginType==null||loginType.getPrimaryKeyValue()==null)  
               return false;  
           try {  
               baseDao.saveOrUpdate(loginType);  
               return true;  
        } catch (Exception e) {  
            throw new ReturnException(false);  
        }  
             
       }  
  
    @Override  
    public BaseEntityDaoI getBaseEntityDao() {  
        return baseDao;  
    }  
         
}
```
说明:
在显示层,一般是activity中;我们通过LoginTypeServiceI service=(LoginTypeServiceI)

 MySqliteTransactionProxy.getSqliteTransactionProxy(LoginTypeServiceI.class,new LoginTypeService());

方式来获取servic层;即用MySqliteTransactionProxy.getSqliteTransactionProxy(service接口类,service实现类)得到service接口的一个实现来获取service;

然后通过返回的接口示例来操作数据库;

若果想要在serive层方法执行的时候发生异常的时候返回一个值;可以使用throw new ReturnException(XXXX);

这样将会回滚当前的事务,并返回指定的值;

示例:
```java
public class UserLoginActivity extends BaseFragmentActivity {  
    private UserServiceI userService;</span>  
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.activity_userlogin);      
        userService=(UserServiceI) MySqliteTransactionProxy.getSqliteTransactionProxy(UserServiceI.class,new UserService());  
        initView();  
    }  
      
    public void initView(){  
        ......  
    }  
      
    public void user_login_click(View v){  
        switch (v.getId()) {  
        case R.id.submit_button_userlogin:  
            {  
                <span style="color:#ff0000;">userService.LoginFromServer(userName,password,webServiceCallBack);</span>  
            }  
            break;  
        case .......  
        default:  
            break;  
        }  
    }  
}
```
# BaseDaoI中方法一览
BaseDao中方法的实现是花了一点时间和精力的,有兴趣的童鞋可以去查看;没兴趣的可以直接使用;
```java
public interface BaseEntityDaoI {  
  
    public abstract SQLiteDatabase getDataBase();  
  
    /** 
     * 得到当前的最大的id号码的值 
     * @param db 
     * @return 
     */  
    public abstract <T extends BaseEntity> Long getMaxId(T t);  
  
    /** 
     * 得到当前的最小的id号码的值 
     * @param db 
     * @return 
     */  
    public abstract <T extends BaseEntity> Long getMinId(T t);  
  
    /** 
     * 得到当前的的记录总数 
     * @param db 
     * @return 
     */  
    public abstract <T extends BaseEntity> Long getCount(Class<T> cls);  
  
    /** 
     *得到除开指定名称的属性列  
     */  
    public abstract <T extends BaseEntity> String[] getEntityColumnNames(  
            Class<T> cls, Boolean isRepacePrimaryKeyName,  
            String... exceptCoulums);  
  
    /**失败返回null 
     *  传入代Id值的Entity的值实例 
     * @param t 返回t 
     * @return 
     * @throws Exception  
     */  
    public abstract <T extends BaseEntity> T get(Class<T> cls, String id)  
            throws Exception;  
  
    /**失败返回null 
     *  传入代Id值的Entity的值实例 
     * @param t 返回t 
     * @return 
     * @throws Exception  
     */  
    public abstract <T extends BaseEntity> T get(T t) throws Exception;  
  
    /**手动的条件搜索 
     * @return 
     * @throws Exception  
     */  
    public abstract <T extends BaseEntity> T get(Class<T> cls,  
            String[] columns, String selection, String[] selectionArgs,  
            String orderBy) throws Exception;  
  
    /**失败返回null 
     *  传入代Id值的Entity的值实例 
     * @param t 返回t 
     * @param exceptCoulums 不需要取出的数据列的名称 
     * @return 
     * @throws Exception  
     */  
    public abstract <T extends BaseEntity> T get(Class<T> cls, String id,  
            String... exceptCoulums) throws Exception;  
  
    /** 
     *  
     * 失败返回空数组 
     * @param db 
     * @param cls 
     * @param selection 
     * @param selectionArgs 
     * @param orderBy 
     * @param limit select * from table_name limit N,M //N序号从0开始 
     * @param exceptCoulums 指定不从数据库取出的列 
     * @return 
     * @throws Exception 
     */  
    public abstract <T extends BaseEntity> List<T> getList(Class<T> cls,  
            String selection, String[] selectionArgs, String orderBy,  
            String limit, String... exceptCoulums) throws Exception;  
  
    /** 
     * 失败返回空数组 
     * @param db 
     * @param cls 
     *@param selection 
     * @param selectionArgs 
     * @param orderBy 
     * @param limit select * from table_name limit N,M //N序号从0开始 
     * @return 
     * @throws Exception  
     */  
    public abstract <T extends BaseEntity> List<T> getList(Class<T> cls,  
            String selection, String[] selectionArgs, String orderBy,  
            String limit) throws Exception;  
  
    /** 
     * 获取数据库中的所有的记录 
     * @param db 
     * @param cls 
     * @return 
     * @throws Exception 
     */  
    public abstract <T extends BaseEntity> List<T> getList(Class<T> cls)  
            throws Exception;  
  
    public abstract <T extends BaseEntity> void saveOrUpdate(T t)  
            throws Exception;  
  
    /** 
     *  
     * @param t 
     * @return 插入返回1 
     * @param columnName 如果指定的字段,有相同的值存在于数据库,那么就更新数据库,否则保存 
     * @throws Exception 
     */  
    public abstract <T extends BaseEntity> void saveOrUpdate(T t,  
            String columnName) throws Exception;  
  
    /** 
     * 先删除,后保存,没有则不删除 
     * @param db 
     * @param t 
     * @throws Exception 
     */  
    public abstract <T extends BaseEntity> void deleteAndSave(T t)  
            throws Exception;  
  
    /** 
     *  
     * @param db 
     * @param list 
     * @return 
     * @throws Exception  
     */  
    public abstract <T extends BaseEntity> void saveOrUpdateList(List<T> list)  
            throws Exception;  
  
    /** 
     *  
     * @param db 
     * @param list 
     * @param column 指定列的值相同就更新,否则就保存 
     * @throws Exception 
     */  
    public abstract <T extends BaseEntity> void saveOrUpdateList(List<T> list,  
            String column) throws Exception;  
  
    /** 
     *删除后保存所有  
     * @param db 
     * @param list 
     * @return 
     * @throws Exception  
     */  
    public abstract <T extends BaseEntity> void deleteAndSaveList(List<T> list)  
            throws Exception;  
  
    public abstract <T extends BaseEntity> int update(T t) throws Exception;  
  
    /** 
     *  
     * @param t 
     * @param notUpdateColumns 不需要更新的字段名称的数组 
     * @return 
     * @throws Exception  
     */  
    public abstract <T extends BaseEntity> int update(T t,  
            String[] notUpdateColumns) throws Exception;  
  
    public abstract <T extends BaseEntity> int save(T t) throws Exception;  
  
    public abstract <T extends BaseEntity> int delete(Class<T> cls, String id)  
            throws Exception;  
  
    public abstract <T extends BaseEntity> int delete(T t) throws Exception;  
  
    public abstract <T extends BaseEntity> int deleteList(Class<T> cls,  
            String ids) throws Exception;  
  
    public abstract <T extends BaseEntity> int deleteList(List<T> ts)  
            throws Exception;  
  
    public abstract <T extends BaseEntity> int deleteAll(Class<T> cls)  
            throws Exception;  
  
    /** 
     *  
     * @param db 
     * @param sqlString 
     * @param selectionArgs sql中?占位符的参数 
     * @param columns 需要出去的列的名称,没有会赋值null;取出的列只支持float/string/blob/string/null这几种类型; 
     * *其中二进制会转换成为byte[]类型;除开这些类型外,系统会默认用string来取出数据 
     * @return List<Object[]> 
     */  
    public abstract <T extends BaseEntity> List<Object[]> getColumns(  
            String sqlString, String[] selectionArgs, String... columns);  
  
    /** 
     * 判断一个属性是否是静态变量,此类暂时不用 
     * @param field 
     */  
    public abstract boolean isStaticField(Field field);  
  
    //通过Cursor自动将值赋值到实体  
    public abstract <T extends BaseEntity> T initFromCursor(Cursor c,  
            Class<T> cls) throws IllegalAccessException,  
            IllegalArgumentException, InstantiationException;  
  
    public abstract <T extends BaseEntity> T initFromCursor(Cursor c, T t)  
            throws IllegalAccessException, IllegalArgumentException,  
            InstantiationException;  
  
    public abstract <T extends BaseEntity> ContentValues getContentValues(T t)  
            throws IllegalAccessException, IllegalArgumentException;  
  
    /** 
     * 返回该类属性的键值对,键和值均为String类型 
     * @return 
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     */  
    public abstract <T extends BaseEntity> Map<String, Object> getMapValues(T t)  
            throws IllegalAccessException, IllegalArgumentException;  
  
    public abstract <T extends BaseEntity> void saveToDataBase(T t)  
            throws Exception;  
  
    public abstract <T extends BaseEntity> void updateToDataBase(T t)  
            throws Exception;  
  
    /** 
     *  
     * @param tableName 
     * @param db 
     * @param columnName 指定此此表的一个列名称,更新所有相同的记录 
     * @throws Exception 
     */  
    public abstract <T extends BaseEntity> void updateToDataBaseByColumn(T t,  
            String columnName) throws Exception;  
  
    /** 
     *  
     * @param tableName 
     * @param data 
     * @param notUpdateColumns 不需要跟新的字段,区分大小写 
     * @throws Exception  
     */  
    public abstract <T extends BaseEntity> void updateToDataBase(T t,  
            String[] notUpdateColumns) throws Exception;  
  
}  
```
# 小结
1.本框架为非正式版,肯定有不少的bug,作者也没有全部测试,有bug的可以反馈留言;

2.此版本通过同步锁对数据库写操作进行阻塞,理论上并不好,实际高并发或者大量输入写入的不推荐使用;

3.下一个版本想要实现的功能:

(1)采用回调的方式来进行数据库的读写操作;

(2)采用一个表一个数据库的方式提高并发写操作;

4.文档可能不全,不过源码注释还是很多,建议看看源码即可

5.2.x版本和1.x版本主要是异步和同步的区别
