# mybatis-demo

在每一张表下增加固定字段，在操作数据的还是通过框架维护这些信息。

字段如下:

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
        com.example.mybatis.demo.domain.Test test = new com.example.mybatis.demo.domain.Test();
        test.setName("new Name 1");
        test.setId(1L);
        int rs = testMapper.updateName(test);
        log.info("rs->{}",rs);
        Assert.assertTrue(rs==1);

        List<com.example.mybatis.demo.domain.Test> list =  testMapper.findAll();
        log.info("list->{}",list);
        Assert.assertTrue(list.size()>0);
    }
    
```

update 更新以后也会带上修改者的信息.
