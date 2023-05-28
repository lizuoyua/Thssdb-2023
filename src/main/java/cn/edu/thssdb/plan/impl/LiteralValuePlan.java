package cn.edu.thssdb.plan.impl;

import cn.edu.thssdb.plan.LogicalPlan;

public class LiteralValuePlan extends LogicalPlan {
    public enum LiteralType {
        INT_OR_LONG,
        FLOAT_OR_DOUBLE,
        STRING,
        NULL
    }

    private LiteralType literalType;
    private String string;

    public LiteralValuePlan(LiteralType type, String string){
        super(LogicalPlanType.LIT_VAL);
        this.literalType = type;
        this.string = string;
    }

    public LiteralType getLiteralType(){
        return literalType;
    }

    public String getString(){
        return string;
    }
}
