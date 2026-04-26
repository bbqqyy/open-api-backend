package com.bqy.openapibackend.generator;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;

import java.nio.file.Paths;
import java.sql.Types;

public class CodeGenerator {
    //todo 将各个信息转化为application管理
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/open_api";
        String username = "root";
        String password = "922428";
        System.out.println(System.getProperty("user.dir"));
        FastAutoGenerator.create(url, username, password)
                .globalConfig(builder -> builder
                        .author("bianqingyun")
                        .outputDir(Paths.get(System.getProperty("user.dir")) + "/src/main/java")
                        .enableSwagger()
                        .commentDate("yyyy-MM-dd")
                )
                .dataSourceConfig(builder ->
                        builder.typeConvertHandler((globalConfig, typeRegistry, metaInfo) -> {
                            int typeCode = metaInfo.getJdbcType().TYPE_CODE;
                            if (typeCode == Types.SMALLINT) {
                                // 自定义类型转换
                                return DbColumnType.INTEGER;
                            }
                            return typeRegistry.getColumnType(metaInfo);
                        })
                )
                .packageConfig(builder -> builder
                        .parent("com.bqy.openapibackend")
                        .entity("entity")
                        .mapper("mapper")
                        .service("service")
                        .serviceImpl("service.impl")
                        .xml("mapper.xml")
                )
                .strategyConfig(builder -> builder
                        .entityBuilder()
                        .enableLombok()
                )
                .templateEngine(new VelocityTemplateEngine())
                .execute();
    }
}
