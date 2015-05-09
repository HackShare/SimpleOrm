package com.anli.simpleorm.queries.mysql;

import com.anli.simpleorm.definitions.CollectionDefinition;
import com.anli.simpleorm.definitions.EntityDefinition;
import com.anli.simpleorm.definitions.FieldDefinition;
import com.anli.simpleorm.definitions.ListDefinition;
import com.anli.simpleorm.definitions.ReferenceDefinition;
import com.anli.simpleorm.queries.QueryBuilder;
import com.anli.simpleorm.queries.QueryDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MysSqlQueryBuilderTest {

    protected final QueryBuilder queryBuilder = new MySqlQueryBuilder();

    @Test
    public void testConcreteSelectQuery() {
        EntityDefinition definition = getConcreteADefinition();
        QueryDescriptor query = queryBuilder.buildSelectEntityQuery(definition);
        Map<String, String> columnFields = new HashMap<>();
        columnFields.put("id", "concretea.concrete_a_id as concretea_id");
        columnFields.put("time", "concretea.time_field as concretea_time");
        columnFields.put("atomic", "concretea.atomic_ref as concretea_atomic");
        List<String> columns = new ArrayList<>(3);
        for (FieldDefinition field : definition.getSingleFields()) {
            columns.add(columnFields.get(field.getName()));
        }
        String fieldList = StringUtils.join(columns.toArray(), ", ");
        String etalonQuery = "select distinct " + fieldList
                + " from concretes_a as concretea where concretea.concrete_a_id = ?";
        assertEquals(etalonQuery, query.getQuery());
        assertEquals(1, query.getParameterBinding("ConcreteA.id"));
        assertEquals("concretea_id", query.getResultBinding("ConcreteA.id"));
        assertEquals("concretea_time", query.getResultBinding("ConcreteA.time"));
        assertEquals("concretea_atomic", query.getResultBinding("ConcreteA.atomic"));
    }

    @Test
    public void testConcreteSelectByKeysQuery() {
        EntityDefinition definition = getConcreteBDefinition();
        QueryDescriptor query = queryBuilder.buildSelectEntitiesByKeysQuery(definition);
        Map<String, String> columnFields = new HashMap<>();
        columnFields.put("id", "concreteb.concrete_b_id as concreteb_id");
        columnFields.put("name", "concreteb.name_field as concreteb_name");
        columnFields.put("atomic", "concreteb.atomic_ref as concreteb_atomic");
        List<String> columns = new ArrayList<>(3);
        for (FieldDefinition field : definition.getSingleFields()) {
            columns.add(columnFields.get(field.getName()));
        }
        String fieldList = StringUtils.join(columns.toArray(), ", ");
        String etalonQuery = "select distinct " + fieldList
                + " from concretes_b as concreteb where concreteb.concrete_b_id in (${list})";
        assertEquals(etalonQuery, query.getQuery());
        assertEquals(1, query.getParameterBinding("ConcreteB.id"));
        assertEquals("concreteb_id", query.getResultBinding("ConcreteB.id"));
        assertEquals("concreteb_name", query.getResultBinding("ConcreteB.name"));
        assertEquals("concreteb_atomic", query.getResultBinding("ConcreteB.atomic"));
    }

    @Test
    public void testConcreteInsertFullQuery() {
        EntityDefinition definition = getConcreteADefinition();
        List<QueryDescriptor> queries = queryBuilder.buildInsertFullEntityQueries(definition);
        assertEquals(1, queries.size());
        QueryDescriptor query = queries.iterator().next();
        Map<String, String> columnFields = new HashMap<>();
        columnFields.put("id", "concrete_a_id");
        columnFields.put("time", "time_field");
        columnFields.put("atomic", "atomic_ref");
        List<String> columns = new ArrayList<>(3);
        for (FieldDefinition field : definition.getSingleFields()) {
            columns.add(columnFields.get(field.getName()));
        }
        String fieldList = StringUtils.join(columns.toArray(), ", ");
        String etalonQuery = "insert into concretes_a (" + fieldList
                + ") values (?, ?, ?)";
        assertEquals(etalonQuery, query.getQuery());
        assertEquals(columns.indexOf("concrete_a_id") + 1, query.getParameterBinding("ConcreteA.id"));
        assertEquals(columns.indexOf("time_field") + 1, query.getParameterBinding("ConcreteA.time"));
        assertEquals(columns.indexOf("atomic_ref") + 1, query.getParameterBinding("ConcreteA.atomic"));
    }

    @Test
    public void testConcreteInsertAnemicQuery() {
        EntityDefinition definition = getConcreteBDefinition();
        List<QueryDescriptor> queries = queryBuilder.buildInsertAnemicEntityQueries(definition);
        assertEquals(1, queries.size());
        QueryDescriptor query = queries.iterator().next();
        String etalonQuery = "insert into concretes_b (concrete_b_id)"
                + " values (?)";
        assertEquals(etalonQuery, query.getQuery());
        assertEquals(1, query.getParameterBinding("ConcreteB.id"));
    }

    @Test
    public void testConcreteUpdateQuery() {
        EntityDefinition definition = getConcreteADefinition();
        QueryDescriptor query = queryBuilder.buildUpdateEntityQuery(definition);
        Map<String, String> columnFields = new HashMap<>();
        columnFields.put("time", "concretea.time_field");
        columnFields.put("atomic", "concretea.atomic_ref");
        List<String> columns = new ArrayList<>(2);
        for (FieldDefinition field : definition.getSingleFields()) {
            String column = columnFields.get(field.getName());
            if (column != null) {
                columns.add(column);
            }
        }
        String fieldList = StringUtils.join(columns.toArray(), " = ?, ") + " = ?";
        String etalonQuery = "update concretes_a as concretea set " + fieldList
                + " where concretea.concrete_a_id = ?";
        assertEquals(etalonQuery, query.getQuery());
        assertEquals(columns.indexOf("concretea.time_field") + 1,
                query.getParameterBinding("ConcreteA.time"));
        assertEquals(columns.indexOf("concretea.atomic_ref") + 1,
                query.getParameterBinding("ConcreteA.atomic"));
        assertEquals(3, query.getParameterBinding("ConcreteA.id"));
    }

    @Test
    public void testConcreteDeleteQuery() {
        EntityDefinition definition = getConcreteBDefinition();
        QueryDescriptor query = queryBuilder.buildDeleteEntityQuery(definition);
        String etalonQuery = "delete concreteb from concretes_b as concreteb "
                + "where concreteb.concrete_b_id = ?";
        assertEquals(etalonQuery, query.getQuery());
        assertEquals(1, query.getParameterBinding("ConcreteB.id"));
    }

    @Test
    public void testSelectCollectionQuery() {
        EntityDefinition definition = getConcreteADefinition();
        CollectionDefinition field = definition.getCollectionField("atomicSet");
        QueryDescriptor query = queryBuilder.buildSelectCollectionKeysQuery(field);
        String etalonQuery = "select distinct atomic.atomic_id as atomic_id"
                + " from atomics as atomic where atomic.a_ref = ?";
        assertEquals(etalonQuery, query.getQuery());
        assertEquals("atomic_id", query.getResultBinding("Atomic.id"));
        assertEquals(1, query.getParameterBinding("foreignKey"));
    }

    @Test
    public void testSelectListQuery() {
        EntityDefinition definition = getConcreteBDefinition();
        CollectionDefinition field = definition.getCollectionField("atomicList");
        QueryDescriptor query = queryBuilder.buildSelectCollectionKeysQuery(field);
        String etalonQuery = "select distinct atomic.atomic_id as atomic_id"
                + " from atomics as atomic where atomic.b_ref = ? order by atomic.b_order";
        assertEquals(etalonQuery, query.getQuery());
        assertEquals("atomic_id", query.getResultBinding("Atomic.id"));
        assertEquals(1, query.getParameterBinding("foreignKey"));
    }

    @Test
    public void testLinkCollectionQuery() {
        EntityDefinition definition = getConcreteADefinition();
        CollectionDefinition field = definition.getCollectionField("atomicSet");
        QueryDescriptor query = queryBuilder.buildLinkCollectionQuery(field);
        String etalonQuery = "update atomics as atomic set atomic.a_ref = ?"
                + " where atomic.atomic_id in (${list})";
        assertEquals(etalonQuery, query.getQuery());
        assertEquals(1, query.getParameterBinding("foreignKey"));
        assertEquals(2, query.getParameterBinding("linkedKeys"));
    }

    @Test
    public void testLinkListQuery() {
        EntityDefinition definition = getConcreteBDefinition();
        CollectionDefinition field = definition.getCollectionField("atomicList");
        QueryDescriptor query = queryBuilder.buildLinkCollectionQuery(field);
        String etalonQuery = "update atomics as atomic join (${ordering}) ordering_subquery "
                + "on atomic.atomic_id = ordering_subquery.key_column "
                + "set atomic.b_ref = ?, atomic.b_order = ordering_subquery.order_column";
        assertEquals(etalonQuery, query.getQuery());
        assertEquals(1, query.getParameterBinding("linkedKeys"));
        assertEquals(2, query.getParameterBinding("foreignKey"));
    }

    @Test
    public void testUnlinkCollectionQuery() {
        EntityDefinition definition = getConcreteADefinition();
        CollectionDefinition field = definition.getCollectionField("atomicSet");
        QueryDescriptor query = queryBuilder.buildUnlinkCollectionQuery(field);
        String etalonQuery = "update atomics as atomic set atomic.a_ref = null"
                + " where atomic.a_ref = ? and atomic.atomic_id not in (${list})";
        assertEquals(etalonQuery, query.getQuery());
        assertEquals(1, query.getParameterBinding("foreignKey"));
        assertEquals(2, query.getParameterBinding("linkedKeys"));
    }

    @Test
    public void testUnlinkListQuery() {
        EntityDefinition definition = getConcreteBDefinition();
        CollectionDefinition field = definition.getCollectionField("atomicList");
        QueryDescriptor query = queryBuilder.buildUnlinkCollectionQuery(field);
        String etalonQuery = "update atomics as atomic "
                + "set atomic.b_ref = null, atomic.b_order = null"
                + " where atomic.b_ref = ? and atomic.atomic_id not in (${list})";
        assertEquals(etalonQuery, query.getQuery());
        assertEquals(1, query.getParameterBinding("foreignKey"));
        assertEquals(2, query.getParameterBinding("linkedKeys"));
    }

    @Test
    public void testClearCollectionQuery() {
        EntityDefinition definition = getConcreteADefinition();
        CollectionDefinition field = definition.getCollectionField("atomicSet");
        QueryDescriptor query = queryBuilder.buildClearCollectionQuery(field);
        String etalonQuery = "update atomics as atomic set atomic.a_ref = null"
                + " where atomic.a_ref = ?";
        assertEquals(etalonQuery, query.getQuery());
        assertEquals(1, query.getParameterBinding("foreignKey"));
    }

    @Test
    public void testClearListQuery() {
        EntityDefinition definition = getConcreteBDefinition();
        CollectionDefinition field = definition.getCollectionField("atomicList");
        QueryDescriptor query = queryBuilder.buildClearCollectionQuery(field);
        String etalonQuery = "update atomics as atomic "
                + "set atomic.b_ref = null, atomic.b_order = null"
                + " where atomic.b_ref = ?";
        assertEquals(etalonQuery, query.getQuery());
        assertEquals(1, query.getParameterBinding("foreignKey"));
    }

    @Test
    public void testRootSelectQuery() {
        EntityDefinition definition = getRootDefinition();
        QueryDescriptor query = queryBuilder.buildSelectEntityQuery(definition);
        List<String> columns = new ArrayList<>(3);
        columns.add("root.root_id as root_id");
        EntityDefinition superDef = definition.getChildrenDefinitions().iterator().next();
        Map<String, String> columnFields = new HashMap<>();
        columnFields.put("id", "super.super_id as super_id");
        columnFields.put("number", "super.number_column as super_number");
        for (FieldDefinition field : superDef.getSingleFields()) {
            columns.add(columnFields.get(field.getName()));
        }
        EntityDefinition aDef = superDef.getChildrenDefinitions().get(0);
        columnFields = new HashMap<>();
        columnFields.put("id", "concretea.concrete_a_id as concretea_id");
        columnFields.put("time", "concretea.time_field as concretea_time");
        columnFields.put("atomic", "concretea.atomic_ref as concretea_atomic");
        for (FieldDefinition field : aDef.getSingleFields()) {
            columns.add(columnFields.get(field.getName()));
        }
        EntityDefinition bDef = superDef.getChildrenDefinitions().get(1);
        columnFields = new HashMap<>();
        columnFields.put("id", "concreteb.concrete_b_id as concreteb_id");
        columnFields.put("name", "concreteb.name_field as concreteb_name");
        columnFields.put("atomic", "concreteb.atomic_ref as concreteb_atomic");
        for (FieldDefinition field : bDef.getSingleFields()) {
            columns.add(columnFields.get(field.getName()));
        }
        String fieldList = StringUtils.join(columns.toArray(), ", ");
        String etalonQuery = "select distinct " + fieldList
                + " from roots as root left join supers as super"
                + " on root.root_id = super.super_id left join concretes_a as concretea"
                + " on super.super_id = concretea.concrete_a_id"
                + " left join concretes_b as concreteb on super.super_id = concreteb.concrete_b_id"
                + " where root.root_id = ?";
        assertEquals(etalonQuery, query.getQuery());
        assertEquals(1, query.getParameterBinding("Root.id"));
        assertEquals("root_id", query.getResultBinding("Root.id"));
        assertEquals("super_id", query.getResultBinding("Super.id"));
        assertEquals("super_number", query.getResultBinding("Super.number"));
        assertEquals("concretea_id", query.getResultBinding("ConcreteA.id"));
        assertEquals("concretea_time", query.getResultBinding("ConcreteA.time"));
        assertEquals("concretea_atomic", query.getResultBinding("ConcreteA.atomic"));
        assertEquals("concreteb_id", query.getResultBinding("ConcreteB.id"));
        assertEquals("concreteb_name", query.getResultBinding("ConcreteB.name"));
        assertEquals("concreteb_atomic", query.getResultBinding("ConcreteB.atomic"));
    }

    @Test
    public void testSuperSelectByKeysQuery() {
        EntityDefinition definition = getRootDefinition();
        List<String> columns = new ArrayList<>(3);
        columns.add("root.root_id as root_id");
        EntityDefinition superDef = definition.getChildrenDefinitions().iterator().next();
        Map<String, String> columnFields = new HashMap<>();
        columnFields.put("id", "super.super_id as super_id");
        columnFields.put("number", "super.number_column as super_number");
        for (FieldDefinition field : superDef.getSingleFields()) {
            columns.add(columnFields.get(field.getName()));
        }
        EntityDefinition aDef = superDef.getChildrenDefinitions().get(0);
        columnFields = new HashMap<>();
        columnFields.put("id", "concretea.concrete_a_id as concretea_id");
        columnFields.put("time", "concretea.time_field as concretea_time");
        columnFields.put("atomic", "concretea.atomic_ref as concretea_atomic");
        for (FieldDefinition field : aDef.getSingleFields()) {
            columns.add(columnFields.get(field.getName()));
        }
        EntityDefinition bDef = superDef.getChildrenDefinitions().get(1);
        columnFields = new HashMap<>();
        columnFields.put("id", "concreteb.concrete_b_id as concreteb_id");
        columnFields.put("name", "concreteb.name_field as concreteb_name");
        columnFields.put("atomic", "concreteb.atomic_ref as concreteb_atomic");
        for (FieldDefinition field : bDef.getSingleFields()) {
            columns.add(columnFields.get(field.getName()));
        }
        String fieldList = StringUtils.join(columns.toArray(), ", ");
        String etalonQuery = "select distinct " + fieldList
                + " from supers as super join roots as root"
                + " on super.super_id = root.root_id left join concretes_a as concretea"
                + " on super.super_id = concretea.concrete_a_id"
                + " left join concretes_b as concreteb on super.super_id = concreteb.concrete_b_id"
                + " where super.super_id in (${list})";
        QueryDescriptor query = queryBuilder.buildSelectEntitiesByKeysQuery(superDef);
        assertEquals(etalonQuery, query.getQuery());
        assertEquals(1, query.getParameterBinding("Super.id"));
        assertEquals("root_id", query.getResultBinding("Root.id"));
        assertEquals("super_id", query.getResultBinding("Super.id"));
        assertEquals("super_number", query.getResultBinding("Super.number"));
        assertEquals("concretea_id", query.getResultBinding("ConcreteA.id"));
        assertEquals("concretea_time", query.getResultBinding("ConcreteA.time"));
        assertEquals("concretea_atomic", query.getResultBinding("ConcreteA.atomic"));
        assertEquals("concreteb_id", query.getResultBinding("ConcreteB.id"));
        assertEquals("concreteb_name", query.getResultBinding("ConcreteB.name"));
        assertEquals("concreteb_atomic", query.getResultBinding("ConcreteB.atomic"));
    }

    @Test
    public void testSuperUpdateQuery() {
        EntityDefinition definition = getRootDefinition();
        EntityDefinition superDef = definition.getChildrenDefinitions().iterator().next();
        String etalonQuery = "update supers as super join roots as root "
                + "on super.super_id = root.root_id "
                + "set super.number_column = ? where super.super_id = ?";
        QueryDescriptor query = queryBuilder.buildUpdateEntityQuery(superDef);
        assertEquals(etalonQuery, query.getQuery());
        assertEquals(1, query.getParameterBinding("Super.number"));
        assertEquals(2, query.getParameterBinding("Super.id"));
    }

    @Test
    public void testSuperDeleteQuery() {
        EntityDefinition definition = getRootDefinition();
        EntityDefinition superDef = definition.getChildrenDefinitions().iterator().next();
        String etalonQuery = "delete root, super from supers as super join roots as root "
                + "on super.super_id = root.root_id "
                + "where super.super_id = ?";
        QueryDescriptor query = queryBuilder.buildDeleteEntityQuery(superDef);
        assertEquals(etalonQuery, query.getQuery());
        assertEquals(1, query.getParameterBinding("Super.id"));
    }

    @Test
    public void testSuperInsertFullQuery() {
        EntityDefinition definition = getRootDefinition()
                .getChildrenDefinitions().iterator().next();
        List<QueryDescriptor> queries = queryBuilder.buildInsertFullEntityQueries(definition);
        assertEquals(2, queries.size());
        QueryDescriptor rootQuery = queries.get(0);
        String etalonQuery = "insert into roots (root_id) values (?)";
        assertEquals(etalonQuery, rootQuery.getQuery());
        assertEquals(1, rootQuery.getParameterBinding("Root.id"));
        QueryDescriptor superQuery = queries.get(1);
        List<String> columns = new ArrayList<>(2);
        Map<String, String> columnFields = new HashMap<>();
        columnFields.put("id", "super_id");
        columnFields.put("number", "number_column");
        for (FieldDefinition field : definition.getSingleFields()) {
            columns.add(columnFields.get(field.getName()));
        }
        String fieldList = StringUtils.join(columns.toArray(), ", ");
        etalonQuery = "insert into supers (" + fieldList
                + ") values (?, ?)";
        assertEquals(etalonQuery, superQuery.getQuery());
        assertEquals(columns.indexOf("super_id") + 1, superQuery.getParameterBinding("Super.id"));
        assertEquals(columns.indexOf("number_column") + 1, superQuery.getParameterBinding("Super.number"));
    }

    @Test
    public void testSuperInsertAnemicQuery() {
        EntityDefinition definition = getRootDefinition()
                .getChildrenDefinitions().iterator().next();
        List<QueryDescriptor> queries = queryBuilder.buildInsertAnemicEntityQueries(definition);
        assertEquals(2, queries.size());
        QueryDescriptor rootQuery = queries.get(0);
        String etalonQuery = "insert into roots (root_id) values (?)";
        assertEquals(etalonQuery, rootQuery.getQuery());
        assertEquals(1, rootQuery.getParameterBinding("Root.id"));
        QueryDescriptor superQuery = queries.get(1);
        etalonQuery = "insert into supers (super_id) values (?)";
        assertEquals(etalonQuery, superQuery.getQuery());
        assertEquals(1, superQuery.getParameterBinding("Super.id"));
    }

    protected EntityDefinition getAtomicDefinition() {
        EntityDefinition definition = new EntityDefinition(null, "Atomic");
        definition.setTable("atomics");
        FieldDefinition idField = new FieldDefinition("id", "atomic_id");
        FieldDefinition nameField = new FieldDefinition("name", "atomic_name");
        definition.addSingleField(nameField);
        definition.addSingleField(idField);
        definition.setPrimaryKeyName("id");
        return definition;
    }

    protected EntityDefinition getConcreteADefinition() {
        EntityDefinition definition = new EntityDefinition(null, "ConcreteA");
        definition.setTable("concretes_a");
        FieldDefinition idField = new FieldDefinition("id", "concrete_a_id");
        FieldDefinition timeField = new FieldDefinition("time", "time_field");
        EntityDefinition atomicDef = getAtomicDefinition();
        ReferenceDefinition atomicReferenceField = new ReferenceDefinition("atomic", "atomic_ref",
                atomicDef, false);
        CollectionDefinition atomicsCollectionField = new CollectionDefinition("atomicSet", "a_ref",
                atomicDef, false);
        definition.addSingleField(idField);
        definition.addSingleField(timeField);
        definition.addSingleField(atomicReferenceField);
        definition.addCollectionField(atomicsCollectionField);
        definition.setPrimaryKeyName("id");
        return definition;
    }

    protected EntityDefinition getConcreteBDefinition() {
        EntityDefinition definition = new EntityDefinition(null, "ConcreteB");
        definition.setTable("concretes_b");
        FieldDefinition idField = new FieldDefinition("id", "concrete_b_id");
        FieldDefinition nameField = new FieldDefinition("name", "name_field");
        EntityDefinition atomicDef = getAtomicDefinition();
        ReferenceDefinition atomicReferenceField = new ReferenceDefinition("atomic", "atomic_ref",
                atomicDef, false);
        ListDefinition atomicsCollectionField = new ListDefinition("atomicList", "b_ref",
                atomicDef, "b_order", false);
        definition.addSingleField(idField);
        definition.addSingleField(nameField);
        definition.addSingleField(atomicReferenceField);
        definition.addCollectionField(atomicsCollectionField);
        definition.setPrimaryKeyName("id");
        return definition;
    }

    protected EntityDefinition getSuperDefinition() {
        EntityDefinition aDefinition = getConcreteADefinition();
        EntityDefinition bDefinition = getConcreteBDefinition();
        EntityDefinition superDefinition = new EntityDefinition(null, "Super");
        superDefinition.addChildrenEntity(aDefinition);
        superDefinition.addChildrenEntity(bDefinition);
        FieldDefinition keyField = new FieldDefinition("id", "super_id");
        FieldDefinition numberField = new FieldDefinition("number", "number_column");
        superDefinition.addSingleField(numberField);
        superDefinition.addSingleField(keyField);
        superDefinition.setTable("supers");
        superDefinition.setPrimaryKeyName("id");
        return superDefinition;
    }

    protected EntityDefinition getRootDefinition() {
        EntityDefinition superDef = getSuperDefinition();
        EntityDefinition root = new EntityDefinition(null, "Root");
        root.addChildrenEntity(superDef);
        FieldDefinition keyField = new FieldDefinition("id", "root_id");
        root.addSingleField(keyField);
        root.setTable("roots");
        root.setPrimaryKeyName("id");
        return root;
    }
}