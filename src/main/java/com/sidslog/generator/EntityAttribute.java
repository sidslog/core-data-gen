package com.sidslog.generator;

/**
 * Created by sidslog on 15.04.14.
 */
enum EntityAttributeType {
    EA_STRING,
    EA_DATE,
    EA_BOOLEAN,
    EA_REF,
}

public class EntityAttribute {

    private String name;
    private EntityAttributeType type;
    private EntityClass ref;

    public EntityAttribute(String name, EntityAttributeType type, EntityClass ref) {
        this.name = name;
        this.type = type;
        this.ref = ref;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EntityAttributeType getType() {
        return type;
    }

    public void setType(EntityAttributeType type) {
        this.type = type;
    }

    public EntityClass getRef() {
        return ref;
    }

    public void setRef(EntityClass ref) {
        this.ref = ref;
    }

}
