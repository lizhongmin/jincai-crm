package com.jincai.crm.system.dto;

import java.util.ArrayList;
import java.util.List;

public record DepartmentTreeView(
    Long id,
    String name,
    Long parentId,
    String parentName,
    String treePath,
    Long leaderUserId,
    String leaderName,
    Boolean hasUsers,
    Boolean hasChildren,
    List<DepartmentTreeView> children
) {
    public DepartmentTreeView(Long id, String name, Long parentId, String parentName, String treePath,
                              Long leaderUserId, String leaderName, Boolean hasUsers, Boolean hasChildren) {
        this(id, name, parentId, parentName, treePath, leaderUserId, leaderName, hasUsers, hasChildren, new ArrayList<>());
    }
}

