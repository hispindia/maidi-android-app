package org.hisp.dhis.android.sdk.persistence.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Table;
import org.hisp.dhis.android.sdk.persistence.Dhis2Database;

import java.util.List;

@Table(databaseName = Dhis2Database.NAME)
public class OrganUnit extends BaseMetaDataObject{

    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    @JsonProperty("children")
    List<OrganUnit> children;

    @Column(name = "children")
    String sChildren;

    @JsonProperty("level")
    @Column(name = "level")
    int level;

    @Override
    public void save() {
        sChildren = "";
        StringBuilder builder = new StringBuilder();
        if(children != null && children.size() > 0){
            for(OrganUnit child : children){
                builder.append(child.getId() + " ");
            }
            sChildren = builder.toString();
        }
        super.save();
    }

    public List<OrganUnit> getChildren() {
        return children;
    }

    public String getSChildren() {
        return sChildren;
    }

    public void setSChildren(String sChildren) {
        this.sChildren = sChildren;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}