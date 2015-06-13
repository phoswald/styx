package styx.core.intrinsics;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import styx.Session;
import styx.SessionFactory;
import styx.SessionManager;
import styx.StyxException;

public class TestCollectionIntrinsics extends Base {

    private static SessionFactory sf = SessionManager.createMemorySessionFactory(false);

    @Test
    public void testFilter() throws StyxException {
        try(Session session = sf.createSession()) {
            assertEquals("[2:\"B\",4:\"D\",6:\"F\"]", evaluate(session, " collection.filter([\"A\",\"B\",\"C\",\"D\",\"E\",\"F\"], (k,v) -> { return (k % 2) == 0 }) ").toString());
            assertEquals("[\"B\",\"D\",\"F\"]", evaluate(session, " collection.filter_vals([\"A\",\"B\",\"C\",\"D\",\"E\",\"F\"], (k,v) -> { return (k % 2) == 0 }) ").toString());

            session.evaluate("[/][*] = [ E1:[name:\"Philip\",year:1977], E2:[name:\"Oy\",year:1984], K1:[name:\"Marc\",year:2008], K2:[name:\"Liam\",year:2012] ]");
            assertEquals(
                    "[E1:[name:\"Philip\",year:1977],E2:[name:\"Oy\",year:1984]]",
                    evaluate(session, " collection.filter([/][*], (k,v) -> { return v.year < 2000 }) ").toString());
            assertEquals(
                    "[[name:\"Marc\",year:2008],[name:\"Liam\",year:2012]]",
                    evaluate(session, " collection.filter_vals([/][*], (k,v) -> { return v.year > 2000 }) ").toString());
        }
    }

    @Test
    public void testMap() throws StyxException {
        try(Session session = sf.createSession()) {
            session.evaluate("[/][*] = [ E1:[name:\"Philip\",year:1977], E2:[name:\"Oy\",year:1984], K1:[name:\"Marc\",year:2008], K2:[name:\"Liam\",year:2012] ]");
            assertEquals(
                    "[E1:[age:37,name:\"Philip\"],E2:[age:30,name:\"Oy\"],K1:[age:6,name:\"Marc\"],K2:[age:2,name:\"Liam\"]]",
                    evaluate(session, " collection.map([/][*], (k,v) -> { return [ name: v.name, age: 2014 - v.year ] }) ").toString());
            assertEquals(
                    "[[age:37,key:\"E1\",name:\"Philip\"],[age:30,key:\"E2\",name:\"Oy\"],[age:6,key:\"K1\",name:\"Marc\"],[age:2,key:\"K2\",name:\"Liam\"]]",
                    evaluate(session, " collection.map_vals([/][*], (k,v) -> { return [ key: k, name: v.name, age: 2014 - v.year ] }) ").toString());
        }
    }

    @Test
    public void testReduce() throws StyxException {
        try(Session session = sf.createSession()) {
            session.evaluate("[/][*] = [10,20,30,40]");
            assertEquals("100", evaluate(session, " collection.reduce([/][*], (v1,v2) -> { return v1 + v2 }) ").toString());
        }
    }
}
