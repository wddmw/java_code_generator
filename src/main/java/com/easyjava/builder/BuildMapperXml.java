package com.easyjava.builder;

import com.easyjava.bean.Constants;
import com.easyjava.bean.FieldInfo;
import com.easyjava.bean.TableInfo;
import com.easyjava.utils.StringUtils;
import com.sun.org.apache.bcel.internal.Const;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.Map;

public class BuildMapperXml {

    private static final Logger logger = LoggerFactory.getLogger(BuildMapperXml.class);

    private static final String BASE_COLUMN_LIST = "base_column_list";

    private static final String BASE_QUERY_CONDITION = "base_query_condition";

    private static final String BASE_QUERY_CONDITION_EXTEND = "base_query_condition_extend";

    private static final String QUERY_CONDITION = "query_condition";

    public static void execute(TableInfo tableInfo) {
        File folder = new File(Constants.PATH_MAPPERS_XMLS);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String className = tableInfo.getBeanName() + Constants.SUFFIX_MAPPERS;

        File poFile = new File(folder, className + ".xml");

        OutputStream out = null;
        OutputStreamWriter outw = null;
        BufferedWriter bw = null;
        try {
            out = new FileOutputStream(poFile);
            outw = new OutputStreamWriter(out, "utf8");
            bw = new BufferedWriter(outw);

            bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            bw.newLine();
            bw.write("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\"");
            bw.newLine();
            bw.write("\t\t\"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">");
            bw.newLine();
            bw.write("<mapper namespace=\"" + Constants.PACKAGE_MAPPERS + "." + className + "\">");
            bw.newLine();

            //resultMap
            bw.write("\t<!--实体映射-->");
            bw.newLine();
            String poClass = Constants.PACKAGE_PO + "." + tableInfo.getBeanName();
            bw.write("\t<resultMap id=\"base_result_map\" type=\"" + poClass + "\">");
            bw.newLine();
            FieldInfo idField = null;
            Map<String, List<FieldInfo>> keyIndexMap = tableInfo.getKeyIndexMap();
            for (Map.Entry<String, List<FieldInfo>> entry : keyIndexMap.entrySet()) {
                if ("PRIMARY".equals(entry.getKey())) {
                    List<FieldInfo> fieldInfoList = entry.getValue();
                    if (fieldInfoList.size() == 1) {
                        idField = fieldInfoList.get(0);
                        break;
                    }
                }
            }
            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                bw.write("\t\t<!--" + fieldInfo.getComment() + "-->");
                bw.newLine();
                String key = "";
                //如果是单个主键的话就用id，多个主键的话就全部用result
                if (idField != null && fieldInfo.getPropertyName().equals(idField.getPropertyName())) {
                    key = "id";
                } else {
                    key = "result";
                }
                bw.write("\t\t<" + key + " column=\"" + fieldInfo.getFieldName() + "\" property=\"" + fieldInfo.getPropertyName() + "\"/>");
                bw.newLine();
            }
            bw.write("\t</resultMap>");
            bw.newLine();

            //通用查询结果列
            bw.newLine();
            bw.write("\t<!--通用查询结果列-->");
            bw.newLine();
            bw.write("\t<sql id=\"" + BASE_COLUMN_LIST + "\">");
            bw.newLine();
            StringBuilder columnBuilder = new StringBuilder();
            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                columnBuilder.append(fieldInfo.getFieldName()).append(",");
            }
            String columnBuilderStr = columnBuilder.substring(0, columnBuilder.lastIndexOf(","));
            bw.write("\t\t" + columnBuilderStr);
            bw.newLine();
            bw.write("\t</sql>");
            bw.newLine();

            //基础查询条件
            bw.newLine();
            bw.write("\t<!--基础查询条件-->");
            bw.newLine();
            bw.write("\t<sql id=\"" + BASE_QUERY_CONDITION + "\">");
            bw.newLine();
            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                String stringQuery = "";
                if (ArrayUtils.contains(Constants.SQL_STRING_TYPES, fieldInfo.getSqlType())) {
                    stringQuery = " and query." + fieldInfo.getPropertyName() + " != ''";
                }

                bw.write("\t\t<if test=\"query." + fieldInfo.getPropertyName() + " != null" + stringQuery + "\">");
                bw.newLine();
                bw.write("\t\t\tand id = #{query." + fieldInfo.getPropertyName() + "}");
                bw.newLine();
                bw.write("\t\t</if>");
                bw.newLine();
            }
            bw.write("\t</sql>");
            bw.newLine();


            //扩展的查询条件
            bw.newLine();
            bw.write("\t<!--扩展的查询条件-->");
            bw.newLine();
            bw.write("\t<sql id=\"" + BASE_QUERY_CONDITION_EXTEND + "\">");
            bw.newLine();
            for (FieldInfo fieldInfo : tableInfo.getFieldExtendList()) {
                String andWhere = "";
                if (ArrayUtils.contains(Constants.SQL_STRING_TYPES, fieldInfo.getSqlType())) {
                    andWhere = "and "+ fieldInfo.getFieldName() +" like concat('%',#(query."+ fieldInfo.getPropertyName() +"), '%') ";
                } else if (ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, fieldInfo.getSqlType()) ||
                        ArrayUtils.contains(Constants.SQL_DATE_TYPES, fieldInfo.getSqlType())) {
                    if(fieldInfo.getPropertyName().endsWith(Constants.SUFFIX_BEAN_QUERY_TIME_START)){
                        andWhere = "<![CDATA[ and " + fieldInfo.getFieldName() + " >= str_to_date(#{" + fieldInfo.getPropertyName() + "}, '%Y-%m-%d') ]]>";
                    }else if(fieldInfo.getPropertyName().endsWith(Constants.SUFFIX_BEAN_QUERY_TIME_END)){
                        andWhere = "<![CDATA[ and " + fieldInfo.getFieldName() + " < date_sub(str_to_date(#{query." + fieldInfo.getPropertyName() + "}, '%Y-%m-%d')," +
                                "interval - 1 day) ]]>";
                    }
                }
                bw.write("\t\t<if test=\"query." + fieldInfo.getPropertyName() + " != null and query." + fieldInfo.getPropertyName() + " !=''\">");
                bw.newLine();
                bw.write("\t\t\t" + andWhere);
                bw.newLine();
                bw.write("\t\t</if>");
                bw.newLine();
            }
            bw.write("\t</sql>");
            bw.newLine();


            //通用查询条件
            bw.newLine();
            bw.write("\t<!--通用查询条件-->");
            bw.newLine();
            bw.write("\t<sql id=\"" + QUERY_CONDITION + "\">");
            bw.newLine();
            bw.write("\t\t<where>");
            bw.newLine();
            bw.write("\t\t\t<include refid=\""+ BASE_QUERY_CONDITION +"\"/>");
            bw.newLine();
            bw.write("\t\t\t<include refid=\""+ BASE_QUERY_CONDITION_EXTEND +"\"/>");
            bw.newLine();
            bw.write("\t\t</where>");
            bw.newLine();
            bw.write("\t</sql>");
            bw.newLine();


            bw.write("</mapper>");


            bw.flush();
        } catch (Exception e) {
            logger.error("创建mappers xml失败", e);
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outw != null) {
                try {
                    outw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
