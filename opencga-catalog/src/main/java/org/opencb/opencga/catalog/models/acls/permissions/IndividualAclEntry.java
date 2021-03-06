/*
 * Copyright 2015-2016 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opencb.opencga.catalog.models.acls.permissions;

import org.opencb.commons.datastore.core.ObjectMap;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by pfurio on 11/05/16.
 */
public class IndividualAclEntry extends AbstractAclEntry<IndividualAclEntry.IndividualPermissions> {

    public enum IndividualPermissions {
        VIEW,
        UPDATE,
        DELETE,
        SHARE,
        WRITE_ANNOTATIONS,
        VIEW_ANNOTATIONS,
        DELETE_ANNOTATIONS
    }

    public IndividualAclEntry() {
        this("", Collections.emptyList());
    }

    public IndividualAclEntry(String member, EnumSet<IndividualPermissions> permissions) {
        super(member, permissions);
    }

    public IndividualAclEntry(String member, ObjectMap permissions) {
        super(member, EnumSet.noneOf(IndividualPermissions.class));

        EnumSet<IndividualPermissions> aux = EnumSet.allOf(IndividualPermissions.class);
        for (IndividualPermissions permission : aux) {
            if (permissions.containsKey(permission.name()) && permissions.getBoolean(permission.name())) {
                this.permissions.add(permission);
            }
        }
    }

    public IndividualAclEntry(String member, List<String> permissions) {
        super(member, EnumSet.noneOf(IndividualPermissions.class));
        if (permissions.size() > 0) {
            this.permissions.addAll(permissions.stream().map(IndividualPermissions::valueOf).collect(Collectors.toList()));
        }
    }

}
