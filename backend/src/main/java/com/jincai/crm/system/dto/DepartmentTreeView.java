package com.jincai.crm.system.dto;

import java.util.ArrayList;
import java.util.List;

public record DepartmentTreeView(
    String id,
    String name,
    String parentId,
    String parentName,
    String treePath,
    String leaderUserId,
    String leaderName,
    Boolean hasUsers,
    Boolean hasChildren,
    Integer sort,
    List<DepartmentTreeView> children
) {
    public DepartmentTreeView(String id, String name, String parentId, String parentName, String treePath,
                              String leaderUserId, String leaderName, Boolean hasUsers, Boolean hasChildren, Integer sort) {
        this(id, name, parentId, parentName, treePath, leaderUserId, leaderName, hasUsers, hasChildren, sort, new ArrayList<>());
    }
}

