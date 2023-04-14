package com.easyjava.utils;


public class StringUtils {
    /**
     * 将字符串的第一个字母转换为大写
     * @param field
     * @return
     */
    public static String uperCaseFirstLetter(String field){
        if(org.apache.commons.lang3.StringUtils.isEmpty(field)){
            return field;
        }
        return field.substring(0,1).toUpperCase() + field.substring(1);
    }

    /**
     * 将字符串的第一个字母转换为小写
     * @param field
     * @return
     */
    public static String lowerCaseFirstLetter(String field){
        if(org.apache.commons.lang3.StringUtils.isEmpty(field)){
            return field;
        }
        return field.substring(0,1).toLowerCase() + field.substring(1);
    }

}
