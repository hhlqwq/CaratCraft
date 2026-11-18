package com.platform.modules.sys.service;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

/** 表示一个业务动作可覆盖的机构, 空范围默认无权. */
public record OrgActionScope(boolean emergency, List<String> organizations) {
    /** 固定建造边界, 防止调用方在授权后修改机构集合. */
    public OrgActionScope {
        organizations = List.copyOf(organizations);
    }

    /** 核对某条业务记录或人员的归属. */
    public boolean permits(String organization) {
        return emergency || (organization != null && organizations.contains(organization));
    }

    /** 仅使用代码选定的列名生成带参数的过滤条件. */
    public String andWhere(String column, List<Object> arguments) {
        if (emergency) {
            return "";
        }
        if (organizations.isEmpty()) {
            return " AND 1=0";
        }
        arguments.addAll(organizations);
        return " AND "
                + column
                + " IN ("
                + String.join(",", Collections.nCopies(organizations.size(), "?"))
                + ")";
    }

    /** 使用MyBatis Wrapper占位参数嵌入固定归属列的查询条件. */
    public String mybatisCondition(String column) {
        if (organizations.isEmpty()) {
            return "1=0";
        }
        return column
                + " IN ("
                + IntStream.range(0, organizations.size())
                        .mapToObj(index -> "{" + index + "}")
                        .collect(java.util.stream.Collectors.joining(","))
                + ")";
    }

    /** 工作台行动待办须同时在对应入口的读取和执行范围内. */
    public OrgActionScope intersect(OrgActionScope other) {
        if (emergency) {
            return other;
        }
        if (other.emergency) {
            return this;
        }
        return new OrgActionScope(
                false, organizations.stream().filter(other.organizations::contains).toList());
    }
}
