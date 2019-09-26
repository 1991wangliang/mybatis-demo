# mybatis-demo

在每一张表下增加固定字段，在操作数据的时候通过权限识别用户然后维护字段信息。

数据库表下增加的字段如下:

```
    /**
     * 创建者
     */

    private String createMan;

    /**
     * 数据库状态
     * 0 正常
     * 1 删除
     */
    private int state;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 最后更新者
     */
    private String lastUpdateMan;

    /**
     * 最后更新时间
     */
    private Date lastUpdateTime;
```

演示说明:

```

    @Autowired
    private TestMapper testMapper;


    @Before
    public void init(){
        //模拟切面先将用户信息传递到ThreadLocal下
        MapperThreadUserInfo mapperThreadUserInfo = new MapperThreadUserInfo();
        mapperThreadUserInfo.setUser("xiao li");
        MapperThreadUserInfo.set(mapperThreadUserInfo);
    }

    @Test
    public void update() {
        //执行业务
        com.example.mybatis.demo.domain.Test test = new com.example.mybatis.demo.domain.Test();
        test.setName("new Name 1");
        test.setId(1L);
        int rs = testMapper.updateName(test);
        log.info("rs->{}",rs);
        Assert.assertTrue(rs==1);
        //查询数据
        List<com.example.mybatis.demo.domain.Test> list =  testMapper.findAll();
        log.info("list->{}",list);
        Assert.assertTrue(list.size()>0);
        
        
    }
    
```
update 更新以后可以查询到维护者的信息. 详情见源码
