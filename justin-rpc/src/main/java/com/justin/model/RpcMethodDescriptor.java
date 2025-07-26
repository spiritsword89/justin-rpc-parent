package com.justin.model;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class RpcMethodDescriptor {
    private String methodId;
    private String className;
    private Integer numOfParams;
    private String methodName;
    private List<String> parameterTypes;
    private boolean hasReturnValue;
    private String returnValueType; // full class name

    public static String generateMethodId(String methodName, int paramsCount, String[] paramTypeSimpleNames, String returnValueTypeSimpleName) {
        // id = method name + "." + param counts + "." + param type name + "." + return type name
        String id = String.join(".", methodName, String.valueOf(paramsCount));

        for (String paramTypeSimpleName: paramTypeSimpleNames) {
            id = String.join(".", id,  paramTypeSimpleName);
        }

        if(returnValueTypeSimpleName != null) {
            id = String.join(".", id, returnValueTypeSimpleName);
        }

        return id;
    }

    public static RpcMethodDescriptor build(Method method) {
        RpcMethodDescriptor md = new RpcMethodDescriptor();
        Class<?>[] paramTypes = method.getParameterTypes();
        md.setClassName(method.getClass().getName());
        md.setNumOfParams(method.getParameterCount());
        md.setMethodName(method.getName());

        // id = method name + "." + param counts + "." + param type name + "." + return type name
        String id = String.join(".", method.getName(), String.valueOf(method.getParameterCount()));
        List<String> parameterTypeList = new ArrayList();

        for(Class<?> paramType : paramTypes){
            parameterTypeList.add(paramType.getSimpleName());
            id = String.join(".", id, paramType.getSimpleName());
        }

        // id = method name + "." + param counts + "." + param type simple name

        md.setHasReturnValue(false);
        if(!method.getReturnType().equals(Void.class)) {
            md.setHasReturnValue(true);
            id =  String.join(".", id, method.getReturnType().getSimpleName());
            md.setReturnValueType(method.getReturnType().getName());
        }

        md.setMethodId(id);

        md.setParameterTypes(parameterTypeList);

        return md;
    }


    public String getMethodId() {
        return methodId;
    }

    public void setMethodId(String methodId) {
        this.methodId = methodId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Integer getNumOfParams() {
        return numOfParams;
    }

    public void setNumOfParams(Integer numOfParams) {
        this.numOfParams = numOfParams;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public List<String> getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(List<String> parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public boolean isHasReturnValue() {
        return hasReturnValue;
    }

    public void setHasReturnValue(boolean hasReturnValue) {
        this.hasReturnValue = hasReturnValue;
    }

    public String getReturnValueType() {
        return returnValueType;
    }

    public void setReturnValueType(String returnValueType) {
        this.returnValueType = returnValueType;
    }
}
