package com.sidslog.generator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sidslog on 15.04.14.
 */
public class EntityClass {

    private String name;
    private EntityClass parent;
    private int index;
    private List<EntityAttribute> attributes;
    private List<EntityClass> children;

    public EntityClass(String name, EntityClass parent) {
        this.name = name;
        this.parent = parent;
        if (parent != null) {
            parent.getChildren().add(this);
        }
        this.attributes = new ArrayList<EntityAttribute>();
        this.children = new ArrayList<EntityClass>();
        this.index = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EntityClass getParent() {
        return parent;
    }

    public void setParent(EntityClass parent) {
        this.parent = parent;
        parent.getChildren().add(this);
    }

    public List<EntityAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<EntityAttribute> attributes) {
        this.attributes = attributes;
    }

    public List<EntityClass> getChildren() {
        return children;
    }

    public void setChildren(List<EntityClass> children) {
        this.children = children;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
