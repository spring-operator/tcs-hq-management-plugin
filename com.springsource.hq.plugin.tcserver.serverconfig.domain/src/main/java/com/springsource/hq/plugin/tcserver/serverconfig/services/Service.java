/*
 * Copyright (C) 2009-2015  Pivotal Software, Inc
 *
 * This program is is free software; you can redistribute it and/or modify
 * it under the terms version 2 of the GNU General Public License as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.springsource.hq.plugin.tcserver.serverconfig.services;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.springsource.hq.plugin.tcserver.serverconfig.BindableAttributes;
import com.springsource.hq.plugin.tcserver.serverconfig.Hierarchical;
import com.springsource.hq.plugin.tcserver.serverconfig.Identity;
import com.springsource.hq.plugin.tcserver.serverconfig.Settings;
import com.springsource.hq.plugin.tcserver.serverconfig.ValidationUtils;
import com.springsource.hq.plugin.tcserver.serverconfig.services.connector.Connector;
import com.springsource.hq.plugin.tcserver.serverconfig.services.engine.Engine;

/**
 * Holder for service settings. (http://tomcat.apache.org/tomcat-6.0-doc/config/service.html)
 * 
 * @since 2.0
 */
@BindableAttributes
@XmlType(name = "service", propOrder = { "connectors", "engine" })
public class Service implements Validator, Hierarchical<Settings>, Identity {

    /**
     * The display name of this Service, which will be included in log messages if you utilize standard Catalina
     * components. The name of each Service that is associated with a particular Server must be unique.
     */
    private String name;

    private Engine engine;

    private Set<Connector> connectors;

    private String id;

    private Settings parent;

    public Service() {
        engine = new Engine();
        connectors = new HashSet<Connector>();
    }

    @XmlAttribute(name = "name", required = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(name = "engine", required = true)
    public Engine getEngine() {
        return engine;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    @XmlElementWrapper(name = "connectors", required = true)
    @XmlElement(name = "connector")
    public Set<Connector> getConnectors() {
        return connectors;
    }

    public void setConnectors(Set<Connector> connectors) {
        this.connectors = connectors;
    }

    @XmlTransient
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean supports(Class<?> clazz) {
        return this.getClass().equals(clazz);
    }

    public void validate(Object target, Errors errors) {
        Service service = (Service) target;
        if (!errors.hasFieldErrors("name")) {
            if (!StringUtils.hasText(service.getName())) {
                errors.rejectValue("name", "service.name.required");
            } else {
                // detect duplicate service names
                if (service.parent() != null) {
                    for (Service s : service.parent().getServices()) {
                        if (s != service && ObjectUtils.nullSafeEquals(service.getName(), s.getName())) {
                            errors.reject("service.name.unique", new Object[] { service.getName() }, null);
                        }
                    }
                }
            }
        }

        ValidationUtils.validateCollection(service.getConnectors(), "connectors", errors);

        errors.pushNestedPath("engine");
        service.getEngine().validate(service.getEngine(), errors);
        errors.popNestedPath();
    }

    public void applyParentToChildren() {
        engine.setParent(this);
        engine.applyParentToChildren();
        for (Connector connector : connectors) {
            connector.setParent(this);
            connector.applyParentToChildren();
        }
    }

    public Settings parent() {
        return parent;
    }

    public void setParent(Settings parent) {
        this.parent = parent;
    }

    @XmlTransient
    public String getHumanId() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Service)) {
            return false;
        }
        Service service = (Service) obj;
        return ObjectUtils.nullSafeEquals(this.getConnectors(), service.getConnectors())
            && ObjectUtils.nullSafeEquals(this.getEngine(), service.getEngine()) && ObjectUtils.nullSafeEquals(this.getName(), service.getName());
    }

    @Override
    public int hashCode() {
        return ObjectUtils.nullSafeHashCode(this.connectors) * 29 + ObjectUtils.nullSafeHashCode(this.engine) * 29
            + ObjectUtils.nullSafeHashCode(this.name) * 29;
    }

}
