package com.easyjava.builder;

import com.easyjava.bean.Constants;
import com.easyjava.bean.FieldInfo;
import com.easyjava.bean.TableInfo;
import com.easyjava.utils.DateUtils;
import com.easyjava.utils.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.Map;

public class BuildMapper {
    private static final Logger logger = LoggerFactory.getLogger(BuildMapper.class);

    public static void execute(TableInfo tableInfo) {
        File folder = new File(Constants.PATH_MAPPERS);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String className = tableInfo.getBeanName() + Constants.SUFFIX_MAPPERS;
        File poFile = new File(folder, className + ".java");

        OutputStream out = null;
        OutputStreamWriter outw = null;
        BufferedWriter bw = null;
        try {
            out = new FileOutputStream(poFile);
            outw = new OutputStreamWriter(out, "utf8");
            bw = new BufferedWriter(outw);

            bw.write("package " + Constants.PACKAGE_MAPPERS + ";");
            bw.newLine();

            bw.newLine();
            bw.write("import org.apache.ibatis.annotations.Param;");
            bw.newLine();

            bw.newLine();

            //构建类注释
            BuildComment.createClassComment(bw, tableInfo.getComment() + "Mapper");
            bw.write("public interface " + className + "<T, P> extends BaseMapper {");
            bw.newLine();

            Map<String, List<FieldInfo>> keyIndexMap = tableInfo.getKeyIndexMap();
            for (Map.Entry<String, List<FieldInfo>> entry : keyIndexMap.entrySet()) {
                List<FieldInfo> keyFieldInfoList = entry.getValue();
                Integer index = 0;

                StringBuilder methodName = new StringBuilder();

                StringBuilder methodParams = new StringBuilder();
                for (FieldInfo fieldInfo : keyFieldInfoList) {
                    index++;
                    methodName.append(StringUtils.uperCaseFirstLetter(fieldInfo.getPropertyName()));
                    if (index < keyFieldInfoList.size()) {
                        methodName.append("And");
                    }
                    //方法参数构建
                    methodParams.append("@Param(\"" + fieldInfo.getPropertyName() + "\") " + fieldInfo.getJavaType() + " " + fieldInfo.getPropertyName());
                    if (index < keyFieldInfoList.size()) {
                        methodParams.append(", ");
                    }
                }
                //查询
                bw.newLine();
                BuildComment.createFieldComment(bw, "根据" + methodName + "查询");
                bw.write("\tT selectBy" + methodName + "(" + methodParams + ");");
                bw.newLine();

                //更新
                bw.newLine();
                BuildComment.createFieldComment(bw, "根据" + methodName + "更新");
                bw.write("\tT updateBy" + methodName + "(@Param(\"bean\") T t, " + methodParams + ");");
                bw.newLine();

                //删除
                bw.newLine();
                BuildComment.createFieldComment(bw, "根据" + methodName + "删除");
                bw.write("\tT deleteBy" + methodName + "(" + methodParams + ");");
                bw.newLine();
            }

            bw.newLine();
            bw.write("}");
            bw.flush();
        } catch (Exception e) {
            logger.error("创建mappers失败", e);
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
