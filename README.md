# easy-elasticsearch
Easy Elasticsearch Guide

1.Easy Elasticsearch 使用

    1.1 clone 下载项目
    
    1.2 项目引用（两种方式）
        1.2.1：将clone 的项目打成jar 包引用
        1.2.2：pom.xml 新增配置
           example：`
            <distributionManagement>
               <repository>
                 <id>com.palmple.maven.central</id>
                 <name>Palmple Maven Central Repository</name>
                 <url>http://sonatype-nexus.palmple.com:8081/nexus/content/repositories/com.palmple.maven.central</url>
               </repository>
            </distributionManagement>`
          将项目打包至私服或者本地，然后进行引用
          example：`
              <dependency>
                 <groupId>com.easy.elasticsearch</groupId>
                 <artifactId>easy</artifactId>
                 <version>{version}</version>
              </dependency>`
    1.3 项目使用
        注入 EasyElasticsearchTemplate 类
          example ：`
               @Autowired
               EasyElasticsearchTemplate easyElasticsearchTemplate;`
        查询使用 query()方法
           EasySearchBody 查询参数类
        example: `
        EasySearchBody easySearchBody = new EasySearchBody();
        easySearchBody.setIndex("test");// ES 索引
        easySearchBody.setType("test");//ES 索引类型
        List<String> fields = new ArrayList<>();//搜索字段 List 可进行多个字段匹配
        fields.add("field_1");//匹配字段 1
        fields.add("field_2");//匹配字段 2
        easySearchBody.setSearchTargetField(fields);
        easySearchBody.setSearchValue("test");//搜索值
        easySearchBody.setPage(1);//页码
        easySearchBody.setSize(10);//每页显示条目
        Result<EasyDemo> result = easyElasticsearchTemplate.query(easySearchBody,EasyDemo.class);`
