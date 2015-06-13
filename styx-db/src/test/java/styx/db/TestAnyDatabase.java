package styx.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;

import styx.StyxException;
import styx.core.sessions.TestBase;

public abstract class TestAnyDatabase extends TestBase {

    protected abstract RowDatabase newDatabase() throws StyxException;

    @Test
    public void testOpen() throws StyxException {
        try(RowDatabase db = newDatabase()) {
            // nop
        }
    }

    @Test
    public void testDatabase() throws StyxException {
        try(RowDatabase db = newDatabase()) {
            db.beginTransaction();

            db.deleteAll();
            db.insert("", "f",   1, "{}");
            db.insert("", "foo", 2, "{}");
            db.insert("", "me",  3, "{}");
            db.insert("", "you", 4, "{}");
            db.insert("1/", "x",    1, "y");
            db.insert("2/", "bar",  1, "baz");
            db.insert("3/", "age",  1, "36");
            db.insert("3/", "more", 2, "{}");
            db.insert("3/", "name", 3, "philip");
            db.insert("3/2/",  "1", 1, "eins");
            db.insert("3/2/",  "2", 2, "zwei");
            db.insert("3/2/",  "3", 3, "{}");
            db.insert("3/2/3/", "a", 1, "drei");
            db.insert("3/2/3/", "b", 2, "three");
            db.insert("4/", "age",  1, "6");
            db.insert("4/", "more", 2, "{}");
            db.insert("4/", "name", 3, "marc");
            db.insert("4/2/",  "1", 1, "eins");
            db.insert("4/2/",  "2", 2, "zwei");
            db.insert("4/2/",  "3", 3, "{}");

            List<Row> chldrn = db.selectChildren("");
            assertEquals(4, chldrn.size());
            assertEquals("f",   chldrn.get(0).name);
            assertEquals("foo", chldrn.get(1).name);
            assertEquals("me",  chldrn.get(2).name);
            assertEquals("you", chldrn.get(3).name);

            chldrn = db.selectChildren("3/");
            assertEquals(3, chldrn.size());
            assertEquals("age",   chldrn.get(0).name);
            assertEquals("more",  chldrn.get(1).name);
            assertEquals("name",  chldrn.get(2).name);

            chldrn = db.selectChildren("me!more!");
            chldrn = db.selectChildren("3/2/");
            assertEquals(3, chldrn.size());
            assertEquals("1", chldrn.get(0).name);
            assertEquals("2", chldrn.get(1).name);
            assertEquals("3", chldrn.get(2).name);

            List<Row> dscndts = db.selectDescendants("3/2/");
            assertEquals(5, dscndts.size());
            assertEquals("3/2/1/",   dscndts.get(0).key());
            assertEquals("3/2/2/",   dscndts.get(1).key());
            assertEquals("3/2/3/",   dscndts.get(2).key());
            assertEquals("3/2/3/1/", dscndts.get(3).key());
            assertEquals("3/2/3/2/", dscndts.get(4).key());
            assertEquals("eins",  dscndts.get(0).value);
            assertEquals("zwei",  dscndts.get(1).value);
            assertEquals("{}",    dscndts.get(2).value);
            assertEquals("drei",  dscndts.get(3).value);
            assertEquals("three", dscndts.get(4).value);

            db.update      ("3/2/3/", "a", "DREI");
            db.deleteSingle("3/2/3/", "b");
            assertEquals("DREI", db.selectSingle("3/2/3/", "a").value);
            assertNull(db.selectSingle("3/2/3/", "b"));
            dscndts = db.selectDescendants("3/2/");
            assertEquals(4, dscndts.size());
            assertEquals("3/2/1/",   dscndts.get(0).key());
            assertEquals("3/2/2/",   dscndts.get(1).key());
            assertEquals("3/2/3/",   dscndts.get(2).key());
            assertEquals("3/2/3/1/", dscndts.get(3).key());
            assertEquals("eins",     dscndts.get(0).value);
            assertEquals("zwei",     dscndts.get(1).value);
            assertEquals("{}",       dscndts.get(2).value);
            assertEquals("DREI",     dscndts.get(3).value);

            db.deleteDescendants("3/2/");
            dscndts = db.selectDescendants("3/2/");
            assertEquals(0, dscndts.size());

            dscndts = db.selectDescendants("3/");
            assertEquals(3, dscndts.size());
            assertEquals("3/1/", dscndts.get(0).key());
            assertEquals("3/2/", dscndts.get(1).key());
            assertEquals("3/3/", dscndts.get(2).key());

            db.commitTransaction();
        }
    }

    @Test
    public void testTransactions() throws StyxException {
        try(RowDatabase db = newDatabase()) {
            db.deleteAll();
            List<Row> res = db.selectAll();
            assertEquals(0, res.size());

            db.beginTransaction();
            db.insert("P1", "N1", 1, "V1");
            db.commitTransaction();

            db.insert("P2", "N2", 2, "V2");

            db.beginTransaction();
            db.insert("P3", "N3", 3, "V3");
            res = db.selectAll();
            assertEquals(3, res.size());
            db.abortTransaction(false);

            res = db.selectAll();
            assertEquals(2, res.size());
            assertEquals("parent=P1, name=N1, suffix=1, value=V1", format(res.get(0)));
            assertEquals("parent=P2, name=N2, suffix=2, value=V2", format(res.get(1)));
        }

        try(RowDatabase db = newDatabase()) {
            db.beginTransaction();
            List<Row> res = db.selectAll();
            db.commitTransaction();
            assertEquals(2, res.size());
            assertEquals("parent=P1, name=N1, suffix=1, value=V1", format(res.get(0)));
            assertEquals("parent=P2, name=N2, suffix=2, value=V2", format(res.get(1)));
        }
    }

//    private static void dump(RowDatabase db) throws StyxException {
//        System.out.println("selectAll():");
//        for(Row row : db.selectAll()) {
//            System.out.println("  " + format(row));
//        }
//    }

    protected static String format(Row row) {
        return "parent=" + row.parent + ", name=" + row.name + ", suffix=" + row.suffix + ", value="+ row.value;
    }
}
