package com.program;

import com.github.fakemongo.Fongo;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

import org.junit.Before;
import org.junit.Test;
import org.junit.After;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.apache.logging.log4j.Logger;

import java.net.UnknownHostException;

/**
   
 * @author wendy926

 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(StringBuilder.class)
public class TestMongodbRebuild {
    private static final Logger logger =
            Log.getLog(TestMongodbRebuild.class.getName());
    private MongoDBRebuild _mongodbrebuild = null;
    private Mongo _mongo = null;
    private Fongo _fongo = null;
    private String _dbName = "dbname";
    private String _collection = "collection";
    private String _index = "index";
    private String _shading = "shading";
    private DBCollection _dbcol = null;
    private StringBuilder _debugInfo = null;

    @Before
    public void setup() {
        _fongo = new Fongo("crawler mongo server");
        _mongo = _fongo.getMongo();
        _debugInfo = PowerMockito.mock(StringBuilder.class);
        _mongodbrebuild = new MongoDBRebuild(_mongo);
    }

    @Test 
    public void testCreateCollection() {
        boolean create = _mongodbrebuild.createCollection(_dbName, _collection, _index, _shading, _debugInfo);
        DB db = _fongo.getDB(_dbName);
        DBCollection collection = db.getCollection(_collection);
        Assert.assertTrue(create);
        Assert.assertNotNull(db);
        Assert.assertNotNull(collection);
    }

    @Test
    public void testDelete() {
        boolean create = _mongodbrebuild.createCollection(_dbName, _collection, _index, _shading, _debugInfo);
        boolean dele = _mongodbrebuild.delete(_dbName, _collection, _debugInfo);
        Assert.assertTrue(dele);
    }
}
